package com.ashwani.HealthCare.DTO;

import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityRequestDto {
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isAvailable = true;
}
