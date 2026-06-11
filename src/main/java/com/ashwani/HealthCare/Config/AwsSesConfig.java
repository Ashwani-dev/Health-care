package com.ashwani.HealthCare.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

/**
 * AWS SES Configuration
 * Configures the SesClient bean using AWS credentials and region
 */
@Configuration
@Slf4j
public class AwsSesConfig {

    @Value("${aws.access-key:}")
    private String accessKey;

    @Value("${aws.secret-key:}")
    private String secretKey;

    @Value("${aws.region:us-east-1}")
    private String region;

    /**
     * Create and configure AWS SES client bean
     * @return configured SesClient
     */
    @Bean
    public SesClient sesClient() {
        validateAwsConfig();

        log.info("✅ Configuring AWS SES Client");
        log.info("   Region: {}", region);
        log.info("   Access Key: {} (length: {}, starts with: {})",
                maskString(accessKey), accessKey.length(),
                accessKey.length() > 3 ? accessKey.substring(0, 3) + "..." : "***");

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        SesClient sesClient = SesClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

        log.info("✅ AWS SES Client initialized successfully");
        return sesClient;
    }

    /**
     * Validate AWS configuration
     */
    private void validateAwsConfig() {
        if (accessKey == null || accessKey.isBlank()) {
            log.error("❌ AWS_ACCESS_KEY_ID is not configured! Please set AWS_ACCESS_KEY_ID environment variable.");
            throw new IllegalStateException("AWS_ACCESS_KEY_ID is required but not configured");
        }

        if (secretKey == null || secretKey.isBlank()) {
            log.error("❌ AWS_SECRET_ACCESS_KEY is not configured! Please set AWS_SECRET_ACCESS_KEY environment variable.");
            throw new IllegalStateException("AWS_SECRET_ACCESS_KEY is required but not configured");
        }

        if (region == null || region.isBlank()) {
            log.warn("⚠️  AWS_REGION not configured, using default: us-east-1");
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
