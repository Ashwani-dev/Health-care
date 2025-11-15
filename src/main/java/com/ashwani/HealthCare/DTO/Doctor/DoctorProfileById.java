package com.ashwani.HealthCare.DTO.Doctor;

import com.ashwani.HealthCare.Enums.Gender;

/**
 * Read-only DTO for fetching doctor profile by ID
 * Contains public profile information
 */
public record DoctorProfileById(
        String email,
        String full_name,
        String contact_number,
        String specialization,
        Integer medical_experience,
        Gender gender
) {
}
