package com.ashwani.HealthCare.Service.Auth;

import com.ashwani.HealthCare.DTO.Authentication.PasswordResetDTO;
import com.ashwani.HealthCare.DTO.Authentication.PasswordResetRequestDTO;
import com.ashwani.HealthCare.Entity.Doctor;
import com.ashwani.HealthCare.Entity.PasswordResetToken;
import com.ashwani.HealthCare.Entity.Patient;
import com.ashwani.HealthCare.ExceptionHandlers.common.ResourceNotFoundException;
import com.ashwani.HealthCare.ExceptionHandlers.token.InvalidTokenException;
import com.ashwani.HealthCare.ExceptionHandlers.token.TokenAlreadyUsedException;
import com.ashwani.HealthCare.ExceptionHandlers.token.TokenExpiredException;
import com.ashwani.HealthCare.Repository.DoctorRepository;
import com.ashwani.HealthCare.Repository.PasswordResetTokenRepository;
import com.ashwani.HealthCare.Repository.PatientRepository;
import com.ashwani.HealthCare.Service.Communication.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final PasswordResetTokenRepository tokenRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Token expiry time in minutes
    @Value("${password.reset.token.expiry.minutes}")
    private int tokenExpiryMinutes;

    /**
     * Request password reset for a patient
     * @param request PasswordResetRequestDTO containing email
     * @return Success message
     */
    @Transactional
    public String requestPatientPasswordReset(PasswordResetRequestDTO request) {
        String email = request.getEmail();

        // Check if patient exists
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", email));

        // Delete any existing tokens for this patient
        tokenRepository.deleteByEmailAndUserType(email, "PATIENT");

        // Generate new token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(tokenExpiryMinutes);

        // Save token
        PasswordResetToken resetToken = new PasswordResetToken(token, email, "PATIENT", expiryDate);
        tokenRepository.save(resetToken);

        // Send email
        emailService.sendPasswordResetEmail(email, patient.getFull_name(), "Patient", token);

        log.info("✅ Password reset token generated for patient: {}", email);
        return "Password reset link has been sent to your email address";
    }

    /**
     * Request password reset for a doctor
     * @param request PasswordResetRequestDTO containing email
     * @return Success message
     */
    @Transactional
    public String requestDoctorPasswordReset(PasswordResetRequestDTO request) {
        String email = request.getEmail();

        // Check if doctor exists
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", email));

        // Delete any existing tokens for this doctor
        tokenRepository.deleteByEmailAndUserType(email, "DOCTOR");

        // Generate new token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(tokenExpiryMinutes);

        // Save token
        PasswordResetToken resetToken = new PasswordResetToken(token, email, "DOCTOR", expiryDate);
        tokenRepository.save(resetToken);

        // Send email
        emailService.sendPasswordResetEmail(email, doctor.getFull_name(), "Doctor", token);

        log.info("✅ Password reset token generated for doctor: {}", email);
        return "Password reset link has been sent to your email address";
    }

    /**
     * Reset password using token
     * @param resetDTO PasswordResetDTO containing token and new password
     * @return Success message
     */
    @Transactional
    public String resetPassword(PasswordResetDTO resetDTO) {
        String token = resetDTO.getToken();
        String newPassword = resetDTO.getNewPassword();

        // Find token
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token", "PASSWORD_RESET"));

        // Validate token
        if (resetToken.isUsed()) {
            throw new TokenAlreadyUsedException("This reset token has already been used", "PASSWORD_RESET");
        }

        if (resetToken.isExpired()) {
            throw new TokenExpiredException("This reset token has expired. Please request a new one", "PASSWORD_RESET");
        }

        // Update password based on user type
        if ("PATIENT".equals(resetToken.getUserType())) {
            Patient patient = patientRepository.findByEmail(resetToken.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient", resetToken.getEmail()));

            patient.setPassword(passwordEncoder.encode(newPassword));
            patientRepository.save(patient);
            log.info("✅ Password reset successful for patient: {}", resetToken.getEmail());
        } else if ("DOCTOR".equals(resetToken.getUserType())) {
            Doctor doctor = doctorRepository.findByEmail(resetToken.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor", resetToken.getEmail()));

            doctor.setPassword(passwordEncoder.encode(newPassword));
            doctorRepository.save(doctor);
            log.info("✅ Password reset successful for doctor: {}", resetToken.getEmail());
        } else {
            throw new IllegalArgumentException("Invalid user type: " + resetToken.getUserType());
        }

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return "Password has been reset successfully. You can now login with your new password";
    }

    /**
     * Clean up expired tokens - runs every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("🧹 Running cleanup of expired password reset tokens");
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("✅ Expired tokens cleanup completed");
    }
}
