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

//    For patient registration
    @PostMapping("/patient/register")
    public String registerPatient(@RequestBody PatientEntity patient){
        return authService.registerPatient(patient);
    }


//    For patient login
    @PostMapping("/patient/login")
    public ResponseEntity<AuthResponse> loginPatient(@RequestBody Map<String, String> credentials) {
        AuthResponse response = authService.loginPatient(
                credentials.get("email"),
                credentials.get("password")
        );
        return ResponseEntity.ok(response);
    }

//    For doctor registration
    @PostMapping("/doctor/register")
    public String registerDoctor(@RequestBody DoctorEntity doctor){
        return authService.registerDoctor(doctor);
    }


//    For doctor login
    @PostMapping("/doctor/login")
    public ResponseEntity<AuthResponse> loginDoctor(@RequestBody Map<String, String> credentials){
        AuthResponse response = authService.loginDoctor(
                credentials.get("email"),
                credentials.get("password")
        );
        return ResponseEntity.ok(response);
    }
}
