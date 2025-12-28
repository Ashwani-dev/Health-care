package com.ashwani.HealthCare.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "appointment_hold")
@EntityListeners(AuditingEntityListener.class)
public class AppointmentHold {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String holdReference;
    private Long patientId;
    private Long doctorId;
    private LocalDate date;
    private LocalTime startTime;
    private String reason;
    private LocalDateTime expiresAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Pre-persist method to generate the readable reference
    @PrePersist
    public void generateHoldReference() {
        this.holdReference = "hold_" + UUID.randomUUID().toString().substring(0, 8);
    }

}
