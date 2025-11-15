package com.ashwani.HealthCare.DTO.Doctor;

import com.ashwani.HealthCare.Enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfile {
    private Long id;
    private String full_name;
    private String username;
    private String email;
    private String contact_number;
    private int medical_experience;
    private String specialization;
    private Gender gender;
    private String license_number;
}
