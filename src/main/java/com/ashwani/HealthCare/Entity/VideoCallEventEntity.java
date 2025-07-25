package com.ashwani.HealthCare.Entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_call_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class VideoCallEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private VideoCallSessionsEntity session;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "participant_identity")
    private String participantIdentity;

    @Column(name = "participant_sid")
    private String participantSid;

    @Column(name = "track_sid")
    private String trackSid;

    @Enumerated(EnumType.STRING)
    @Column(name = "track_kind")
    private TrackKind trackKind;

    @Column(name = "event_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode eventData;

    @Column(name = "twilio_event_id")
    private String twilioEventId;

    @CreatedDate
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    public enum EventType {
        ROOM_CREATED, PARTICIPANT_CONNECTED, PARTICIPANT_DISCONNECTED,
        TRACK_PUBLISHED, TRACK_UNPUBLISHED, TRACK_SUBSCRIBED, TRACK_UNSUBSCRIBED,
        RECORDING_STARTED, RECORDING_STOPPED, RECORDING_FAILED,
        ROOM_ENDED, ROOM_FAILED
    }

    public enum TrackKind {
        AUDIO, VIDEO, DATA
    }

}
