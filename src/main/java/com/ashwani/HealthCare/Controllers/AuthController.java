package com.ashwani.HealthCare.Controllers;

import com.ashwani.HealthCare.DTO.Authentication.AuthResponse;
import com.ashwani.HealthCare.DTO.Authentication.ServiceAuthResponse;
import java.util.Map;
import com.ashwani.HealthCare.DTO.Authentication.PasswordLoginRequest;
import com.ashwani.HealthCare.DTO.Authentication.PasswordResetDTO;
import com.ashwani.HealthCare.DTO.Authentication.PasswordResetRequestDTO;
import com.ashwani.HealthCare.DTO.Authentication.TotpLoginRequest;
import com.ashwani.HealthCare.Entity.Doctor;
import com.ashwani.HealthCare.Entity.Patient;
import com.ashwani.HealthCare.Service.Auth.AuthService;
import com.ashwani.HealthCare.Service.Auth.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    @Value("${jwt.expiration.ms}")
    private int expirationMs;

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final Environment env;

    public AuthController(AuthService authService, PasswordResetService passwordResetService, Environment env){
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.env = env;
    }

    /**
     * Register a new patient account
     * @param patient Patient entity payload containing registration details
     * @return Registration result message
     */
    @PostMapping("/patient/register")
    public String registerPatient(@RequestBody Patient patient){
        return authService.registerPatient(patient);
    }

    /**
     * Register a new doctor account
     * @param doctor Doctor entity payload containing registration details
     * @return Registration result message
     */
    @PostMapping("/doctor/register")
    public String registerDoctor(@RequestBody Doctor doctor){
        return authService.registerDoctor(doctor);
    }

    // ============================================
    // Unified Authentication Endpoints
    // ============================================

    /**
     * Universal password login endpoint
     * Supports both patients and doctors via userType parameter
     * @param loginRequest PasswordLoginRequest with email and password
     * @param userType "PATIENT" or "DOCTOR"
     * @return AuthResponse containing token and user info
     */
    @PostMapping("/login/password")
    public ResponseEntity<AuthResponse> loginWithPassword(
            @Valid @RequestBody PasswordLoginRequest loginRequest,
            @RequestParam String userType) {

        ServiceAuthResponse serviceResponse;
        if ("PATIENT".equalsIgnoreCase(userType)) {
            serviceResponse = authService.loginPatient(loginRequest.email(), loginRequest.password());
        } else if ("DOCTOR".equalsIgnoreCase(userType)) {
            serviceResponse = authService.loginDoctor(loginRequest.email(), loginRequest.password());
        } else {
            throw new RuntimeException("Invalid user type. Must be PATIENT or DOCTOR");
        }

        // Determine secure flag: must be true if we use SameSite=None (even in local development, 
        // unless you run local backend over HTTP, in which case dev should remain Lax)
        boolean isDev = env.acceptsProfiles(Profiles.of("dev"));
        String sameSiteMode = isDev ? "Lax" : "None";
        boolean secureFlag = !isDev; // SameSite=None requires Secure=true

        ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", serviceResponse.token())
                .httpOnly(true)
                .secure(secureFlag)
                .path("/")
                .maxAge(Duration.ofMillis(expirationMs))
                .sameSite(sameSiteMode) // Set to "None" in production for cross-site cookie support
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(serviceResponse.authResponse());
    }

    /**
     * Universal TOTP login endpoint
     * Supports both patients and doctors via userType parameter
     * @param loginRequest TotpLoginRequest with email and code
     * @param userType "PATIENT" or "DOCTOR"
     * @return AuthResponse containing token and user info
     */
    @PostMapping("/login/totp")
    public ResponseEntity<AuthResponse> loginWithTotp(
            @Valid @RequestBody TotpLoginRequest loginRequest,
            @RequestParam String userType) {

        ServiceAuthResponse serviceResponse;
        if ("PATIENT".equalsIgnoreCase(userType)) {
            serviceResponse = authService.loginPatientWithTotp(loginRequest.email(), loginRequest.code());
        } else if ("DOCTOR".equalsIgnoreCase(userType)) {
            serviceResponse = authService.loginDoctorWithTotp(loginRequest.email(), loginRequest.code());
        } else {
            throw new RuntimeException("Invalid user type. Must be PATIENT or DOCTOR");
        }

        // Determine secure flag: must be true if we use SameSite=None (even in local development, 
        // unless you run local backend over HTTP, in which case dev should remain Lax)
        boolean isDev = env.acceptsProfiles(Profiles.of("dev"));
        String sameSiteMode = isDev ? "Lax" : "None";
        boolean secureFlag = !isDev; // SameSite=None requires Secure=true

        ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", serviceResponse.token())
                .httpOnly(true)
                .secure(secureFlag)
                .path("/")
                .maxAge(Duration.ofMillis(expirationMs))
                .sameSite(sameSiteMode) // Set to "None" in production for cross-site cookie support
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(serviceResponse.authResponse());
    }

    /**
     * Logout endpoint to clear the jwtToken HTTP-only cookie
     * @return Success message response
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // Determine secure flag: must be true if we use SameSite=None (even in local development, 
        // unless you run local backend over HTTP, in which case dev should remain Lax)
        boolean isDev = env.acceptsProfiles(Profiles.of("dev"));
        String sameSiteMode = isDev ? "Lax" : "None";
        boolean secureFlag = !isDev; // SameSite=None requires Secure=true

        ResponseCookie deleteCookie = ResponseCookie.from("jwtToken", "")
                .httpOnly(true)
                .secure(secureFlag)
                .path("/")
                .maxAge(0) // 0 tells the browser to immediately delete the cookie
                .sameSite(sameSiteMode)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }

    // ============================================
    // Password Reset Endpoints
    // ============================================

    /**
     * Request password reset for a patient
     * @param request PasswordResetRequestDTO containing email
     * @return Success message
     */
    @PostMapping("/patient/forgot-password")
    public ResponseEntity<String> requestPatientPasswordReset(@Valid @RequestBody PasswordResetRequestDTO request) {
        String message = passwordResetService.requestPatientPasswordReset(request);
        return ResponseEntity.ok(message);
    }

    /**
     * Request password reset for a doctor
     * @param request PasswordResetRequestDTO containing email
     * @return Success message
     */
    @PostMapping("/doctor/forgot-password")
    public ResponseEntity<String> requestDoctorPasswordReset(@Valid @RequestBody PasswordResetRequestDTO request) {
        String message = passwordResetService.requestDoctorPasswordReset(request);
        return ResponseEntity.ok(message);
    }

    /**
     * Reset password using token (works for both patient and doctor)
     * @param resetDTO PasswordResetDTO containing token and new password
     * @return Success message
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetDTO resetDTO) {
        String message = passwordResetService.resetPassword(resetDTO);
        return ResponseEntity.ok(message);
    }
}
