package com.ashwani.HealthCare.ExceptionHandlers.appointment;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Thrown when a requested appointment slot is not available
 */
public class SlotNotAvailableException extends RuntimeException {
    private final LocalDate date;
    private final LocalTime time;

    public SlotNotAvailableException(String message, LocalDate date, LocalTime time) {
        super(message);
        this.date = date;
        this.time = time;
    }

    public SlotNotAvailableException(String message) {
        super(message);
        this.date = null;
        this.time = null;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }
}
