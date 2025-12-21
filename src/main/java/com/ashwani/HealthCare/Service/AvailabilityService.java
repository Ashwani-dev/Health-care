package com.ashwani.HealthCare.Service;

import com.ashwani.HealthCare.DTO.DoctorAvailability.AvailabilityRequestDto;
import com.ashwani.HealthCare.DTO.DoctorAvailability.AvailabilityResponseDto;
import com.ashwani.HealthCare.Entity.DoctorAvailability;
import com.ashwani.HealthCare.Entity.DoctorEntity;
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
        DoctorEntity doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

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
        DoctorEntity doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        boolean slotExists = availabilityRepository.existsById(slotId);

        if (!slotExists) {
            throw new RuntimeException("Availability slot not found or does not belong to this doctor");
        }

        availabilityRepository.deleteByDoctorAndId(doctor, slotId);
    }
}
