package com.ashwani.HealthCare.DTO.Authentication;

public record MfaResponse(
    String message,
    String loginMethod
) {}
