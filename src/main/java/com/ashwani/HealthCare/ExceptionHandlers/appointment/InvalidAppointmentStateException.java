package com.ashwani.HealthCare.ExceptionHandlers.appointment;

/**
 * Thrown when an operation is attempted on an appointment in an invalid state
 * Examples: updating a completed appointment, joining a cancelled call
 */
public class InvalidAppointmentStateException extends RuntimeException {
    private final String currentState;
    private final String attemptedOperation;

    public InvalidAppointmentStateException(String message, String currentState, String attemptedOperation) {
        super(message);
        this.currentState = currentState;
        this.attemptedOperation = attemptedOperation;
    }

    public InvalidAppointmentStateException(String message) {
        super(message);
        this.currentState = null;
        this.attemptedOperation = null;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getAttemptedOperation() {
        return attemptedOperation;
    }
}
