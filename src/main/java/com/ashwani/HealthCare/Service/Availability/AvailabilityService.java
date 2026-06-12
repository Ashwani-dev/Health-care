package com.ashwani.HealthCare.Service.Availability;

import com.ashwani.HealthCare.DTO.DoctorAvailability.AvailabilityRequestDto;
import com.ashwani.HealthCare.DTO.DoctorAvailability.AvailabilityResponseDto;
import com.ashwani.HealthCare.Entity.DoctorAvailability;
import com.ashwani.HealthCare.Entity.Doctor;
import com.ashwani.HealthCare.ExceptionHandlers.common.ResourceNotFoundException;
import com.ashwani.HealthCare.Repository.DoctorAvailabilityRepository;
import com.ashwani.HealthCare.Repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {
    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorRepository doctorRepository;
    private final ModelMapper modelMapper;

    private AvailabilityResponseDto convertToResponse(DoctorAvailability availability) {
        return modelMapper.map(availability, AvailabilityResponseDto.class);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponseDto> getDoctorAvailability(Long doctorId) {
        return availabilityRepository.findByDoctorId(doctorId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<AvailabilityResponseDto> setAvailability(Long doctorId, List<AvailabilityRequestDto> requests){
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        return requests.stream()
                .map(request -> {
                    DoctorAvailability availability = modelMapper.map(request, DoctorAvailability.class);
                    availability.setDoctor(doctor);
                    return availabilityRepository.save(availability);
                })
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAvailabilitySlot(Long doctorId, Long slotId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        boolean slotExists = availabilityRepository.existsById(slotId);

        if (!slotExists) {
            throw new ResourceNotFoundException("Availability slot", slotId);
        }

        availabilityRepository.deleteByDoctorAndId(doctor, slotId);
    }

    @Transactional
    public AvailabilityResponseDto updateAvailabilitySlot(Long doctorId, Long slotId, AvailabilityRequestDto request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        DoctorAvailability availability = availabilityRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability slot", slotId));

        if (!availability.getDoctor().getId().equals(doctor.getId())) {
            throw new IllegalArgumentException("Availability slot " + slotId + " does not belong to doctor " + doctorId);
        }

        if (request.getDayOfWeek() != null) {
            availability.setDayOfWeek(request.getDayOfWeek());
        }
        if (request.getStartTime() != null) {
            availability.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            availability.setEndTime(request.getEndTime());
        }
        if (request.getIsAvailable() != null) {
            availability.setIsAvailable(request.getIsAvailable());
        }

        if (availability.getStartTime().isAfter(availability.getEndTime()) || 
                availability.getStartTime().equals(availability.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        DoctorAvailability updated = availabilityRepository.save(availability);
        return convertToResponse(updated);
    }
}
