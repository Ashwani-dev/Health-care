package com.ashwani.HealthCare.Controllers;

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

    @PostMapping("/register")
    public String register(@RequestBody UserEntity user){
        return authService.register(user);
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, String password){
        return authService.login(email, password);
    }
}
