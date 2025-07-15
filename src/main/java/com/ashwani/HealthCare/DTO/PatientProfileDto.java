package com.ashwani.HealthCare.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientProfileDto {
    private Long id;
    private String full_name;
    private String username;
    private String email;
    private String contact_number;
    private String address;
}
