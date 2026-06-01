package com.ashwani.HealthCare.DTO.Patient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Patient Profile Image Patch request
 * Contains presigned URL for direct S3 upload and S3 object key
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientProfileImagePatchResponse {
    private String presignedUploadUrl;
    private String s3ObjectKey;
    private Integer expirationTimeMinutes;
}

