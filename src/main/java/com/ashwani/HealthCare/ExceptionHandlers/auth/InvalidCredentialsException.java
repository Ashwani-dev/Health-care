package com.ashwani.HealthCare.ExceptionHandlers.auth;

/**
 * Thrown when login credentials (email/password) are incorrect
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
