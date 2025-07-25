package com.ashwani.HealthCare.Service;

import com.ashwani.HealthCare.DTO.Doctor.DoctorDto;
import com.ashwani.HealthCare.DTO.Doctor.DoctorProfile;
import com.ashwani.HealthCare.DTO.Doctor.DoctorProfileUpdateRequest;
import com.ashwani.HealthCare.Entity.DoctorEntity;
import com.ashwani.HealthCare.Repository.DoctorRepository;
import com.ashwani.HealthCare.specifications.DoctorSpecifications;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final ModelMapper modelMapper;

    private DoctorDto convertToDto(DoctorEntity doctor) {
        return modelMapper.map(doctor, DoctorDto.class);
    }

    private Specification<DoctorEntity> buildSearchSpecification(String query) {
        return Specification.where(DoctorSpecifications.hasSpecialization(query))
                .or(DoctorSpecifications.nameContains(query));
    }

    private Specification<DoctorEntity> buildFilterSpecification(String specialization) {
        return Specification.where(DoctorSpecifications.hasSpecialization(specialization));
    }

    @Transactional(readOnly = true)
    public List<DoctorDto> searchDoctors(@Nullable String searchQuery,
                                         @Nullable String specialization){
        Specification<DoctorEntity> spec;
        if(searchQuery != null){
            spec = buildSearchSpecification(searchQuery);
        }
        else{
            spec = buildFilterSpecification(specialization);
        }

        return doctorRepository.findAll(spec)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public DoctorProfile updateDoctorProfile(Long doctorId, @Valid DoctorProfileUpdateRequest updateRequest) {
        DoctorEntity doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Doctor not found with ID: " + doctorId
                ));

        // Update only allowed fields
        doctor.setFull_name(updateRequest.full_name());
        doctor.setMedical_experience(updateRequest.medical_experience());

        DoctorEntity updatedDoctor = doctorRepository.save(doctor);
        return modelMapper.map(updatedDoctor, DoctorProfile.class);
    }
}
