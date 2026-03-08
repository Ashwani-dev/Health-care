package com.ashwani.HealthCare.ExceptionHandlers.auth;

public class InvalidTotpCodeException extends RuntimeException {
    public InvalidTotpCodeException(String message) {
        super(message);
    }
}
