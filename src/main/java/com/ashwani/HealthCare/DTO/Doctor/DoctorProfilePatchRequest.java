package com.ashwani.HealthCare.DTO.Doctor;

import jakarta.validation.constraints.Size;

public record DoctorProfilePatchRequest(
        @Size(max = 500, message = "Profile image URL cannot exceed 500 characters")
        String profileImageUrl
) {}
