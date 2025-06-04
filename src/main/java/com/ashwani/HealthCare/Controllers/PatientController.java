package com.ashwani.HealthCare.Controllers;


import com.ashwani.HealthCare.Service.AppointmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient")
public class PatientController {
    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/me")
    public String getCurrentUser(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        return "Authenticated user ID: " + userId;
    }
}
