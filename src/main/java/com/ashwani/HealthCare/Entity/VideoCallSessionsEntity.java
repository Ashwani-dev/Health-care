package com.ashwani.HealthCare.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_call_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class VideoCallSessionsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private AppointmentEntity appointment;

    @Column(name = "twilio_room_sid", unique = true)
    private String twilioRoomSid;

    @Column(name = "twilio_room_name", unique = true, nullable = false)
    private String twilioRoomName;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_status", nullable = false)
    private RoomStatus roomStatus = RoomStatus.CREATED;

    @Column(name = "patient_access_token", columnDefinition = "TEXT")
    private String patientAccessToken;

    @Column(name = "doctor_access_token", columnDefinition = "TEXT")
    private String doctorAccessToken;

    @Column(name = "patient_joined")
    private Boolean patientJoined = false;

    @Column(name = "doctor_joined")
    private Boolean doctorJoined = false;

    @Column(name = "patient_joined_at")
    private LocalDateTime patientJoinedAt;

    @Column(name = "doctor_joined_at")
    private LocalDateTime doctorJoinedAt;

    @Column(name = "call_started_at")
    private LocalDateTime callStartedAt;

    @Column(name = "call_ended_at")
    private LocalDateTime callEndedAt;

    @Column(name = "recording_enabled")
    private Boolean recordingEnabled = false;

    @Column(name = "recording_sid")
    private String recordingSid;

    @Column(name = "max_participants")
    private Integer maxParticipants = 2;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RoomStatus {
        CREATED, IN_PROGRESS, COMPLETED, FAILED
    }
}
