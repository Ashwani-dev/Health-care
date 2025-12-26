package com.ashwani.HealthCare.DTO.Doctor;

import com.ashwani.HealthCare.Enums.Gender;
import jakarta.validation.constraints.*;

public record DoctorProfileUpdateRequest(
        @NotBlank(message = "Full name cannot be blank")
        @Size(max = 100, message = "Full name cannot exceed 100 characters")
        String full_name,

        @Min(value = 0, message = "Experience cannot be negative")
        @Max(value = 50, message = "Experience cannot exceed 50 years")
        Integer medical_experience,

        @NotBlank(message = "License number is required")
        @Size(min = 5, max = 50, message = "License number must be between 5 and 50 characters")
        @Pattern(regexp = "^[A-Za-z0-9\\-]+$", message = "License number must be alphanumeric with optional hyphens")
        String license_number
) {}
