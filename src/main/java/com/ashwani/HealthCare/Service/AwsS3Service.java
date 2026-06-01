package com.ashwani.HealthCare.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AWS S3 Service
 * Handles presigned URL generation for direct S3 uploads
 */
@Service
@Slf4j
public class AwsS3Service {

    private final S3Presigner s3Presigner;
    private final String s3BucketName;
    private static final Duration PRESIGNED_URL_DURATION = Duration.ofMinutes(15);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public AwsS3Service(S3Presigner s3Presigner, String s3BucketName) {
        this.s3Presigner = s3Presigner;
        this.s3BucketName = s3BucketName;
    }

    /**
     * Generate presigned upload URL for doctor profile image
     * @param doctorId doctor ID
     * @return PresignedUrlResponse containing presigned URL and S3 object key
     */
    public PresignedUrlResponse generateDoctorProfileImagePresignedUrl(Long doctorId) {
        String s3ObjectKey = generateS3ObjectKey("doctor", doctorId, "avatar.jpg");
        return generatePresignedUploadUrl(s3ObjectKey);
    }

    /**
     * Generate presigned upload URL for patient profile image
     * @param patientId patient ID
     * @return PresignedUrlResponse containing presigned URL and S3 object key
     */
    public PresignedUrlResponse generatePatientProfileImagePresignedUrl(Long patientId) {
        String s3ObjectKey = generateS3ObjectKey("patient", patientId, "avatar.jpg");
        return generatePresignedUploadUrl(s3ObjectKey);
    }

    /**
     * Generate S3 object key (path) for profile images
     * Format: profile-images/{type}/{userId}/{timestamp}-{filename}
     * @param type "doctor" or "patient"
     * @param userId user ID
     * @param filename original filename
     * @return S3 object key
     */
    public String generateS3ObjectKey(String type, Long userId, String filename) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String s3ObjectKey = String.format("profile-images/%s/%d/%s-%s", type, userId, timestamp, filename);
        log.debug("Generated S3 object key: {}", s3ObjectKey);
        return s3ObjectKey;
    }

    /**
     * Generate presigned upload URL for given S3 object key
     * @param s3ObjectKey S3 object key (path)
     * @return PresignedUrlResponse containing presigned URL and S3 object key
     */
    private PresignedUrlResponse generatePresignedUploadUrl(String s3ObjectKey) {
        try {
            // Create PutObject request
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(s3ObjectKey)
                    .build();

            // Create presigned request with expiration
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(PRESIGNED_URL_DURATION)
                    .putObjectRequest(putObjectRequest)
                    .build();

            // Generate presigned URL
            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            log.info("✅ Generated presigned upload URL for S3 object: {}", s3ObjectKey);
            log.debug("Presigned URL: {} (will expire in {} minutes)", presignedUrl, PRESIGNED_URL_DURATION.toMinutes());

            return PresignedUrlResponse.builder()
                    .presignedUploadUrl(presignedUrl)
                    .s3ObjectKey(s3ObjectKey)
                    .expirationTimeMinutes(15)
                    .build();

        } catch (Exception e) {
            log.error("❌ Failed to generate presigned URL for S3 object: {}", s3ObjectKey, e);
            throw new RuntimeException("Failed to generate presigned upload URL", e);
        }
    }

    /**
     * PresignedUrlResponse DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class PresignedUrlResponse {
        private String presignedUploadUrl;
        private String s3ObjectKey;
        private Integer expirationTimeMinutes;
    }
}

