package com.ashwani.HealthCare.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileDto {
    private Long id;
    private String full_name;
    private String username;
    private String email;
    private String contact_number;
    private int medical_experience;
    private String specialization;
}
