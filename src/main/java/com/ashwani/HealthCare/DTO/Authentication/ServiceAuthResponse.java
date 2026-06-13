package com.ashwani.HealthCare.DTO.Authentication;

/**
 * Wrapper for authentication results returned from the service layer to the controller.
 * Keeps the JWT token separated from the client-facing AuthResponse DTO.
 */
public record ServiceAuthResponse(
    AuthResponse authResponse,
    String token
) {}
