package com.ashwani.HealthCare.DTO.Appointments;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class BookAppointmentRequest {
    private Long patientId;
    private Long doctorId;
    private LocalDate date;
    private LocalTime startTime;
    private String reason;
}
