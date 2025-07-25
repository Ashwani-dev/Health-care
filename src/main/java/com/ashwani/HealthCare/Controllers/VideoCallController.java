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
    public ResponseEntity<VideoSession> createVideoSession(@PathVariable Long appointmentId) {
        VideoSession session = videoCallService.createVideoSession(appointmentId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/session/{appointmentId}")
    public ResponseEntity<VideoSession> getVideoSession(@PathVariable Long appointmentId) {
        VideoSession session = videoCallService.getVideoSession(appointmentId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/token/{appointmentId}")
    public ResponseEntity<String> getAccessToken(
            @PathVariable Long appointmentId,
            @RequestParam String userType,
            @RequestParam Long userId) {
        String token = videoCallService.getAccessToken(appointmentId, userType, userId);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/end/{appointmentId}")
    public ResponseEntity<Void> endVideoSession(@PathVariable Long appointmentId) {
        videoCallService.endVideoSession(appointmentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleTwilioWebhook(@RequestBody TwilioWebhookEventEntity event) {
        videoCallService.processTwilioWebhook(event);
        return ResponseEntity.ok().build();
    }
}