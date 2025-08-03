package com.ashwani.HealthCare.Config;

import com.cashfree.pg.Cashfree;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
        // Convert String environment to CFEnvironment enum
        Cashfree.CFEnvironment envEnum = Cashfree.CFEnvironment.valueOf(environment.toUpperCase());

        return new Cashfree(envEnum, appId, secretKey, null, null, null);
    }
}