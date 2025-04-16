package com.ashwani.HealthCare.Controllers;

import com.ashwani.HealthCare.Entity.UserEntity;
import com.ashwani.HealthCare.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/me")
    public String getCurrentUser(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        return "Authenticated user ID: " + userId;
    }

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserEntity> registerUser(@RequestBody UserEntity user){
        UserEntity savedUser = userService.registerUser(user);
        return ResponseEntity.ok(savedUser);
    }
}
