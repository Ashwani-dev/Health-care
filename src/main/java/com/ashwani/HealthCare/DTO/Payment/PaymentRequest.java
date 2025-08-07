package com.ashwani.HealthCare.DTO.Payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private BigDecimal amount;
}
