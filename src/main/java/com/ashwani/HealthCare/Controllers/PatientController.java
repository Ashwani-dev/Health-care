package com.ashwani.HealthCare.Controllers;


import com.ashwani.HealthCare.Entity.AppointmentEntity;
import com.ashwani.HealthCare.Service.AppointmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

    @GetMapping("patient/{patientId}")
    public ResponseEntity<List<AppointmentEntity>> getPatientAppointments(
            @PathVariable Long patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate
                    date
    ){
        if(date == null){
            date = LocalDate.now();
        }

        List<AppointmentEntity> appointments = appointmentService.getPatientAppointments(patientId);

        return ResponseEntity.ok(appointments);
    }
}
