package com.ashwani.HealthCare.DTO.Payment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private String orderId;
    private String paymentSessionId;
}
