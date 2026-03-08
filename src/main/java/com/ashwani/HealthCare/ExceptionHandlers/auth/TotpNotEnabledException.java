package com.ashwani.HealthCare.ExceptionHandlers.auth;

public class TotpNotEnabledException extends RuntimeException {
    public TotpNotEnabledException(String message) {
        super(message);
    }
}
