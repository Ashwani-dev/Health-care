package com.ashwani.HealthCare.DTO.Doctor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DoctorProfileUpdateRequest(
        @NotBlank(message = "Full name cannot be blank")
        @Size(max = 100, message = "Full name cannot exceed 100 characters")
        String full_name,

        @Min(value = 0, message = "Experience cannot be negative")
        @Max(value = 50, message = "Experience cannot exceed 50 years")
        Integer medical_experience
) {}
