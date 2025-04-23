package com.ashwani.HealthCare.Controllers;

import com.ashwani.HealthCare.Entity.DoctorEntity;
import com.ashwani.HealthCare.Entity.UserEntity;
import com.ashwani.HealthCare.Service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/user/register")
    public String registerUser(@RequestBody UserEntity user){
        return authService.registerUser(user);
    }


    @PostMapping("/user/login")
    public String loginUser(@RequestParam String email, String password){
        return authService.loginUser(email, password);
    }

    @PostMapping("/doctor/register")
    public String registerDoctor(@RequestBody DoctorEntity doctor){
        return authService.registerDoctor(doctor);
    }


    @PostMapping("/doctor/login")
    public String loginDoctor(@RequestParam String email, String password){
        return authService.loginDoctor(email, password);
    }
}
