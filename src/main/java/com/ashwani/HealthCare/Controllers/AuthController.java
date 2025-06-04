package com.ashwani.HealthCare.Controllers;

import com.ashwani.HealthCare.Entity.DoctorEntity;
import com.ashwani.HealthCare.Entity.PatientEntity;
import com.ashwani.HealthCare.Service.AuthService;
import org.springframework.web.bind.annotation.*;

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
    public String loginPatient(@RequestParam String email, String password){
        return authService.loginPatient(email, password);
    }

//    For doctor registration
    @PostMapping("/doctor/register")
    public String registerDoctor(@RequestBody DoctorEntity doctor){
        return authService.registerDoctor(doctor);
    }


//    For doctor login
    @PostMapping("/doctor/login")
    public String loginDoctor(@RequestParam String email, String password){
        return authService.loginDoctor(email, password);
    }
}
