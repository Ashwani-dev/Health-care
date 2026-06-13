package com.ashwani.HealthCare.DTO.Authentication;

public record AuthResponse (
    boolean success,
    String role,
    Long userId,
    String loginMethod
){}
