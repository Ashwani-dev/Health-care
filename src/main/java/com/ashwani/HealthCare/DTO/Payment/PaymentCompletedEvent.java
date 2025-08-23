package com.ashwani.HealthCare.DTO.Payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent implements Serializable {
    private String orderId; // Useful for linking back
    private String referenceId; // The actual payment transaction ID
    private String customerId;
    private BigDecimal orderAmount;
    private String paymentMode;

    private String appointmentHoldReference;

}
