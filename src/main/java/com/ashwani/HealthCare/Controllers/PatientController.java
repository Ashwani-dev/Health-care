package com.ashwani.HealthCare.Controllers;


import com.ashwani.HealthCare.DTO.PatientProfileDto;
import com.ashwani.HealthCare.Entity.PatientEntity;
import com.ashwani.HealthCare.Repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientRepository patientRepository;
    private final ModelMapper modelMapper;

    @GetMapping("/profile")
    public ResponseEntity<PatientProfileDto> getPatientProfile(Principal principal) {
        // Fetch patient details from database
        PatientEntity patient = patientRepository.findById(Long.parseLong(principal.getName()))
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Convert to DTO
        PatientProfileDto response = modelMapper.map(patient, PatientProfileDto.class);

        return ResponseEntity.ok(response);
    }

}
