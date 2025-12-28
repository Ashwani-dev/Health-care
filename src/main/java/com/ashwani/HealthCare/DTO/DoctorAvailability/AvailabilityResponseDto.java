package com.ashwani.HealthCare.DTO.DoctorAvailability;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponseDto {
    private Long id;
    private Long doctorId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
