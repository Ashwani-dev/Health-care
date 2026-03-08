package com.ashwani.HealthCare.Service.Patient;

import com.ashwani.HealthCare.DTO.Patient.PatientProfile;
import com.ashwani.HealthCare.DTO.Patient.PatientProfileUpdateRequest;
import com.ashwani.HealthCare.Entity.Patient;
import com.ashwani.HealthCare.ExceptionHandlers.common.ResourceNotFoundException;
import com.ashwani.HealthCare.Repository.PatientRepository;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientService {

    @Autowired
    private final PatientRepository patientRepository;

    @Autowired
    private final ModelMapper modelMapper;


    public PatientService(PatientRepository patientRepository,ModelMapper modelMapper) {
        this.patientRepository = patientRepository;
        this.modelMapper = modelMapper;
    }

    public PatientProfile updatePatientProfile(Long patientId, @Valid PatientProfileUpdateRequest updateRequest) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        // Update only allowed fields
        patient.setFull_name(updateRequest.full_name());
        patient.setAddress(updateRequest.address());

        Patient updatedPatient = patientRepository.save(patient);
        return modelMapper.map(updatedPatient, PatientProfile.class);
    }
}
