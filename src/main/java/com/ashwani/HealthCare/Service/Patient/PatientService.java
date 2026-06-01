package com.ashwani.HealthCare.Service.Patient;

import com.ashwani.HealthCare.DTO.Patient.PatientProfile;
import com.ashwani.HealthCare.DTO.Patient.PatientProfilePatchRequest;
import com.ashwani.HealthCare.DTO.Patient.PatientProfileUpdateRequest;
import com.ashwani.HealthCare.DTO.Patient.PatientProfileImagePatchResponse;
import com.ashwani.HealthCare.Entity.Patient;
import com.ashwani.HealthCare.ExceptionHandlers.common.ResourceNotFoundException;
import com.ashwani.HealthCare.Repository.PatientRepository;
import com.ashwani.HealthCare.Service.AwsS3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;
    private final ModelMapper modelMapper;
    private final AwsS3Service awsS3Service;

    public PatientService(PatientRepository patientRepository, ModelMapper modelMapper, AwsS3Service awsS3Service) {
        this.patientRepository = patientRepository;
        this.modelMapper = modelMapper;
        this.awsS3Service = awsS3Service;
    }

    public PatientProfile updatePatientProfile(Long patientId, @Valid PatientProfileUpdateRequest updateRequest) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        // Update only allowed fields
        patient.setFull_name(updateRequest.full_name());
        patient.setAddress(updateRequest.address());
        if (updateRequest.profileImageUrl() != null) {
            patient.setProfileImageUrl(updateRequest.profileImageUrl());
        }

        Patient updatedPatient = patientRepository.save(patient);
        return modelMapper.map(updatedPatient, PatientProfile.class);
    }

    @Transactional
    public PatientProfileImagePatchResponse patchPatientProfileImage(Long patientId, @Valid PatientProfilePatchRequest patchRequest) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        String profileImageUrl = patchRequest.profileImageUrl();

        // Case 1: Generate presigned URL for new upload (profileImageUrl is null)
        if (profileImageUrl == null) {
            log.info("Generating presigned upload URL for patient: {}", patientId);
            AwsS3Service.PresignedUrlResponse presignedResponse = awsS3Service.generatePatientProfileImagePresignedUrl(patientId);

            return PatientProfileImagePatchResponse.builder()
                    .presignedUploadUrl(presignedResponse.getPresignedUploadUrl())
                    .s3ObjectKey(presignedResponse.getS3ObjectKey())
                    .expirationTimeMinutes(presignedResponse.getExpirationTimeMinutes())
                    .build();
        }

        // Case 2: Remove the image (profileImageUrl is "remove")
        if ("remove".equalsIgnoreCase(profileImageUrl.trim())) {
            log.info("Removing profile image for patient: {}", patientId);
            patient.setProfileImageUrl(null);
            patientRepository.save(patient);

            return PatientProfileImagePatchResponse.builder()
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

        log.info("Storing S3 object key for patient: {}, s3Key: {}", patientId, trimmedProfileImageUrl);
        patient.setProfileImageUrl(trimmedProfileImageUrl);
        patientRepository.save(patient);

        return PatientProfileImagePatchResponse.builder()
                .presignedUploadUrl(null)
                .s3ObjectKey(trimmedProfileImageUrl)
                .expirationTimeMinutes(null)
                .build();
    }
}
