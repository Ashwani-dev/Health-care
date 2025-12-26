package com.ashwani.HealthCare.DTO.Authentication;

public record AuthResponse (
    boolean success,
    String token,
    String role,
    Long userId
){}
