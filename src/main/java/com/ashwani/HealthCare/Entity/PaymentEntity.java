package com.ashwani.HealthCare.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", unique = true)
    private String orderId;         // Cashfree order ID
    
    private String status;          // PaymentEntity status (PAID, FAILED, etc.)
    @Column(name = "reference_id", unique = true)
    private String referenceId;     // Cashfree transaction/reference ID
    @Column(name = "payment_mode")
    private String paymentMode;     // UPI, card, etc.
    @Column(name = "transaction_time")
    private String transactionTime; // Time of payment
    @Column(name = "order_amount")
    private BigDecimal orderAmount;

    @Column(name = "patient_id", nullable = false) // Make it mandatory
    private Long patientId;

    // Only store the reference, not the data
    private String appointmentHoldReference;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
