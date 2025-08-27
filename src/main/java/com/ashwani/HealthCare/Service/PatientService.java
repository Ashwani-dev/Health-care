package com.ashwani.HealthCare.Service;

import com.ashwani.HealthCare.DTO.Patient.PatientProfile;
import com.ashwani.HealthCare.DTO.Patient.PatientProfileUpdateRequest;
import com.ashwani.HealthCare.Entity.PatientEntity;
import com.ashwani.HealthCare.Repository.PatientRepository;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Patient not found with ID: " + patientId
                ));

        // Update only allowed fields
        patient.setFull_name(updateRequest.full_name());
        patient.setAddress(updateRequest.address());

        PatientEntity updatedPatient = patientRepository.save(patient);
        return modelMapper.map(updatedPatient, PatientProfile.class);
    }
}
