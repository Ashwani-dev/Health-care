package com.ashwani.HealthCare.Service.Doctor;

import com.ashwani.HealthCare.DTO.Doctor.DoctorDto;
import com.ashwani.HealthCare.DTO.Doctor.DoctorProfile;
import com.ashwani.HealthCare.DTO.Doctor.DoctorProfileById;
import com.ashwani.HealthCare.DTO.Doctor.DoctorProfileUpdateRequest;
import com.ashwani.HealthCare.Entity.Doctor;
import com.ashwani.HealthCare.Enums.Gender;
import com.ashwani.HealthCare.ExceptionHandlers.common.DuplicateResourceException;
import com.ashwani.HealthCare.ExceptionHandlers.common.ResourceNotFoundException;
import com.ashwani.HealthCare.Repository.DoctorRepository;
import com.ashwani.HealthCare.specifications.DoctorSpecifications;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final ModelMapper modelMapper;

    private DoctorDto convertToDto(Doctor doctor) {
        return modelMapper.map(doctor, DoctorDto.class);
    }

    private Specification<Doctor> buildSearchSpecification(String query) {
        return Specification.where(DoctorSpecifications.hasSpecialization(query))
                .or(DoctorSpecifications.nameContains(query));
    }

    private Specification<Doctor> buildFilterSpecification(String specialization, Gender gender) {
        return Specification.where(DoctorSpecifications.hasSpecialization(specialization))
                .and(DoctorSpecifications.hasGender(gender));
    }

    @Transactional(readOnly = true)
    public List<DoctorDto> searchDoctors(@Nullable String searchQuery,
                                         @Nullable String specialization,
                                         @Nullable Gender gender){
        Specification<Doctor> spec;
        if(searchQuery != null){
            spec = buildSearchSpecification(searchQuery);
        }
        else{
            spec = buildFilterSpecification(specialization, gender);
        }

        return doctorRepository.findAll(spec)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public DoctorProfile updateDoctorProfile(Long doctorId, @Valid DoctorProfileUpdateRequest updateRequest) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        // Handle license number validation only if it's being provided in the update
        if (updateRequest.license_number() != null && !updateRequest.license_number().trim().isEmpty()) {
            // Check if license number is being changed and if it's unique
            if (doctor.getLicense_number() == null ||
                    !doctor.getLicense_number().equals(updateRequest.license_number())) {

                doctorRepository.findByLicenseNumber(updateRequest.license_number())
                        .ifPresent(existingDoctor -> {
                            if (!existingDoctor.getId().equals(doctorId)) {
                                throw new DuplicateResourceException("Doctor", "license_number");
                            }
                        });
            }
        }

        // Update allowed fields
        doctor.setFull_name(updateRequest.full_name());
        doctor.setMedical_experience(updateRequest.medical_experience());
        doctor.setLicense_number(updateRequest.license_number());

        Doctor updatedDoctor = doctorRepository.save(doctor);
        return modelMapper.map(updatedDoctor, DoctorProfile.class);
    }

    @Transactional(readOnly = true)
    public DoctorProfileById getDoctorProfileById(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        return new DoctorProfileById(
                doctor.getEmail(),
                doctor.getFull_name(),
                doctor.getContact_number(),
                doctor.getSpecialization(),
                doctor.getMedical_experience(),
                doctor.getGender()
        );
    }
}
