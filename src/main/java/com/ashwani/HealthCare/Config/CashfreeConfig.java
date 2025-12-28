package com.ashwani.HealthCare.Config;

import com.cashfree.pg.Cashfree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CashfreeConfig {

    @Value("${cashfree.appId}")
    private String appId;

    @Value("${cashfree.secretKey}")
    private String secretKey;

    // Optional environment variable if needed (e.g. "TEST" or "PROD")
    @Value("${cashfree.env}")
    private String environment;

    @Bean
    public Cashfree cashfreeSDK() {
        // Validate configuration
        if (appId == null || appId.trim().isEmpty()) {
            log.error("❌ Cashfree APP_ID is not configured! Please set cashfree.appId or APP_ID environment variable in Render.");
            throw new IllegalStateException("Cashfree APP_ID is required but not configured");
        }
        
        if (secretKey == null || secretKey.trim().isEmpty()) {
            log.error("❌ Cashfree SECRET_KEY is not configured! Please set cashfree.secretKey or SECRET_KEY environment variable in Render.");
            throw new IllegalStateException("Cashfree SECRET_KEY is required but not configured");
        }

        // Convert String environment to CFEnvironment enum
        Cashfree.CFEnvironment envEnum;
        try {
            envEnum = Cashfree.CFEnvironment.valueOf(environment.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid Cashfree environment: {}. Must be SANDBOX or PRODUCTION. Defaulting to SANDBOX.", environment);
            envEnum = Cashfree.CFEnvironment.SANDBOX;
        }

        // Trim whitespace (common issue with environment variables)
        appId = appId.trim();
        secretKey = secretKey.trim();
        
        // Log configuration details (masked for security)
        String maskedAppId = appId.length() > 8 ? appId.substring(0, 8) + "***" : "***";
        String maskedSecretKey = secretKey.length() > 8 ? secretKey.substring(0, 8) + "***" : "***";
        
        log.info("✅ Cashfree SDK Configuration:");
        log.info("   Environment: {}", envEnum);
        log.info("   AppId: {} (length: {}, starts with: {})", 
                maskedAppId, appId.length(), 
                appId.length() > 3 ? appId.substring(0, 3) + "..." : "***");
        log.info("   SecretKey: {} (length: {}, starts with: {})", 
                maskedSecretKey, secretKey.length(),
                secretKey.length() > 3 ? secretKey.substring(0, 3) + "..." : "***");
        log.info("   Environment Variable: cashfree.env = {}", environment);
        
        // Additional validation: Check if credentials look valid
        if (appId.length() < 10) {
            log.warn("⚠️  APP_ID seems too short ({} chars). Cashfree App IDs are typically longer. Please verify.", appId.length());
        }
        if (secretKey.length() < 20) {
            log.warn("⚠️  SECRET_KEY seems too short ({} chars). Cashfree Secret Keys are typically longer. Please verify.", secretKey.length());
        }
        
        // Check for common issues
        if (appId.contains("${") || appId.contains("APP_ID")) {
            log.error("❌ APP_ID appears to be unresolved! Value contains: {}. Check if APP_ID environment variable is set.", appId);
        }
        if (secretKey.contains("${") || secretKey.contains("SECRET_KEY")) {
            log.error("❌ SECRET_KEY appears to be unresolved! Value contains: {}. Check if SECRET_KEY environment variable is set.", secretKey);
        }
        
        log.info("🔧 Initializing Cashfree SDK with environment: {}", envEnum);
        Cashfree cashfree = new Cashfree(envEnum, appId, secretKey, null, null, null);
        log.info("✅ Cashfree SDK initialized successfully");
        
        return cashfree;
    }
}