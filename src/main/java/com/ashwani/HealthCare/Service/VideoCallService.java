package com.ashwani.HealthCare.Service;

import com.ashwani.HealthCare.Config.TwilioConfig;
import com.ashwani.HealthCare.DTO.VideoSession.VideoSession;
import com.ashwani.HealthCare.Entity.AppointmentEntity;
import com.ashwani.HealthCare.Entity.TwilioWebhookEventEntity;
import com.ashwani.HealthCare.Entity.VideoCallEventEntity;
import com.ashwani.HealthCare.Entity.VideoCallSessionsEntity;
import com.ashwani.HealthCare.Repository.AppointmentRepository;
import com.ashwani.HealthCare.Repository.TwilioWebhookEventRepository;
import com.ashwani.HealthCare.Repository.VideoCallEventRepository;
import com.ashwani.HealthCare.Repository.VideoCallSessionsRepository;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VideoGrant;
import com.twilio.rest.video.v1.Room;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoCallService {

    private final TwilioConfig twilioConfig;
    private final VideoCallSessionsRepository videoCallSessionsRepository;
    private final AppointmentRepository appointmentRepository;
    private final VideoCallEventRepository videoCallEventRepository;
    private final TwilioWebhookEventRepository twilioWebhookEventRepository;
    private final ModelMapper mapper;

    @Transactional
    public VideoSession createVideoSession(Long appointmentId) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Check if session already exists
        Optional<VideoCallSessionsEntity> existingSession = videoCallSessionsRepository.findByAppointmentId(appointmentId);
        if (existingSession.isPresent()) {
            return mapToVideoSession(existingSession.get());
        }

        // Create unique room name
        String roomName = "healthcare-" + UUID.randomUUID().toString();

        // Create Twilio room
        Room room = Room.creator()
                .setUniqueName(roomName)
                .setType(Room.RoomType.GROUP)
                .setMaxParticipants(2)
                .create();

        // Create session entity
        VideoCallSessionsEntity session = VideoCallSessionsEntity.builder()
                .appointment(appointment)
                .twilioRoomSid(room.getSid())
                .twilioRoomName(roomName)
                .roomStatus(VideoCallSessionsEntity.RoomStatus.CREATED)
                .maxParticipants(2)
                .recordingEnabled(false)
                .build();

        // Generate access tokens
        String patientToken = generateAccessToken(appointment.getPatient().getId(), roomName, "PATIENT");
        String doctorToken = generateAccessToken(appointment.getDoctor().getId(), roomName, "DOCTOR");

        session.setPatientAccessToken(patientToken);
        session.setDoctorAccessToken(doctorToken);
        session.setCreatedAt(LocalDateTime.now());

        VideoCallSessionsEntity savedSession = videoCallSessionsRepository.save(session);

        // Log room creation event
        logVideoCallEvent(savedSession, VideoCallEventEntity.EventType.ROOM_CREATED, null, null, null, null);

        return mapToVideoSession(savedSession);
    }

    public VideoSession getVideoSession(Long appointmentId) {
        VideoCallSessionsEntity session = videoCallSessionsRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Video session not found"));
        return mapToVideoSession(session);
    }

    public String getAccessToken(Long appointmentId, String userType, Long userId) {
        VideoCallSessionsEntity session = videoCallSessionsRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Video session not found"));

        // Validate user is either patient or doctor for this appointment
        if (userType.equals("PATIENT")) {
            if (!session.getAppointment().getPatient().getId().equals(userId)) {
                throw new RuntimeException("Unauthorized access");
            }
            return session.getPatientAccessToken();
        } else if (userType.equals("DOCTOR")) {
            if (!session.getAppointment().getDoctor().getId().equals(userId)) {
                throw new RuntimeException("Unauthorized access");
            }
            return session.getDoctorAccessToken();
        } else {
            throw new RuntimeException("Invalid user type");
        }
    }

    @Transactional
    public void endVideoSession(Long appointmentId) {
        VideoCallSessionsEntity session = videoCallSessionsRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Video session not found"));

        // Update room status
        session.setRoomStatus(VideoCallSessionsEntity.RoomStatus.COMPLETED);
        session.setCallEndedAt(LocalDateTime.now());
        videoCallSessionsRepository.save(session);

        // Log room ended event
        logVideoCallEvent(session, VideoCallEventEntity.EventType.ROOM_ENDED, null, null, null, null);

        // Complete room in Twilio with error handling
        try {
            if (session.getTwilioRoomSid() != null && !session.getTwilioRoomSid().isEmpty()) {
                Room room = Room.updater(session.getTwilioRoomSid(), Room.RoomStatus.COMPLETED)
                        .update();
                log.info("Successfully completed Twilio room: {}", session.getTwilioRoomSid());
            } else {
                log.warn("No Twilio room SID found for appointment: {}", appointmentId);
            }
        } catch (com.twilio.exception.ApiException e) {
            if (e.getCode() == 20404) { // Room not found
                log.warn("Twilio room not found (already completed/deleted): {}", session.getTwilioRoomSid());
                // This is not a critical error - the room might have been auto-completed
            } else {
                log.error("Failed to complete Twilio room: {}", e.getMessage(), e);
                // You might want to throw this error depending on your requirements
            }
        } catch (Exception e) {
            log.error("Unexpected error completing Twilio room: {}", e.getMessage(), e);
            // Handle other unexpected errors
        }
    }

    @Transactional
    public void handleParticipantJoined(Long appointmentId, String participantIdentity, String participantSid) {
        VideoCallSessionsEntity session = videoCallSessionsRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Video session not found"));

        if (participantIdentity.startsWith("PATIENT-")) {
            session.setPatientJoined(true);
            session.setPatientJoinedAt(LocalDateTime.now());
        } else if (participantIdentity.startsWith("DOCTOR-")) {
            session.setDoctorJoined(true);
            session.setDoctorJoinedAt(LocalDateTime.now());
        }

        // If both participants joined, mark call as started
        if (session.getPatientJoined() && session.getDoctorJoined()) {
            session.setRoomStatus(VideoCallSessionsEntity.RoomStatus.IN_PROGRESS);
            session.setCallStartedAt(LocalDateTime.now());
        }

        videoCallSessionsRepository.save(session);

        // Log participant connected event
        logVideoCallEvent(session, VideoCallEventEntity.EventType.PARTICIPANT_CONNECTED,
                participantIdentity, participantSid, null, null);
    }

    @Transactional
    public void handleParticipantLeft(Long appointmentId, String participantIdentity, String participantSid) {
        VideoCallSessionsEntity session = videoCallSessionsRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Video session not found"));

        if (participantIdentity.startsWith("PATIENT-")) {
            session.setPatientJoined(false);
        } else if (participantIdentity.startsWith("DOCTOR-")) {
            session.setDoctorJoined(false);
        }

        videoCallSessionsRepository.save(session);

        // Log participant disconnected event
        logVideoCallEvent(session, VideoCallEventEntity.EventType.PARTICIPANT_DISCONNECTED,
                participantIdentity, participantSid, null, null);
    }

    @Transactional
    public void processTwilioWebhook(TwilioWebhookEventEntity twilioWebhookEventEntity) {
        // Save webhook event
        TwilioWebhookEventEntity savedEvent = twilioWebhookEventRepository.save(twilioWebhookEventEntity);

        try {
            // Process different event types
            switch (twilioWebhookEventEntity.getEventType()) {
                case "room-created":
                    // Handle room created
                    break;
                case "participant-connected":
                    // Handle participant connected
                    break;
                case "participant-disconnected":
                    // Handle participant disconnected
                    break;
                case "track-published":
                    // Handle track published
                    break;
                case "track-unpublished":
                    // Handle track unpublished
                    break;
                case "room-ended":
                    // Handle room ended
                    break;
                default:
                    log.warn("Unhandled Twilio webhook event type: {}", twilioWebhookEventEntity.getEventType());
            }

            savedEvent.setProcessed(true);
            savedEvent.setProcessedAt(LocalDateTime.now());
            twilioWebhookEventRepository.save(savedEvent);
        } catch (Exception e) {
            log.error("Error processing Twilio webhook event: {}", e.getMessage(), e);
        }
    }

    private String generateAccessToken(Long userId, String roomName, String userType) {
        String identity = userType + "-" + userId;

        VideoGrant grant = new VideoGrant();
        grant.setRoom(roomName);

        AccessToken token = new AccessToken.Builder(
                twilioConfig.getAccountSid(),
                twilioConfig.getApiKey(),
                twilioConfig.getApiSecret()
        ).identity(identity).grant(grant).build();

        return token.toJwt();
    }

    private void logVideoCallEvent(VideoCallSessionsEntity session, VideoCallEventEntity.EventType eventType,
                                   String participantIdentity, String participantSid,
                                   String trackSid, VideoCallEventEntity.TrackKind trackKind) {
        VideoCallEventEntity event = VideoCallEventEntity.builder()
                .session(session)
                .eventType(eventType)
                .participantIdentity(participantIdentity)
                .participantSid(participantSid)
                .trackSid(trackSid)
                .trackKind(trackKind)
                .timestamp(LocalDateTime.now())
                .build();

        videoCallEventRepository.save(event);
    }

    private VideoSession mapToVideoSession(VideoCallSessionsEntity entity) {
        return mapper.map(entity, VideoSession.class);
    }
}