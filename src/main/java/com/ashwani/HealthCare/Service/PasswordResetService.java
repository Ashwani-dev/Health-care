package com.ashwani.HealthCare.Service;

import com.ashwani.HealthCare.DTO.Authentication.PasswordResetDTO;
import com.ashwani.HealthCare.DTO.Authentication.PasswordResetRequestDTO;
import com.ashwani.HealthCare.Entity.DoctorEntity;
import com.ashwani.HealthCare.Entity.PasswordResetTokenEntity;
import com.ashwani.HealthCare.Entity.PatientEntity;
import com.ashwani.HealthCare.Repository.DoctorRepository;
import com.ashwani.HealthCare.Repository.PasswordResetTokenRepository;
import com.ashwani.HealthCare.Repository.PatientRepository;
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
        PatientEntity patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email address"));

        // Delete any existing tokens for this patient
        tokenRepository.deleteByEmailAndUserType(email, "PATIENT");

        // Generate new token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(tokenExpiryMinutes);

        // Save token
        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity(token, email, "PATIENT", expiryDate);
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
        DoctorEntity doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email address"));

        // Delete any existing tokens for this doctor
        tokenRepository.deleteByEmailAndUserType(email, "DOCTOR");

        // Generate new token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(tokenExpiryMinutes);

        // Save token
        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity(token, email, "DOCTOR", expiryDate);
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
        PasswordResetTokenEntity resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        // Validate token
        if (resetToken.isUsed()) {
            throw new RuntimeException("This reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new RuntimeException("This reset token has expired. Please request a new one");
        }

        // Update password based on user type
        if ("PATIENT".equals(resetToken.getUserType())) {
            PatientEntity patient = patientRepository.findByEmail(resetToken.getEmail())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));

            patient.setPassword(passwordEncoder.encode(newPassword));
            patientRepository.save(patient);
            log.info("✅ Password reset successful for patient: {}", resetToken.getEmail());
        } else if ("DOCTOR".equals(resetToken.getUserType())) {
            DoctorEntity doctor = doctorRepository.findByEmail(resetToken.getEmail())
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            doctor.setPassword(passwordEncoder.encode(newPassword));
            doctorRepository.save(doctor);
            log.info("✅ Password reset successful for doctor: {}", resetToken.getEmail());
        } else {
            throw new RuntimeException("Invalid user type");
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
