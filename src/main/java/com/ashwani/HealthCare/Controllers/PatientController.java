package com.ashwani.HealthCare.Controllers;


import com.ashwani.HealthCare.DTO.Patient.PatientProfile;
import com.ashwani.HealthCare.DTO.Patient.PatientProfilePatchRequest;
import com.ashwani.HealthCare.DTO.Patient.PatientProfileUpdateRequest;
import com.ashwani.HealthCare.DTO.Patient.PatientProfileImagePatchResponse;
import com.ashwani.HealthCare.Entity.Patient;
import com.ashwani.HealthCare.Repository.PatientRepository;
import com.ashwani.HealthCare.Service.Patient.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientRepository patientRepository;
    private final PatientService patientService;
    private final ModelMapper modelMapper;

    // Get the authenticated patient's profile
    @GetMapping("/profile")
    public ResponseEntity<PatientProfile> getPatientProfile(Principal principal) {
        // Fetch patient details from database
        Long userId = Long.parseLong(principal.getName());
        Patient patient = patientRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Convert to DTO
        PatientProfile response = modelMapper.map(patient, PatientProfile.class);

        return ResponseEntity.ok(response);
    }

    // Update the authenticated patient's profile
    @PutMapping("/profile")
    public ResponseEntity<PatientProfile> updatePatientProfile(
            @Valid @RequestBody PatientProfileUpdateRequest updateRequest,
            Principal principal) {

        Long patientId = Long.parseLong(principal.getName());
        PatientProfile updatedProfile = patientService.updatePatientProfile(patientId, updateRequest);

        return ResponseEntity.ok(updatedProfile);
    }

    // Patch the authenticated patient's profile image URL
    @PatchMapping("/profile")
    public ResponseEntity<PatientProfileImagePatchResponse> patchPatientProfileImage(
            @Valid @RequestBody PatientProfilePatchRequest patchRequest,
            Principal principal) {

        Long patientId = Long.parseLong(principal.getName());
        PatientProfileImagePatchResponse response = patientService.patchPatientProfileImage(patientId, patchRequest);

        return ResponseEntity.ok(response);
    }


}
