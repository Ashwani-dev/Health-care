package com.ashwani.HealthCare.Controllers;

import com.ashwani.HealthCare.DTO.Appointments.BookAppointmentRequest;
import com.ashwani.HealthCare.DTO.Appointments.PatientAppointmentResponse;
import com.ashwani.HealthCare.Entity.AppointmentEntity;
import com.ashwani.HealthCare.Service.AppointmentService;
import com.ashwani.HealthCare.Utility.TimeSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
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
//    For cancelling appointment with doctor by patient
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
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", e.getMessage(),
                    "appointmentId", appointmentId
            ));
        }
    }

//    Doctor can access all of his/her scheduled appointment
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<PatientAppointmentResponse>> getDoctorAppointments(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) String status) {

        List<PatientAppointmentResponse> appointments = appointmentService.getDoctorAppointments(
                doctorId,
                appointmentDate,
                startTime,
                status);

        return ResponseEntity.ok(appointments);
    }

//    Patient can access all of his/her scheduled appointment
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PatientAppointmentResponse>> getPatientAppointments(
            @PathVariable Long patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) String status){

        List<PatientAppointmentResponse> appointments = appointmentService.getPatientAppointments(
                patientId,
                appointmentDate,
                startTime,
                status);
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
