package com.ashwani.HealthCare.Service;

import com.ashwani.HealthCare.Entity.UserEntity;
import com.ashwani.HealthCare.Repository.UserRepository;
import com.ashwani.HealthCare.Utility.JWTUtility;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AuthService {
    private final UserRepository userRepository;
    private final JWTUtility jwtUtility;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JWTUtility jwtUtility) {
        this.userRepository = userRepository;
        this.jwtUtility = jwtUtility;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String register(UserEntity user) {
        // Check if user exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        // Hash password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save user
        userRepository.save(user);

        // Generate token
        return jwtUtility.generateToken(user.getId().toString());
    }

    public String login(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtUtility.generateToken(user.getId().toString());
    }
}
