package com.ashwani.HealthCare.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;         // Cashfree order ID
    private String status;          // Payment status (PAID, FAILED, etc.)
    private String referenceId;     // Cashfree transaction/reference ID
    private String paymentMode;     // UPI, card, etc.
    private String transactionTime; // Time of payment
    private Double orderAmount;
}
