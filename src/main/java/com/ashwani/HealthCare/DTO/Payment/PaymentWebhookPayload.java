package com.ashwani.HealthCare.DTO.Payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.util.Map;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class PaymentWebhookPayload {
    private Map<String, Object> data;
    private String type;
    private String event_time;

    // Helper methods to safely extract values
    public String getOrderId() {
        return data != null ? (String) data.get("orderId") : null;
    }

    public Double getOrderAmount() {
        return data != null ? (Double) data.get("orderAmount") : null;
    }

    public String getOrderStatus() {
        return data != null ? (String) data.get("orderStatus") : null;
    }

    public String getReferenceId() {
        return data != null ? (String) data.get("referenceId") : null;
    }

    public String getPaymentMode() {
        return data != null ? (String) data.get("paymentMode") : null;
    }

    public String getTxTime() {
        return data != null ? (String) data.get("txTime") : null;
    }

    public String getSignature() {
        return data != null ? (String) data.get("signature") : null;
    }

    public String getPaymentCurrency() {
        return data != null ? (String) data.get("paymentCurrency") : null;
    }
}