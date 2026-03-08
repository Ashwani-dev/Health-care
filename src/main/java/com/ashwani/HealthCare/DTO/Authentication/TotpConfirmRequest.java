package com.ashwani.HealthCare.DTO.Authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TotpConfirmRequest(
    @NotBlank(message = "Secret is required")
    String secret,

    @NotBlank(message = "Code is required")
    @Size(min = 6, max = 6, message = "TOTP code must be 6 digits")
    @Pattern(regexp = "^[0-9]{6}$", message = "TOTP code must be 6 digits")
    String code
) {}
