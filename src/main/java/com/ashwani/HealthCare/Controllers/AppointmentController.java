package com.ashwani.HealthCare.Controllers;

import com.ashwani.HealthCare.DTO.BookAppointmentRequest;
import com.ashwani.HealthCare.DTO.PatientAppointmentResponse;
import com.ashwani.HealthCare.Entity.AppointmentEntity;
import com.ashwani.HealthCare.Service.AppointmentService;
import com.ashwani.HealthCare.Utility.TimeSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/appointments")
public class AppointmentController {
    @Autowired
    private AppointmentService appointmentService;

//    For booking appointment with doctor by patient
    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@RequestBody BookAppointmentRequest request){
        try {
            AppointmentEntity appointment = appointmentService.bookAppointment(
                    request.getPatientId(),
                    request.getDoctorId(),
                    request.getDate(),
                    request.getStartTime(),
                    request.getReason()
            );
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
//    For deleting appointment with doctor by patient
    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId, Principal principal) {
        try {
            Long userId = Long.parseLong(principal.getName());
            appointmentService.cancelAppointment(appointmentId, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "Appointment cancelled successfully",
                    "appointmentId", appointmentId
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Appointment already cancelled",
                    "appointmentId", appointmentId,
                    "status", "CANCELLED"
            ));
        }
    }

//    Doctor can access all of his/her scheduled appointment
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<PatientAppointmentResponse>> getDoctorAppointments(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate
                    date
            ){
        if(date == null){
            date = LocalDate.now();
        }

        List<PatientAppointmentResponse> appointments = appointmentService.getDoctorAppointments(doctorId, date);

        return ResponseEntity.ok(appointments);
    }

//    Patient can access all of his/her scheduled appointment
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PatientAppointmentResponse>> getPatientAppointments(
            @PathVariable Long patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate
                    date
    ){
        if(date == null){
            date = LocalDate.now();
        }

        List<PatientAppointmentResponse> appointments = appointmentService.getPatientAppointments(patientId);

        return ResponseEntity.ok(appointments);
    }

    @GetMapping("availability/{doctorId}")
    public ResponseEntity<List<TimeSlot>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<TimeSlot> slots = appointmentService.getAvailableSlots(doctorId, date);
        return ResponseEntity.ok(slots);
    }
}
