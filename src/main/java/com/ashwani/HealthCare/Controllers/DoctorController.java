package com.ashwani.HealthCare.Controllers;


import com.ashwani.HealthCare.DTO.DoctorDto;
import com.ashwani.HealthCare.DTO.DoctorProfileDto;
import com.ashwani.HealthCare.DTO.PatientProfileDto;
import com.ashwani.HealthCare.Entity.DoctorEntity;
import com.ashwani.HealthCare.Entity.PatientEntity;
import com.ashwani.HealthCare.Repository.DoctorRepository;
import com.ashwani.HealthCare.Service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {
    @Autowired
    private final DoctorService doctorService;
    private final DoctorRepository doctorRepository;
    private final ModelMapper modelMapper;


    @GetMapping("/profile")
    public ResponseEntity<DoctorProfileDto> getDoctorProfile(Principal principal) {
        // Fetch doctor details from database
        DoctorEntity doctor = doctorRepository.findById(Long.parseLong(principal.getName()))
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Convert to DTO
        DoctorProfileDto response = modelMapper.map(doctor, DoctorProfileDto.class);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<DoctorDto>> searchDoctor(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String name){
        return ResponseEntity.ok(doctorService.searchDoctors(specialization, name));
    }
}
