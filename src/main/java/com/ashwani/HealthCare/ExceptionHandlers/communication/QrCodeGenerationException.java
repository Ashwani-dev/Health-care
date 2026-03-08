package com.ashwani.HealthCare.ExceptionHandlers.communication;

/**
 * Thrown when QR code generation fails for TOTP setup
 */
public class QrCodeGenerationException extends RuntimeException {
    public QrCodeGenerationException(String message) {
        super(message);
    }

    public QrCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
