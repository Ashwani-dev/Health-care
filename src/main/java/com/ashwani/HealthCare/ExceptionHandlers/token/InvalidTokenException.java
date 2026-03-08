package com.ashwani.HealthCare.ExceptionHandlers.token;

/**
 * Thrown when a provided token is invalid or malformed
 */
public class InvalidTokenException extends RuntimeException {
    private final String tokenType;

    public InvalidTokenException(String message, String tokenType) {
        super(message);
        this.tokenType = tokenType;
    }

    public InvalidTokenException(String message) {
        super(message);
        this.tokenType = null;
    }

    public String getTokenType() {
        return tokenType;
    }
}
