package com.ashwani.HealthCare.ExceptionHandlers.auth;

public class LoginMethodMismatchException extends RuntimeException {
    public LoginMethodMismatchException(String message) {
        super(message);
    }
}
