package com.ashwani.HealthCare.DTO.VideoSession;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoSession {
    private Long id;
    private Long appointmentId;
    private String twilioRoomSid;
    private String twilioRoomName;
    private String roomStatus;
    private Boolean patientJoined;
    private Boolean doctorJoined;
    private LocalDateTime patientJoinedAt;
    private LocalDateTime doctorJoinedAt;
    private LocalDateTime callStartedAt;
    private LocalDateTime callEndedAt;
    private Boolean recordingEnabled;
    private String recordingSid;
    private LocalDateTime createdAt;
    private String patientAccessToken;
    private String doctorAccessToken;
}

