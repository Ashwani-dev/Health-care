package com.ashwani.HealthCare.ExceptionHandlers.token;

/**
 * Thrown when a token (password reset, appointment hold, etc.) has expired
 */
public class TokenExpiredException extends RuntimeException {
    private final String tokenType;

    public TokenExpiredException(String message, String tokenType) {
        super(message);
        this.tokenType = tokenType;
    }

    public TokenExpiredException(String message) {
        super(message);
        this.tokenType = null;
    }

    public String getTokenType() {
        return tokenType;
    }
}
