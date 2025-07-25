package com.ashwani.HealthCare.DTO.Patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PatientProfileUpdateRequest(
        @NotBlank(message = "Full name cannot be blank")
        @Size(max = 100, message = "Full name cannot exceed 100 characters")
        String full_name,

        @Size(max = 200, message = "Address cannot exceed 200 characters")
        String address
) {}
