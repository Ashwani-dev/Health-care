package com.ashwani.HealthCare.Controllers;

import com.ashwani.HealthCare.DTO.Authentication.AuthResponse;
import com.ashwani.HealthCare.DTO.Authentication.PasswordResetDTO;
import com.ashwani.HealthCare.DTO.Authentication.PasswordResetRequestDTO;
import com.ashwani.HealthCare.Entity.DoctorEntity;
import com.ashwani.HealthCare.Entity.PatientEntity;
import com.ashwani.HealthCare.Service.AuthService;
import com.ashwani.HealthCare.Service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService){
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    /**
     * Register a new patient account
     * @param patient Patient entity payload containing registration details
     * @return Registration result message
     */
    @PostMapping("/patient/register")
    public String registerPatient(@RequestBody PatientEntity patient){
        return authService.registerPatient(patient);
    }


    /**
     * Authenticate a patient and return JWT tokens
     * @param credentials Map with keys: email, password
     * @return AuthResponse containing token and user info
     */
    @PostMapping("/patient/login")
    public ResponseEntity<AuthResponse> loginPatient(@RequestBody Map<String, String> credentials) {
        AuthResponse response = authService.loginPatient(
                credentials.get("email"),
                credentials.get("password")
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Register a new doctor account
     * @param doctor Doctor entity payload containing registration details
     * @return Registration result message
     */
    @PostMapping("/doctor/register")
    public String registerDoctor(@RequestBody DoctorEntity doctor){
        return authService.registerDoctor(doctor);
    }


    /**
     * Authenticate a doctor and return JWT tokens
     * @param credentials Map with keys: email, password
     * @return AuthResponse containing token and user info
     */
    @PostMapping("/doctor/login")
    public ResponseEntity<AuthResponse> loginDoctor(@RequestBody Map<String, String> credentials){
        AuthResponse response = authService.loginDoctor(
                credentials.get("email"),
                credentials.get("password")
        );
        return ResponseEntity.ok(response);
    }

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
