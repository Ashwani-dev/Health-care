package com.ashwani.HealthCare.Controllers;

import com.ashwani.HealthCare.DTO.AvailabilityRequestDto;
import com.ashwani.HealthCare.DTO.AvailabilityResponseDto;
import com.ashwani.HealthCare.Service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {
    private final AvailabilityService availabilityService;
    @GetMapping("/me")
    public String getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }
        return "Current user: " + principal.getName(); // Returns the userId from JWT
    }
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

        List<AvailabilityResponseDto> responses = availabilities.stream()
                .map(av-> new AvailabilityResponseDto(
                        av.getId(),
                        av.getDoctorId(),
                        av.getDayOfWeek(),
                        av.getStartTime(),
                        av.getEndTime(),
                        av.isAvailable()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<List<AvailabilityResponseDto>> getAvailability(
            @PathVariable Long doctorId) {
        List<AvailabilityResponseDto> availabilities = availabilityService.getDoctorAvailability(doctorId);

        List<AvailabilityResponseDto> responses = availabilities.stream()
                .map(av -> new AvailabilityResponseDto(
                        av.getId(),
                        av.getDoctorId(),
                        av.getDayOfWeek(),
                        av.getStartTime(),
                        av.getEndTime(),
                        av.isAvailable()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
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
