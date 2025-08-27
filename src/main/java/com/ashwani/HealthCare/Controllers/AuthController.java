package com.ashwani.HealthCare.Controllers;

import com.ashwani.HealthCare.DTO.AuthResponse;
import com.ashwani.HealthCare.Entity.DoctorEntity;
import com.ashwani.HealthCare.Entity.PatientEntity;
import com.ashwani.HealthCare.Service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
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
}
