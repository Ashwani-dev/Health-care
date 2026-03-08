package com.ashwani.HealthCare.DTO.Authentication;

public record TotpSetupResponse(
    String qrCodeImage,  // Base64 data URI of QR code
    String secret        // Raw secret for manual entry
) {}
