package com.ashwani.HealthCare.Service.Doctor;

import com.ashwani.HealthCare.DTO.Doctor.DoctorDto;
import com.ashwani.HealthCare.DTO.Doctor.DoctorProfile;
import com.ashwani.HealthCare.DTO.Doctor.DoctorProfileById;
import com.ashwani.HealthCare.DTO.Doctor.DoctorProfilePatchRequest;
import com.ashwani.HealthCare.DTO.Doctor.DoctorProfileUpdateRequest;
import com.ashwani.HealthCare.DTO.Doctor.DoctorProfileImagePatchResponse;
import com.ashwani.HealthCare.Entity.Doctor;
import com.ashwani.HealthCare.Enums.Gender;
import com.ashwani.HealthCare.ExceptionHandlers.common.DuplicateResourceException;
import com.ashwani.HealthCare.ExceptionHandlers.common.ResourceNotFoundException;
import com.ashwani.HealthCare.Repository.DoctorRepository;
import com.ashwani.HealthCare.Service.AwsS3Service;
import com.ashwani.HealthCare.specifications.DoctorSpecifications;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final ModelMapper modelMapper;
    private final AwsS3Service awsS3Service;

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
        if (updateRequest.profileImageUrl() != null) {
            doctor.setProfileImageUrl(updateRequest.profileImageUrl());
        }

        Doctor updatedDoctor = doctorRepository.save(doctor);
        return modelMapper.map(updatedDoctor, DoctorProfile.class);
    }

    @Transactional
    public DoctorProfileImagePatchResponse patchDoctorProfileImage(Long doctorId, @Valid DoctorProfilePatchRequest patchRequest) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        String profileImageUrl = patchRequest.profileImageUrl();

        // Case 1: Generate presigned URL for new upload (profileImageUrl is null)
        if (profileImageUrl == null) {
            log.info("Generating presigned upload URL for doctor: {}", doctorId);
            AwsS3Service.PresignedUrlResponse presignedResponse = awsS3Service.generateDoctorProfileImagePresignedUrl(doctorId);

            return DoctorProfileImagePatchResponse.builder()
                    .presignedUploadUrl(presignedResponse.getPresignedUploadUrl())
                    .s3ObjectKey(presignedResponse.getS3ObjectKey())
                    .expirationTimeMinutes(presignedResponse.getExpirationTimeMinutes())
                    .build();
        }

        // Case 2: Remove the image (profileImageUrl is "remove")
        if ("remove".equalsIgnoreCase(profileImageUrl.trim())) {
            log.info("Removing profile image for doctor: {}", doctorId);
            doctor.setProfileImageUrl(null);
            doctorRepository.save(doctor);

            return DoctorProfileImagePatchResponse.builder()
                    .presignedUploadUrl(null)
                    .s3ObjectKey(null)
                    .expirationTimeMinutes(null)
                    .build();
        }

        // Case 3: Confirm upload with S3 object key
        String trimmedProfileImageUrl = profileImageUrl.trim();
        if (trimmedProfileImageUrl.isEmpty()) {
            throw new IllegalArgumentException("Profile image URL cannot be blank");
        }

        log.info("Storing S3 object key for doctor: {}, s3Key: {}", doctorId, trimmedProfileImageUrl);
        doctor.setProfileImageUrl(trimmedProfileImageUrl);
        doctorRepository.save(doctor);

        return DoctorProfileImagePatchResponse.builder()
                .presignedUploadUrl(null)
                .s3ObjectKey(trimmedProfileImageUrl)
                .expirationTimeMinutes(null)
                .build();
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
                doctor.getGender(),
                doctor.getProfileImageUrl()
        );
    }
}
