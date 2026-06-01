package com.ashwani.HealthCare.Controllers;


import com.ashwani.HealthCare.DTO.Doctor.DoctorDto;
import com.ashwani.HealthCare.DTO.Doctor.DoctorProfile;
import com.ashwani.HealthCare.DTO.Doctor.DoctorProfileById;
import com.ashwani.HealthCare.DTO.Doctor.DoctorProfilePatchRequest;
import com.ashwani.HealthCare.DTO.Doctor.DoctorProfileUpdateRequest;
import com.ashwani.HealthCare.DTO.Doctor.DoctorProfileImagePatchResponse;
import com.ashwani.HealthCare.Entity.Doctor;
import com.ashwani.HealthCare.Enums.Gender;
import com.ashwani.HealthCare.Repository.DoctorRepository;
import com.ashwani.HealthCare.Service.Doctor.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


    // Get the authenticated doctor's profile
    @GetMapping("/profile")
    public ResponseEntity<DoctorProfile> getDoctorProfile(Principal principal) {
        // Fetch doctor details from database
        long userId = Long.parseLong(principal.getName());
        Doctor doctor = doctorRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Convert to DTO
        DoctorProfile response = modelMapper.map(doctor, DoctorProfile.class);

        return ResponseEntity.ok(response);
    }

    // Update the authenticated doctor's profile
    @PutMapping("/profile")
    public ResponseEntity<DoctorProfile> updateDoctorProfile(
            @Valid @RequestBody DoctorProfileUpdateRequest updateRequest,
            Principal principal) {

        Long doctorId = Long.parseLong(principal.getName());
        DoctorProfile updatedProfile = doctorService.updateDoctorProfile(doctorId, updateRequest);

        return ResponseEntity.ok(updatedProfile);
    }

    // Patch the authenticated doctor's profile image URL
    @PatchMapping("/profile")
    public ResponseEntity<DoctorProfileImagePatchResponse> patchDoctorProfileImage(
            @Valid @RequestBody DoctorProfilePatchRequest patchRequest,
            Principal principal) {

        Long doctorId = Long.parseLong(principal.getName());
        DoctorProfileImagePatchResponse response = doctorService.patchDoctorProfileImage(doctorId, patchRequest);

        return ResponseEntity.ok(response);
    }

    // Single-search-bar endpoint
    // Search doctors using a single free-text query
    @GetMapping("/search")
    public ResponseEntity<List<DoctorDto>> searchDoctorUsingSearchBar(
            @RequestParam(required = false) String q){
        return ResponseEntity.ok(doctorService.searchDoctors(q, null, null));
    }

    // Multi-field filter endpoint
    // Filter doctors by multiple fields
    @GetMapping("/filter")
    public List<DoctorDto> searchDoctorUsingFilters(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) Gender gender
    ) {
        return doctorService.searchDoctors(null, specialization, gender);
    }

    // Get doctor profile by ID (public read-only information)
    @GetMapping("/{id}")
    public ResponseEntity<DoctorProfileById> getDoctorProfileById(@PathVariable Long id) {
        DoctorProfileById profile = doctorService.getDoctorProfileById(id);
        return ResponseEntity.ok(profile);
    }
}
