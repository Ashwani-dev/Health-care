package com.ashwani.HealthCare.Utility;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class TimeSlot {
    private LocalTime startTime;
    private LocalTime endTime;

    public TimeSlot(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
