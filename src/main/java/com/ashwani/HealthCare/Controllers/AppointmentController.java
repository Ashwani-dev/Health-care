package com.ashwani.HealthCare.Controllers;

import com.ashwani.HealthCare.DTO.Appointments.BookAppointmentRequest;
import com.ashwani.HealthCare.DTO.Appointments.PatientAppointmentResponse;
import com.ashwani.HealthCare.DTO.Appointments.UpdateAppointmentRequest;
import com.ashwani.HealthCare.Service.AppointmentService;
import com.ashwani.HealthCare.Utility.TimeSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
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

    /**
     * DEPRECATED: Direct booking endpoint - no longer used
     * All appointments now go through the payment flow:
     * 1. Create hold via POST /hold
     * 2. Complete payment via Cashfree
     * 3. PaymentEventListener creates the appointment automatically
     *
     * This endpoint is kept for reference but is disabled.
     * To re-enable, uncomment the @PostMapping annotation.
     */
    // @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@RequestBody BookAppointmentRequest request){
        return ResponseEntity.status(HttpStatus.GONE)
                .body(Map.of(
                        "error", "This endpoint is deprecated",
                        "message", "Please use POST /hold to create an appointment hold, then complete payment. The appointment will be created automatically after successful payment."
                ));
    }

    /**
     * Create an appointment hold (temporary reservation)
     * Useful to reserve a slot during payment workflow
     * @param request Appointment booking request
     * @return Hold identifier string
     */
    @PostMapping("/hold")
    public ResponseEntity<?> createAppointmentHold(@RequestBody BookAppointmentRequest request) {
        try {

            String holdId = appointmentService.createAppointmentHold(
                    request.getPatientId(),
                    request.getDoctorId(),
                    request.getDate(),
                    request.getStartTime(),
                    request.getReason()
            );

            return ResponseEntity.ok().body(holdId);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Cancel an existing appointment by the patient
     * @param appointmentId Appointment ID to cancel
     * @param principal Authenticated user principal
     * @return Success or error details
     */
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

    /**
     * Update an existing appointment's date and time
     * Only allows updates for appointments with SCHEDULED status
     * @param appointmentId Appointment ID to update
     * @param request Update request containing new date, startTime, and endTime
     * @return Updated appointment details or error message
     */
    @PutMapping("/{appointmentId}")
    public ResponseEntity<?> updateAppointment(
            @PathVariable Long appointmentId,
            @RequestBody UpdateAppointmentRequest request) {
        try {
            PatientAppointmentResponse response = appointmentService.updateAppointment(
                    appointmentId,
                    request.getAppointmentDate(),
                    request.getStartTime(),
                    request.getEndTime()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", e.getMessage(),
                    "appointmentId", appointmentId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "appointmentId", appointmentId
            ));
        }
    }

    /**
     * Get paginated appointments for a doctor
     * @param doctorId Doctor ID (path)
     * @param appointmentStartDate Optional start date filter (YYYY-MM-DD)
     * @param appointmentEndDate Optional end date filter (YYYY-MM-DD)
     * @param startTime Optional start time filter (HH:MM:SS)
     * @param endTime Optional end time filter (HH:MM:SS)
     * @param status Optional status filter
     * @param pageable Pageable configuration (defaults: sort by date ASC)
     * @param assembler HATEOAS assembler
     * @return PagedModel of patient appointment responses
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<PagedModel<EntityModel<PatientAppointmentResponse>>> getDoctorAppointments(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentStartDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentEndDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) String status,
            @PageableDefault(sort = "appointmentDate", direction = Sort.Direction.ASC) Pageable pageable,
            PagedResourcesAssembler<PatientAppointmentResponse> assembler) {

        Page<PatientAppointmentResponse> appointments = appointmentService.getDoctorAppointments(
                doctorId, appointmentStartDate, appointmentEndDate, startTime, endTime, status, pageable);

        return ResponseEntity.ok(assembler.toModel(appointments));
    }

    /**
     * Get paginated appointments for a patient
     * @param patientId Patient ID (path)
     * @param appointmentStartDate Optional start date filter (YYYY-MM-DD)
     * @param appointmentEndDate Optional end date filter (YYYY-MM-DD)
     * @param startTime Optional start time filter (HH:MM:SS)
     * @param endTime Optional end time filter (HH:MM:SS)
     * @param status Optional status filter
     * @param pageable Pageable configuration (defaults: sort by date ASC)
     * @param assembler HATEOAS assembler
     * @return PagedModel of patient appointment responses
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<PagedModel<EntityModel<PatientAppointmentResponse>>> getPatientAppointments(
            @PathVariable Long patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentStartDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentEndDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) String status,
            @PageableDefault(sort = "appointmentDate", direction = Sort.Direction.ASC) Pageable pageable,
            PagedResourcesAssembler<PatientAppointmentResponse> assembler) {

        Page<PatientAppointmentResponse> appointments = appointmentService.getPatientAppointments(
                patientId, appointmentStartDate, appointmentEndDate, startTime, endTime, status, pageable);

        return ResponseEntity.ok(assembler.toModel(appointments));
    }

    @GetMapping("availability/{doctorId}")
    /**
     * Get available time slots for a specific doctor on a date
     * @param doctorId Doctor ID
     * @param date Date (YYYY-MM-DD)
     * @return List of available time slots
     */
    public ResponseEntity<List<TimeSlot>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<TimeSlot> slots = appointmentService.getAvailableSlots(doctorId, date);
        return ResponseEntity.ok(slots);
    }
}
