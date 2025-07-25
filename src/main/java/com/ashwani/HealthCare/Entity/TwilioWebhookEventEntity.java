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
@Table(name = "twilio_webhook_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TwilioWebhookEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_sid", nullable = false)
    private String roomSid;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "participant_sid")
    private String participantSid;

    @Column(name = "participant_identity")
    private String participantIdentity;

    @Column(name = "track_sid")
    private String trackSid;

    @Column(name = "recording_sid")
    private String recordingSid;

    @Column(name = "event_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode eventData;

    @Column(name = "processed")
    private Boolean processed = false;

    @CreatedDate
    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
