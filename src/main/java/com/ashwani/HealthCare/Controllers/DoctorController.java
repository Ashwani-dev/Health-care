package com.ashwani.HealthCare.Controllers;


import com.ashwani.HealthCare.DTO.DoctorDto;
import com.ashwani.HealthCare.Service.DoctorService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {
    @Autowired
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/profile")
    public String getCurrentUser(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        return "Authenticated user ID: " + userId;
    }

    @GetMapping
    public ResponseEntity<List<DoctorDto>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/search")
    public ResponseEntity<List<DoctorDto>> searchDoctor(
            @RequestParam(required = false) String specialization){
        if(specialization != null){
            return ResponseEntity.ok(doctorService.searchDoctors(specialization));
        }
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }
}
