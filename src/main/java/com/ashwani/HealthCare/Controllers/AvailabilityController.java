package com.ashwani.HealthCare.Controllers;

import com.ashwani.HealthCare.DTO.DoctorAvailability.AvailabilityRequestDto;
import com.ashwani.HealthCare.DTO.DoctorAvailability.AvailabilityResponseDto;
import com.ashwani.HealthCare.Service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    @PostMapping("/{doctorId}")
    public ResponseEntity<List<AvailabilityResponseDto>> setAvailability(
            @PathVariable Long doctorId,
            @RequestBody List<AvailabilityRequestDto> requests,
            Principal principal
    ) throws AccessDeniedException {
        // Verify doctor is updating their own availability
        if(!principal.getName().equals(doctorId.toString())){
            throw new AccessDeniedException("You can only update your own availability");
        }

        List<AvailabilityResponseDto> availabilities = availabilityService.setAvailability(doctorId, requests);
        return ResponseEntity.ok(availabilities);
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<List<AvailabilityResponseDto>> getAvailability(
            @PathVariable Long doctorId) {
        List<AvailabilityResponseDto> availabilities = availabilityService.getDoctorAvailability(doctorId);
        return ResponseEntity.ok(availabilities);
    }

    @DeleteMapping("/{doctorId}/{slotId}")
    public ResponseEntity<Void> deleteAvailabilitySlot(
            @PathVariable Long doctorId,
            @PathVariable Long slotId,
            Principal principal) throws AccessDeniedException {

        if (!principal.getName().equals(doctorId.toString())) {
            throw new AccessDeniedException("You can only delete your own availability slots");
        }

        availabilityService.deleteAvailabilitySlot(doctorId, slotId);
        return ResponseEntity.noContent().build();
    }
}
