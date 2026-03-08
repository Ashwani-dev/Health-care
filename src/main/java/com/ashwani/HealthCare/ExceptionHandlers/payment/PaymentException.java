package com.ashwani.HealthCare.ExceptionHandlers.payment;

/**
 * Thrown when payment operations fail
 * Examples: payment gateway errors, webhook validation failures, incomplete payments
 */
public class PaymentException extends RuntimeException {
    private final String orderId;
    private final String errorCode;

    public PaymentException(String message, String orderId, String errorCode) {
        super(message);
        this.orderId = orderId;
        this.errorCode = errorCode;
    }

    public PaymentException(String message) {
        super(message);
        this.orderId = null;
        this.errorCode = null;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
