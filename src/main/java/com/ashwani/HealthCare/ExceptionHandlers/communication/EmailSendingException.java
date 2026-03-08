package com.ashwani.HealthCare.ExceptionHandlers.communication;

/**
 * Thrown when email sending operations fail
 */
public class EmailSendingException extends RuntimeException {
    private final String recipient;

    public EmailSendingException(String message, String recipient) {
        super(message);
        this.recipient = recipient;
    }

    public EmailSendingException(String message, String recipient, Throwable cause) {
        super(message, cause);
        this.recipient = recipient;
    }

    public String getRecipient() {
        return recipient;
    }
}
