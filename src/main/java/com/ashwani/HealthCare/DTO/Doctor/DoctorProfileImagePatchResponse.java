package com.ashwani.HealthCare.DTO.Doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Doctor Profile Image Patch request
 * Contains presigned URL for direct S3 upload and S3 object key
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileImagePatchResponse {
    private String presignedUploadUrl;
    private String s3ObjectKey;
    private Integer expirationTimeMinutes;
}

