package com.ashwani.HealthCare.ExceptionHandlers.token;

/**
 * Thrown when attempting to use a token that has already been used
 * Example: password reset token used twice
 */
public class TokenAlreadyUsedException extends RuntimeException {
    private final String tokenType;

    public TokenAlreadyUsedException(String message, String tokenType) {
        super(message);
        this.tokenType = tokenType;
    }

    public TokenAlreadyUsedException(String message) {
        super(message);
        this.tokenType = null;
    }

    public String getTokenType() {
        return tokenType;
    }
}
