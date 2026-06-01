package com.ashwani.HealthCare.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS S3 Configuration
 * Loads AWS credentials from environment variables and configures S3 client with presigner
 */
@Configuration
@Slf4j
public class AwsS3Config {

    @Value("${aws.s3.access-key:}")
    private String accessKey;

    @Value("${aws.s3.secret-key:}")
    private String secretKey;

    @Value("${aws.s3.bucket-name:}")
    private String bucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    /**
     * Create and configure S3 client bean
     * @return configured S3Client
     */
    @Bean
    public S3Client s3Client() {
        validateAwsConfig();

        log.info("✅ Configuring AWS S3 Client");
        log.info("   Region: {}", region);
        log.info("   Bucket: {}", bucketName);
        log.info("   Access Key: {} (length: {}, starts with: {})",
                maskString(accessKey), accessKey.length(),
                accessKey.length() > 3 ? accessKey.substring(0, 3) + "..." : "***");

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        S3Client s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

        log.info("✅ AWS S3 Client initialized successfully");
        return s3Client;
    }

    /**
     * Create S3Presigner bean for generating presigned URLs
     * @param s3Client the S3 client
     * @return configured S3Presigner
     */
    @Bean
    public S3Presigner s3Presigner(S3Client s3Client) {
        log.info("✅ Configuring AWS S3 Presigner for presigned URL generation");

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

        log.info("✅ AWS S3 Presigner initialized successfully");
        return presigner;
    }

    /**
     * Get S3 bucket name
     * @return bucket name
     */
    @Bean
    public String s3BucketName() {
        return bucketName;
    }

    /**
     * Validate AWS configuration
     */
    private void validateAwsConfig() {
        if (accessKey == null || accessKey.isBlank()) {
            log.error("❌ AWS_S3_ACCESS_KEY is not configured! Please set AWS_S3_ACCESS_KEY environment variable.");
            throw new IllegalStateException("AWS_S3_ACCESS_KEY is required but not configured");
        }

        if (secretKey == null || secretKey.isBlank()) {
            log.error("❌ AWS_S3_SECRET_KEY is not configured! Please set AWS_S3_SECRET_KEY environment variable.");
            throw new IllegalStateException("AWS_S3_SECRET_KEY is required but not configured");
        }

        if (bucketName == null || bucketName.isBlank()) {
            log.error("❌ AWS_S3_BUCKET_NAME is not configured! Please set AWS_S3_BUCKET_NAME environment variable.");
            throw new IllegalStateException("AWS_S3_BUCKET_NAME is required but not configured");
        }

        if (region == null || region.isBlank()) {
            log.warn("⚠️  AWS_S3_REGION not configured, using default: us-east-1");
        }
    }

    /**
     * Mask sensitive string for logging
     * @param str string to mask
     * @return masked string
     */
    private String maskString(String str) {
        if (str == null || str.length() < 8) {
            return "***";
        }
        return str.substring(0, 8) + "***";
    }
}

