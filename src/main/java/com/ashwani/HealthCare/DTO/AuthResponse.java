package com.ashwani.HealthCare.DTO;

public record AuthResponse (
    boolean success,
    String token,
    String role,
    Long userId
){}
