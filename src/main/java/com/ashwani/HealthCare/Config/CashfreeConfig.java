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
    @Value("${cashfree.env:SANDBOX}")
    private String environment;

    @Bean
    public Cashfree cashfreeSDK() {
        // Validate configuration
        if (appId == null || appId.trim().isEmpty()) {
            log.error("Cashfree APP_ID is not configured! Please set cashfree.appId or APP_ID environment variable.");
            throw new IllegalStateException("Cashfree APP_ID is required but not configured");
        }
        
        if (secretKey == null || secretKey.trim().isEmpty()) {
            log.error("Cashfree SECRET_KEY is not configured! Please set cashfree.secretKey or SECRET_KEY environment variable.");
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

        log.info("Initializing Cashfree SDK with environment: {}, AppId: {}", envEnum, appId.substring(0, Math.min(8, appId.length())) + "***");
        
        return new Cashfree(envEnum, appId, secretKey, null, null, null);
    }
}