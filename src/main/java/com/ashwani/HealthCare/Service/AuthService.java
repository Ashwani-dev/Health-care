package com.ashwani.HealthCare.Service;

import com.ashwani.HealthCare.DTO.Authentication.AuthResponse;
import com.ashwani.HealthCare.Entity.DoctorEntity;
import com.ashwani.HealthCare.Entity.PatientEntity;
import com.ashwani.HealthCare.Repository.DoctorRepository;
import com.ashwani.HealthCare.Repository.PatientRepository;
import com.ashwani.HealthCare.Utility.JWTUtility;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final JWTUtility jwtUtility;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(PatientRepository patientRepository, DoctorRepository doctorRepository, JWTUtility jwtUtility) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.jwtUtility = jwtUtility;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String registerPatient(PatientEntity patient) {
        // Check if user exists
        if (patientRepository.findByEmail(patient.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        if (patientRepository.findByUsername(patient.getUsername()).isPresent()) {
            throw new RuntimeException("Username does already exist");
        }

        // Hash password
        patient.setPassword(passwordEncoder.encode(patient.getPassword()));

        // Save user
        PatientEntity savedPatient = patientRepository.save(patient);

        // Generate token
        return jwtUtility.generateToken(savedPatient.getId().toString(), "PATIENT");
    }


    public AuthResponse loginPatient(String email, String password) {
        PatientEntity patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, patient.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        String token = jwtUtility.generateToken(patient.getId().toString(), "PATIENT");

        return new AuthResponse(true, token, "PATIENT", patient.getId());
    }

    public String registerDoctor(DoctorEntity doctor) {
        if (doctorRepository.findByEmail(doctor.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        if (doctorRepository.findByUsername(doctor.getUsername()).isPresent()) {
            throw new RuntimeException("Username does already exist");
        }

        if (doctorRepository.findByLicenseNumber(doctor.getLicense_number()).isPresent()) {
            throw new RuntimeException("License number already exists");
        }

        doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));

        DoctorEntity savedDoctor = doctorRepository.save(doctor);

        return jwtUtility.generateToken(savedDoctor.getId().toString(), "DOCTOR");
    }

    public AuthResponse loginDoctor(String email, String password) {
        DoctorEntity doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(!passwordEncoder.matches(password, doctor.getPassword())){
            throw new RuntimeException("Invalid Credentials");
        }
        String token = jwtUtility.generateToken(doctor.getId().toString(), "DOCTOR");

        return new AuthResponse(true, token, "DOCTOR", doctor.getId());
    }
}
