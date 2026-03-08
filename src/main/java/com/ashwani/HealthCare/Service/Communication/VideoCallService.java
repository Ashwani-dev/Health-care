package com.ashwani.HealthCare.Service.Communication;

import com.ashwani.HealthCare.Config.TwilioConfig;
import com.ashwani.HealthCare.DTO.VideoSession.VideoSession;
import com.ashwani.HealthCare.Entity.Appointment;
import com.ashwani.HealthCare.Entity.TwilioWebhookEvent;
import com.ashwani.HealthCare.Entity.VideoCallEvent;
import com.ashwani.HealthCare.Entity.VideoCallSessions;
import com.ashwani.HealthCare.ExceptionHandlers.auth.UnauthorizedAccessException;
import com.ashwani.HealthCare.ExceptionHandlers.common.ResourceNotFoundException;
import com.ashwani.HealthCare.ExceptionHandlers.communication.VideoCallException;
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
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        // Check if session already exists
        Optional<VideoCallSessions> existingSession = videoCallSessionsRepository.findByAppointmentId(appointmentId);
        if (existingSession.isPresent()) {
            return mapToVideoSession(existingSession.get());
        }

        // Create unique room name
        String roomName = "healthcare-" + appointmentId.toString();

        // Create Twilio room
        try {
            Room room = Room.creator()
                    .setUniqueName(roomName)
                    .setType(Room.RoomType.GROUP)
                    .setMaxParticipants(2)
                    .create();

            // Create session entity
            VideoCallSessions session = VideoCallSessions.builder()
                    .appointment(appointment)
                    .twilioRoomSid(room.getSid())
                    .twilioRoomName(roomName)
                    .roomStatus(VideoCallSessions.RoomStatus.CREATED)
                    .maxParticipants(2)
                    .recordingEnabled(false)
                    .build();

            // Generate access tokens
            String patientToken = generateAccessToken(appointment.getPatient().getId(), roomName, "PATIENT");
            String doctorToken = generateAccessToken(appointment.getDoctor().getId(), roomName, "DOCTOR");

            session.setPatientAccessToken(patientToken);
            session.setDoctorAccessToken(doctorToken);

            VideoCallSessions savedSession = videoCallSessionsRepository.save(session);

            // Log room creation event
            logVideoCallEvent(savedSession, VideoCallEvent.EventType.ROOM_CREATED, null, null, null, null);

            return mapToVideoSession(savedSession);
        } catch (com.twilio.exception.ApiException e) {
            log.error("❌ Twilio API error creating room for appointment: {}", appointmentId, e);
            throw new VideoCallException("Failed to create Twilio video room", roomName, "ROOM_CREATION_FAILED");
        } catch (Exception e) {
            log.error("❌ Unexpected error creating video session for appointment: {}", appointmentId, e);
            throw new VideoCallException("Failed to create video session", roomName, "UNKNOWN_ERROR");
        }
    }

    public VideoSession getVideoSession(Long appointmentId) {
        VideoCallSessions session = videoCallSessionsRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Video session", appointmentId));
        return mapToVideoSession(session);
    }

    public String getAccessToken(Long appointmentId, String userType, Long userId) {
        // Input validation
        if (appointmentId == null || userId == null) {
            throw new IllegalArgumentException("Appointment ID and User ID cannot be null");
        }

        // Check if video session exists for the appointment
        if (!videoCallSessionsRepository.existsByAppointmentId(appointmentId)) {
            throw new ResourceNotFoundException("Video session", appointmentId);
        }

        // Check if user has access to this appointment
        if (!videoCallSessionsRepository.existsByAppointmentIdAndUserId(appointmentId, userId)) {
            throw new UnauthorizedAccessException("You don't have access to this appointment", userId, "Appointment");
        }

        return switch (userType) {
            case "PATIENT" -> videoCallSessionsRepository.findPatientAccessToken(appointmentId, userId)
                    .orElseThrow(() -> new UnauthorizedAccessException("Patient access denied for this video session", userId, "VideoSession"));
            case "DOCTOR" -> videoCallSessionsRepository.findDoctorAccessToken(appointmentId, userId)
                    .orElseThrow(() -> new UnauthorizedAccessException("Doctor access denied for this video session", userId, "VideoSession"));
            default ->
                    throw new IllegalArgumentException("Invalid user type: " + userType + ". Must be 'PATIENT' or 'DOCTOR'");
        };
    }

    @Transactional
    public void endVideoSession(Long appointmentId) {
        VideoCallSessions session = videoCallSessionsRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Video session", appointmentId));

        // Update room status
        session.setRoomStatus(VideoCallSessions.RoomStatus.COMPLETED);
        session.setCallEndedAt(LocalDateTime.now());
        videoCallSessionsRepository.save(session);

        // Log room ended event
        logVideoCallEvent(session, VideoCallEvent.EventType.ROOM_ENDED, null, null, null, null);

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
                throw new VideoCallException("Failed to end Twilio video room", session.getTwilioRoomName(), "TWILIO_API_ERROR");
            }
        } catch (Exception e) {
            log.error("Unexpected error completing Twilio room: {}", e.getMessage(), e);
            throw new VideoCallException("Unexpected error ending video session", session.getTwilioRoomName(), "UNKNOWN_ERROR");
        }
    }

    @Transactional
    public void handleParticipantJoined(Long appointmentId, String participantIdentity, String participantSid) {
        VideoCallSessions session = videoCallSessionsRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Video session", appointmentId));

        if (participantIdentity.startsWith("PATIENT-")) {
            session.setPatientJoined(true);
            session.setPatientJoinedAt(LocalDateTime.now());
        } else if (participantIdentity.startsWith("DOCTOR-")) {
            session.setDoctorJoined(true);
            session.setDoctorJoinedAt(LocalDateTime.now());
        }

        // If both participants joined, mark call as started
        if (session.getPatientJoined() && session.getDoctorJoined()) {
            session.setRoomStatus(VideoCallSessions.RoomStatus.IN_PROGRESS);
            session.setCallStartedAt(LocalDateTime.now());
        }

        videoCallSessionsRepository.save(session);

        // Log participant connected event
        logVideoCallEvent(session, VideoCallEvent.EventType.PARTICIPANT_CONNECTED,
                participantIdentity, participantSid, null, null);
    }

    @Transactional
    public void handleParticipantLeft(Long appointmentId, String participantIdentity, String participantSid) {
        VideoCallSessions session = videoCallSessionsRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Video session", appointmentId));

        if (participantIdentity.startsWith("PATIENT-")) {
            session.setPatientJoined(false);
        } else if (participantIdentity.startsWith("DOCTOR-")) {
            session.setDoctorJoined(false);
        }

        videoCallSessionsRepository.save(session);

        // Log participant disconnected event
        logVideoCallEvent(session, VideoCallEvent.EventType.PARTICIPANT_DISCONNECTED,
                participantIdentity, participantSid, null, null);
    }

    @Transactional
    public void processTwilioWebhook(TwilioWebhookEvent twilioWebhookEvent) {
        // Check if this is a test webhook
        if (isTestTwilioWebhook(twilioWebhookEvent)) {
            log.info("Test Twilio webhook received: {}", twilioWebhookEvent.getEventType());
            return;
        }

        // Save webhook event
        TwilioWebhookEvent savedEvent = twilioWebhookEventRepository.save(twilioWebhookEvent);

        try {
            Long appointmentId = Long.parseLong(savedEvent.getRoomSid().substring("healthcare-".length()));
            String participantIdentity = savedEvent.getParticipantIdentity();
            String participantSid = savedEvent.getParticipantSid();
            // Process different event types
            switch (twilioWebhookEvent.getEventType()) {
                case "room-created":
                    // Handle room created
                    break;
                case "participant-connected":
                    handleParticipantJoined(appointmentId, participantIdentity, participantSid);
                    break;
                case "participant-disconnected":
                    handleParticipantLeft(appointmentId, participantIdentity, participantSid);
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
                    log.warn("Unhandled Twilio webhook event type: {}", twilioWebhookEvent.getEventType());
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

    private void logVideoCallEvent(VideoCallSessions session, VideoCallEvent.EventType eventType,
                                   String participantIdentity, String participantSid,
                                   String trackSid, VideoCallEvent.TrackKind trackKind) {
        VideoCallEvent event = VideoCallEvent.builder()
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

    private boolean isTestTwilioWebhook(TwilioWebhookEvent twilioWebhookEvent) {
        // Check if this is a test webhook by looking for test indicators
        if (twilioWebhookEvent.getRoomSid() == null || twilioWebhookEvent.getRoomSid().isEmpty()) {
            return true;
        }
        
        // Check if roomSid doesn't start with "healthcare-" (our expected format)
        if (!twilioWebhookEvent.getRoomSid().startsWith("healthcare-")) {
            return true;
        }
        
        // Check if eventType is null or empty
        if (twilioWebhookEvent.getEventType() == null || twilioWebhookEvent.getEventType().isEmpty()) {
            return true;
        }
        
        return false;
    }

    private VideoSession mapToVideoSession(VideoCallSessions entity) {
        return mapper.map(entity, VideoSession.class);
    }
}