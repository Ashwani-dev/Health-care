package com.ashwani.HealthCare.DTO;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoctorDto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 20, message = "Full name must be between 3 and 20 characters")
    private String full_name;

    @Column(nullable = false)
    @NotBlank(message = "Area of specialization is required")
    private String specialization;

    @Column(nullable = false)
    @NotNull(message = "Experience is required")
    private Integer medical_experience;
//    private String profilePictureUrl;
}
