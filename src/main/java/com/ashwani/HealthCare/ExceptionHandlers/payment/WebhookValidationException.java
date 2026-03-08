package com.ashwani.HealthCare.ExceptionHandlers.payment;

/**
 * Thrown when webhook signature validation fails
 */
public class WebhookValidationException extends RuntimeException {
    private final String orderId;

    public WebhookValidationException(String message, String orderId) {
        super(message);
        this.orderId = orderId;
    }

    public WebhookValidationException(String message) {
        super(message);
        this.orderId = null;
    }

    public String getOrderId() {
        return orderId;
    }
}
