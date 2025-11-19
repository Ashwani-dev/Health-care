package com.ashwani.HealthCare.Config;

import com.ashwani.HealthCare.Filter.JwtFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173,https://*.ngrok-free.app}")
    private String corsAllowedOrigins;

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // Disable CSRF (APIs are stateless)
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (no auth required)
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/api/auth/patient/register",
                                "/api/auth/patient/login",
                                "/api/auth/doctor/register",
                                "/api/auth/doctor/login",
                                "/api/video-call/webhook",
                                "/api/payments/webhook/**"
                        ).permitAll()

                        // Role-based access
                        .requestMatchers("/api/patient/**").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.PUT, "/api/doctor/profile").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.GET,"/api/doctor/**").hasAnyRole("DOCTOR", "PATIENT")
                        .requestMatchers(HttpMethod.POST, "/api/availability/{doctorId}").hasRole("DOCTOR")
                        .requestMatchers("/api/availability/**").hasAnyRole("DOCTOR", "PATIENT")
                        .requestMatchers(
                                HttpMethod.DELETE, "/api/availability/{doctorId}/{slotId}"
                        ).hasRole("DOCTOR")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint())
                );

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(){
        return (request, response, ex) -> {
            response.setContentType("application/json");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(
                    "{\"error\":\"Unauthorized\",\"message\":\"Invalid credentials\"}"
            );
        };
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Parse allowed origins from environment variable or use defaults
        List<String> allowedOrigins = Arrays.asList(corsAllowedOrigins.split(","))
                .stream()
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .collect(Collectors.toList());
        
        // Add common production origins if not already present
        if (!allowedOrigins.contains("https://health-care-7oam.onrender.com")) {
            allowedOrigins.add("https://health-care-7oam.onrender.com");
        }
        if (!allowedOrigins.contains("http://localhost:5173")) {
            allowedOrigins.add("http://localhost:5173");
        }
        
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}