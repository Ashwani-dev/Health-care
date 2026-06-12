package com.ashwani.HealthCare.Controllers;

import com.ashwani.HealthCare.DTO.DoctorAvailability.AvailabilityRequestDto;
import com.ashwani.HealthCare.DTO.DoctorAvailability.AvailabilityResponseDto;
import com.ashwani.HealthCare.Service.Availability.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    @PostMapping("/{doctorId}")
    /**
     * Set availability slots for a doctor
     * @param doctorId Doctor's ID (path)
     * @param requests List of availability requests to set
     * @param principal Current authenticated user (doctor)
     * @return List of created/updated availability slots
     */
    public ResponseEntity<List<AvailabilityResponseDto>> setAvailability(
            @PathVariable Long doctorId,
            @RequestBody List<AvailabilityRequestDto> requests,
            Principal principal
    ) {
        // Verify doctor is updating their own availability
        if(!principal.getName().equals(doctorId.toString())){
            throw new AccessDeniedException("You can only update your own availability");
        }

        List<AvailabilityResponseDto> availabilities = availabilityService.setAvailability(doctorId, requests);
        return ResponseEntity.ok(availabilities);
    }

    @GetMapping("/{doctorId}")
    /**
     * Get availability slots for a doctor
     * @param doctorId Doctor's ID
     * @return List of availability slots
     */
    public ResponseEntity<List<AvailabilityResponseDto>> getAvailability(
            @PathVariable Long doctorId) {
        List<AvailabilityResponseDto> availabilities = availabilityService.getDoctorAvailability(doctorId);
        return ResponseEntity.ok(availabilities);
    }

    @DeleteMapping("/{doctorId}/{slotId}")
    /**
     * Delete a specific availability slot for a doctor
     * @param doctorId Doctor's ID
     * @param slotId Slot ID to delete
     * @param principal Current authenticated user (doctor)
     * @return Success message on success
     */
    public ResponseEntity<String> deleteAvailabilitySlot(
            @PathVariable Long doctorId,
            @PathVariable Long slotId,
            Principal principal) {

        if (!principal.getName().equals(doctorId.toString())) {
            throw new AccessDeniedException("You can only delete your own availability slots");
        }

        availabilityService.deleteAvailabilitySlot(doctorId, slotId);
        return ResponseEntity.ok("Success: Availability slot " + slotId + " deleted successfully");
    }

    @PutMapping("/{doctorId}/{slotId}")
    /**
     * Update a specific availability slot for a doctor
     * @param doctorId Doctor's ID
     * @param slotId Slot ID to update
     * @param request Updated availability details
     * @param principal Current authenticated user (doctor)
     * @return Updated availability slot details
     */
    public ResponseEntity<AvailabilityResponseDto> updateAvailabilitySlot(
            @PathVariable Long doctorId,
            @PathVariable Long slotId,
            @RequestBody AvailabilityRequestDto request,
            Principal principal) {

        if (!principal.getName().equals(doctorId.toString())) {
            throw new AccessDeniedException("You can only update your own availability slots");
        }

        AvailabilityResponseDto updatedSlot = availabilityService.updateAvailabilitySlot(doctorId, slotId, request);
        return ResponseEntity.ok(updatedSlot);
    }
}
