package com.ashwani.HealthCare.Controllers;

import com.ashwani.HealthCare.DTO.VideoSession.VideoSession;
import com.ashwani.HealthCare.Entity.TwilioWebhookEventEntity;
import com.ashwani.HealthCare.Service.VideoCallService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video-call")
@RequiredArgsConstructor
public class VideoCallController {

    private final VideoCallService videoCallService;

    @PostMapping("/session/{appointmentId}")
    /**
     * Create a new video call session for an appointment
     * @param appointmentId Appointment ID
     * @return Video session details
     */
    public ResponseEntity<VideoSession> createVideoSession(@PathVariable Long appointmentId) {
        VideoSession session = videoCallService.createVideoSession(appointmentId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/session/{appointmentId}")
    /**
     * Get an existing video call session for an appointment
     * @param appointmentId Appointment ID
     * @return Video session details
     */
    public ResponseEntity<VideoSession> getVideoSession(@PathVariable Long appointmentId) {
        VideoSession session = videoCallService.getVideoSession(appointmentId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/token/{appointmentId}")
    /**
     * Generate an access token for joining a video call
     * @param appointmentId Appointment ID
     * @param userType User type (e.g., DOCTOR or PATIENT)
     * @param userId User ID
     * @return Access token string
     */
    public ResponseEntity<String> getAccessToken(
            @PathVariable Long appointmentId,
            @RequestParam String userType,
            @RequestParam Long userId) {
        String token = videoCallService.getAccessToken(appointmentId, userType, userId);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/end/{appointmentId}")
    /**
     * End an existing video call session
     * @param appointmentId Appointment ID
     * @return 200 OK on success
     */
    public ResponseEntity<Void> endVideoSession(@PathVariable Long appointmentId) {
        videoCallService.endVideoSession(appointmentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/webhook")
    /**
     * Handle incoming Twilio webhook events
     * @param event Webhook event payload
     * @return 200 OK on success
     */
    public ResponseEntity<Void> handleTwilioWebhook(@RequestBody TwilioWebhookEventEntity event) {
        videoCallService.processTwilioWebhook(event);
        return ResponseEntity.ok().build();
    }
}