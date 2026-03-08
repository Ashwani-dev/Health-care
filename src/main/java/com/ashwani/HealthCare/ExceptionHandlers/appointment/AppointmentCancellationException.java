package com.ashwani.HealthCare.ExceptionHandlers.appointment;

/**
 * Thrown when appointment cancellation fails or violates business rules
 * Examples: 24-hour cancellation policy violation, already cancelled appointment
 */
public class AppointmentCancellationException extends RuntimeException {
    public AppointmentCancellationException(String message) {
        super(message);
    }
}
