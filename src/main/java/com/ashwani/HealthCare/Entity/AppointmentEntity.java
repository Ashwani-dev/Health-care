package com.ashwani.HealthCare.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class AppointmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorEntity doctor;

    @Column(nullable = false)
    private LocalDate appointmentDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column
    private String status = "SCHEDULED";

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_id", unique = true)
    private PaymentEntity paymentDetails;

    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column
    private Long cancelledBy;

    @Column
    private int callDurationMinutes = 30;

    @Column
    private int joinBufferMinutes = 10;

    @Column
    private Boolean recordingEnabled = false;

    @Column
    private Boolean recordingConsent = false;

    public boolean belongsToPatient(Long patientId) {
        return this.patient != null && this.patient.getId().equals(patientId);
    }

    public boolean isPastCancellationDeadline() {
        LocalDateTime appointmentDateTime = LocalDateTime.of(
                this.appointmentDate,
                this.startTime
        );
        // 24h before appointment
        return LocalDateTime.now()
                .isAfter(appointmentDateTime.minusHours(24));
    }

    /**
     * Marks the appointment as cancelled with timestamp
     * @param cancelledByUser The user who initiated cancellation
     * @throws IllegalStateException if already cancelled
     */
    public void cancel(Long cancelledByUser) {
        if ("CANCELLED".equals(this.status)) {
            throw new IllegalStateException("Appointment already cancelled");
        }

        this.status = "CANCELLED";
        this.cancelledAt = OffsetDateTime.now();
        this.cancelledBy = cancelledByUser;
    }

    public LocalDateTime getAppointmentDateTime() {
        LocalDateTime appointmentDateTime = LocalDateTime.of(
                this.appointmentDate,
                this.startTime
        );
        return LocalDateTime.now();
    }
}
