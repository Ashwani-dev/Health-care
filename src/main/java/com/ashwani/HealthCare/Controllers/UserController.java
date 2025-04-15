package com.ashwani.HealthCare.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/me")
    public String getCurrentUser(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        return "Authenticated user ID: " + userId;
    }
}
