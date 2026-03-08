package com.ashwani.HealthCare.Service.Auth;

import com.ashwani.HealthCare.DTO.Authentication.AuthResponse;
import com.ashwani.HealthCare.Entity.Doctor;
import com.ashwani.HealthCare.Entity.Patient;
import com.ashwani.HealthCare.Enums.LoginMethod;
import com.ashwani.HealthCare.ExceptionHandlers.auth.InvalidCredentialsException;
import com.ashwani.HealthCare.ExceptionHandlers.auth.InvalidTotpCodeException;
import com.ashwani.HealthCare.ExceptionHandlers.auth.LoginMethodMismatchException;
import com.ashwani.HealthCare.ExceptionHandlers.auth.TotpNotEnabledException;
import com.ashwani.HealthCare.ExceptionHandlers.common.DuplicateResourceException;
import com.ashwani.HealthCare.ExceptionHandlers.common.ResourceNotFoundException;
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
    private final MfaService mfaService;

    public AuthService(PatientRepository patientRepository, DoctorRepository doctorRepository,
                      JWTUtility jwtUtility, MfaService mfaService) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.jwtUtility = jwtUtility;
        this.mfaService = mfaService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String registerPatient(Patient patient) {
        // Check if user exists
        if (patientRepository.findByEmail(patient.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User", "email");
        }

        if (patientRepository.findByUsername(patient.getUsername()).isPresent()) {
            throw new DuplicateResourceException("User", "username");
        }

        // Hash password
        patient.setPassword(passwordEncoder.encode(patient.getPassword()));

        // Save user
        Patient savedPatient = patientRepository.save(patient);

        // Generate token
        return jwtUtility.generateToken(savedPatient.getId().toString(), "PATIENT");
    }


    public AuthResponse loginPatient(String email, String password) {
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", email));

        // Check if user is using TOTP only (not password or both)
        if (patient.getLoginMethod() == LoginMethod.TOTP) {
            throw new LoginMethodMismatchException("This account uses authenticator login only. Please use TOTP code.");
        }

        if (!passwordEncoder.matches(password, patient.getPassword())) {
            throw new InvalidCredentialsException();
        }
        String token = jwtUtility.generateToken(patient.getId().toString(), "PATIENT");

        return new AuthResponse(true, token, "PATIENT", patient.getId(), patient.getLoginMethod().name());
    }

    public String registerDoctor(Doctor doctor) {
        if (doctorRepository.findByEmail(doctor.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Doctor", "email");
        }

        if (doctorRepository.findByUsername(doctor.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Doctor", "username");
        }

        if (doctorRepository.findByLicenseNumber(doctor.getLicense_number()).isPresent()) {
            throw new DuplicateResourceException("Doctor", "license_number");
        }

        doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));

        Doctor savedDoctor = doctorRepository.save(doctor);

        return jwtUtility.generateToken(savedDoctor.getId().toString(), "DOCTOR");
    }

    public AuthResponse loginDoctor(String email, String password) {
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", email));

        // Check if user is using TOTP only (not password or both)
        if (doctor.getLoginMethod() == LoginMethod.TOTP) {
            throw new LoginMethodMismatchException("This account uses authenticator login only. Please use TOTP code.");
        }

        if(!passwordEncoder.matches(password, doctor.getPassword())){
            throw new InvalidCredentialsException();
        }
        String token = jwtUtility.generateToken(doctor.getId().toString(), "DOCTOR");

        return new AuthResponse(true, token, "DOCTOR", doctor.getId(), doctor.getLoginMethod().name());
    }

    /**
     * Login patient with TOTP code
     */
    public AuthResponse loginPatientWithTotp(String email, String code) {
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", email));

        if (!patient.isTotpEnabled()) {
            throw new TotpNotEnabledException("TOTP is not enabled for this account");
        }

        if (!mfaService.verifyTotpCode(email, code, "PATIENT")) {
            throw new InvalidTotpCodeException("Invalid TOTP code");
        }

        String token = jwtUtility.generateToken(patient.getId().toString(), "PATIENT");
        return new AuthResponse(true, token, "PATIENT", patient.getId(), patient.getLoginMethod().name());
    }

    /**
     * Login doctor with TOTP code
     */
    public AuthResponse loginDoctorWithTotp(String email, String code) {
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", email));

        if (!doctor.isTotpEnabled()) {
            throw new TotpNotEnabledException("TOTP is not enabled for this account");
        }

        if (!mfaService.verifyTotpCode(email, code, "DOCTOR")) {
            throw new InvalidTotpCodeException("Invalid TOTP code");
        }

        String token = jwtUtility.generateToken(doctor.getId().toString(), "DOCTOR");
        return new AuthResponse(true, token, "DOCTOR", doctor.getId(), doctor.getLoginMethod().name());
    }
}
