package com.ashwani.HealthCare.Controllers;

import com.ashwani.HealthCare.DTO.Authentication.MfaResponse;
import com.ashwani.HealthCare.DTO.Authentication.TotpConfirmRequest;
import com.ashwani.HealthCare.DTO.Authentication.TotpSetupResponse;
import com.ashwani.HealthCare.Service.Auth.MfaService;
import com.ashwani.HealthCare.Utility.JWTUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/totp")
public class MfaController {

    private final MfaService mfaService;
    private final JWTUtility jwtUtility;

    public MfaController(MfaService mfaService, JWTUtility jwtUtility) {
        this.mfaService = mfaService;
        this.jwtUtility = jwtUtility;
    }

    /**
     * Setup TOTP - generates secret and QR code
     * Requires authentication
     */
    @PostMapping("/setup")
    public ResponseEntity<TotpSetupResponse> setupTotp(HttpServletRequest request) {
        String token = extractToken(request);
        Long userId = Long.parseLong(jwtUtility.getUserIdFromToken(token));
        String userType = jwtUtility.getRoleFromToken(token);

        TotpSetupResponse response = mfaService.setupTotpByUserId(userId, userType);
        return ResponseEntity.ok(response);
    }

    /**
     * Confirm TOTP setup - verifies code and enables TOTP
     * Requires authentication
     */
    @PostMapping("/confirm")
    public ResponseEntity<MfaResponse> confirmTotp(
            @Valid @RequestBody TotpConfirmRequest confirmRequest,
            HttpServletRequest request) {
        String token = extractToken(request);
        Long userId = Long.parseLong(jwtUtility.getUserIdFromToken(token));
        String userType = jwtUtility.getRoleFromToken(token);

        MfaResponse response = mfaService.confirmTotpByUserId(
                userId,
                confirmRequest.secret(),
                confirmRequest.code(),
                userType
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Disable TOTP for authenticated user
     * Requires authentication
     */
    @PostMapping("/disable")
    public ResponseEntity<MfaResponse> disableTotp(HttpServletRequest request) {
        String token = extractToken(request);
        Long userId = Long.parseLong(jwtUtility.getUserIdFromToken(token));
        String userType = jwtUtility.getRoleFromToken(token);

        MfaResponse response = mfaService.disableTotpByUserId(userId, userType);
        return ResponseEntity.ok(response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("No authorization token found");
    }
}
