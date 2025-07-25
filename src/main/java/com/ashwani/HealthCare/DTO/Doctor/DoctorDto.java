package com.ashwani.HealthCare.DTO.Doctor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoctorDto {
    private Long id;
    private String full_name;
    private String specialization;
    private Integer medical_experience;
//    private String profilePictureUrl;
}
