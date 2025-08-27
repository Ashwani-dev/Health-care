package com.ashwani.HealthCare.Controllers;


import com.ashwani.HealthCare.DTO.Doctor.DoctorDto;
import com.ashwani.HealthCare.DTO.Doctor.DoctorProfile;
import com.ashwani.HealthCare.DTO.Doctor.DoctorProfileUpdateRequest;
import com.ashwani.HealthCare.Entity.DoctorEntity;
import com.ashwani.HealthCare.Repository.DoctorRepository;
import com.ashwani.HealthCare.Service.DoctorService;
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


    @GetMapping("/profile")
    /**
     * Get the authenticated doctor's profile
     * @param principal Current authenticated doctor
     * @return Doctor profile details
     */
    public ResponseEntity<DoctorProfile> getDoctorProfile(Principal principal) {
        // Fetch doctor details from database
        long userId = Long.parseLong(principal.getName());
        DoctorEntity doctor = doctorRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Convert to DTO
        DoctorProfile response = modelMapper.map(doctor, DoctorProfile.class);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    /**
     * Update the authenticated doctor's profile
     * @param updateRequest Profile update payload
     * @param principal Current authenticated doctor
     * @return Updated doctor profile details
     */
    public ResponseEntity<DoctorProfile> updateDoctorProfile(
            @Valid @RequestBody DoctorProfileUpdateRequest updateRequest,
            Principal principal) {

        Long doctorId = Long.parseLong(principal.getName());
        DoctorProfile updatedProfile = doctorService.updateDoctorProfile(doctorId, updateRequest);

        return ResponseEntity.ok(updatedProfile);
    }

    // Single-search-bar endpoint
    @GetMapping("/search")
    /**
     * Search doctors using a single free-text query
     * @param q Optional search string
     * @return List of matching doctors
     */
    public ResponseEntity<List<DoctorDto>> searchDoctorUsingSearchBar(
            @RequestParam(required = false) String q){
        return ResponseEntity.ok(doctorService.searchDoctors(q, null));
    }

    // Multi-field filter endpoint
    @GetMapping("/filter")
    /**
     * Filter doctors by multiple fields
     * @param specialization Optional specialization filter
     * @return List of doctors matching filters
     */
    public List<DoctorDto> searchDoctorUsingFilters(
            @RequestParam(required = false) String specialization
    ) {
        return doctorService.searchDoctors(null, specialization);
    }
}
