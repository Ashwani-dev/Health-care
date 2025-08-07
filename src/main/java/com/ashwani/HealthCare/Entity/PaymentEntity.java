package com.ashwani.HealthCare.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
