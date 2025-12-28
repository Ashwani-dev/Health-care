package com.ashwani.HealthCare.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfig {

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return new AuditorAware<>() {
            @Override
            public Optional<Long> getCurrentAuditor() {
                try {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication == null || !authentication.isAuthenticated()) {
                        return Optional.empty();
                    }

                    String principal = String.valueOf(authentication.getPrincipal());
                    // JwtFilter sets principal as userId string
                    try {
                        return Optional.of(Long.parseLong(principal));
                    } catch (NumberFormatException e) {
                        return Optional.empty();
                    }
                } catch (Exception e) {
                    return Optional.empty();
                }
            }
        };
    }
}

