package com.ashwani.HealthCare.Service.Payment.Gateway;

import com.ashwani.HealthCare.DTO.Payment.PaymentRequest;
import com.ashwani.HealthCare.DTO.Payment.PaymentResponse;
import com.ashwani.HealthCare.DTO.Payment.PaymentWebhookPayload;
import com.cashfree.pg.ApiException;

import java.util.Map;

/**
 * Payment Gateway Interface
 * Defines the contract for all payment gateway implementations
 */
public interface PaymentGateway {

    /**
     * Initiate a payment order
     * @param paymentRequest Payment request details
     * @return PaymentResponse with orderId and paymentSessionId
     * @throws ApiException if payment initiation fails
     */
    PaymentResponse initiatePayment(PaymentRequest paymentRequest) throws ApiException;

    /**
     * Validate webhook signature
     * @param payload Webhook payload
     * @param signature Signature from webhook header
     * @param rawBody Raw webhook body
     * @return true if signature is valid, false otherwise
     */
    boolean validateWebhookSignature(PaymentWebhookPayload payload, String signature, String rawBody);

    /**
     * Check if webhook is a test webhook
     * @param payload Webhook payload
     * @return true if test webhook, false otherwise
     */
    boolean isTestWebhook(PaymentWebhookPayload payload);

    /**
     * Get payment gateway configuration status for debugging
     * @return Configuration status map
     */
    Map<String, Object> getConfigStatus();

    /**
     * Get the payment gateway name
     * @return Gateway name (e.g., "Cashfree", "Paytm")
     */
    String getGatewayName();
}
