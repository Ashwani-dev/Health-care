package com.ashwani.HealthCare.ExceptionHandlers.auth;

public class TotpAlreadyEnabledException extends RuntimeException {
    public TotpAlreadyEnabledException(String message) {
        super(message);
    }
}
