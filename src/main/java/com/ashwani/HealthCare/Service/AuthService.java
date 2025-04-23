package com.ashwani.HealthCare.Service;

import com.ashwani.HealthCare.Entity.DoctorEntity;
import com.ashwani.HealthCare.Entity.UserEntity;
import com.ashwani.HealthCare.Repository.DoctorRepository;
import com.ashwani.HealthCare.Repository.UserRepository;
import com.ashwani.HealthCare.Utility.JWTUtility;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final JWTUtility jwtUtility;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, DoctorRepository doctorRepository, JWTUtility jwtUtility) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.jwtUtility = jwtUtility;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String registerUser(UserEntity user) {
        // Check if user exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username does already exist");
        }

        // Hash password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save user
        userRepository.save(user);

        // Generate token
        return jwtUtility.generateToken(user.getId().toString());
    }


    public String loginUser(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return jwtUtility.generateToken(user.getId().toString());
    }

    public String registerDoctor(DoctorEntity doctor) {
        if (doctorRepository.findByEmail(doctor.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        if (doctorRepository.findByUsername(doctor.getUsername()).isPresent()) {
            throw new RuntimeException("Username does already exist");
        }
        doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
        doctorRepository.save(doctor);
        return jwtUtility.generateToken(doctor.getId().toString());
    }

    public String loginDoctor(String email, String password) {
        DoctorEntity doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(!passwordEncoder.matches(password, doctor.getPassword())){
            throw new RuntimeException("Invalid Credentials");
        }
        return jwtUtility.generateToken(doctor.getId().toString());
    }
}
