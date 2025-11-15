package com.ashwani.HealthCare.DTO.Doctor;

import com.ashwani.HealthCare.Enums.Gender;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoctorDto {
    private Long id;
    private String full_name;
    private String specialization;
    private Gender gender;
    private Integer medical_experience;
//    private String profilePictureUrl;
}
