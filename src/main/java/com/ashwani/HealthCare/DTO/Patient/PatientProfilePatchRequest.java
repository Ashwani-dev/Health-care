package com.ashwani.HealthCare.DTO.Patient;

import jakarta.validation.constraints.Size;

public record PatientProfilePatchRequest(
        @Size(max = 500, message = "Profile image URL cannot exceed 500 characters")
        String profileImageUrl
) {}
