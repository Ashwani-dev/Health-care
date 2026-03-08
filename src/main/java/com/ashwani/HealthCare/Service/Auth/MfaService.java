package com.ashwani.HealthCare.Service.Auth;

import com.ashwani.HealthCare.DTO.Authentication.MfaResponse;
import com.ashwani.HealthCare.DTO.Authentication.TotpSetupResponse;
import com.ashwani.HealthCare.Entity.Doctor;
import com.ashwani.HealthCare.Entity.Patient;
import com.ashwani.HealthCare.Enums.LoginMethod;
import com.ashwani.HealthCare.ExceptionHandlers.auth.InvalidTotpCodeException;
import com.ashwani.HealthCare.ExceptionHandlers.auth.TotpAlreadyEnabledException;
import com.ashwani.HealthCare.ExceptionHandlers.auth.TotpNotEnabledException;
import com.ashwani.HealthCare.ExceptionHandlers.common.ResourceNotFoundException;
import com.ashwani.HealthCare.ExceptionHandlers.communication.QrCodeGenerationException;
import com.ashwani.HealthCare.Repository.DoctorRepository;
import com.ashwani.HealthCare.Repository.PatientRepository;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.stereotype.Service;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Service
public class MfaService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    private final SecretGenerator secretGenerator;
    private final QrGenerator qrGenerator;
    private final CodeVerifier codeVerifier;

    private static final String ISSUER = "TheraConnect";

    public MfaService(PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;

        // Initialize TOTP components
        this.secretGenerator = new DefaultSecretGenerator();
        this.qrGenerator = new ZxingPngQrGenerator();

        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        this.codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    }

    /**
     * Setup TOTP for a user by userId - generates secret and QR code
     * @param userId User's ID
     * @param userType "PATIENT" or "DOCTOR"
     * @return TotpSetupResponse with QR code and secret
     */
    public TotpSetupResponse setupTotpByUserId(Long userId, String userType) {
        String email;
        boolean totpEnabled;

        if ("PATIENT".equals(userType)) {
            Patient patient = patientRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient", userId));
            email = patient.getEmail();
            totpEnabled = patient.isTotpEnabled();
        } else {
            Doctor doctor = doctorRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor", userId));
            email = doctor.getEmail();
            totpEnabled = doctor.isTotpEnabled();
        }

        if (totpEnabled) {
            throw new TotpAlreadyEnabledException("TOTP is already enabled for this account");
        }

        // Generate secret
        String secret = secretGenerator.generate();

        // Generate QR code
        QrData data = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer(ISSUER)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        try {
            byte[] imageData = qrGenerator.generate(data);
            String qrCodeImage = getDataUriForImage(imageData, qrGenerator.getImageMimeType());

            return new TotpSetupResponse(qrCodeImage, secret);
        } catch (QrGenerationException e) {
            throw new QrCodeGenerationException("Failed to generate QR code", e);
        }
    }

    /**
     * Confirm TOTP setup by userId - verifies code and enables TOTP
     * @param userId User's ID
     * @param secret TOTP secret
     * @param code 6-digit TOTP code
     * @param userType "PATIENT" or "DOCTOR"
     * @return MfaResponse with success message
     */
    public MfaResponse confirmTotpByUserId(Long userId, String secret, String code, String userType) {
        // Verify the code
        if (!codeVerifier.isValidCode(secret, code)) {
            throw new InvalidTotpCodeException("Invalid TOTP code");
        }

        // Save secret and enable TOTP - set login method to BOTH
        if ("PATIENT".equals(userType)) {
            Patient patient = patientRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient", userId));

            patient.setTotpSecret(secret);
            patient.setTotpEnabled(true);
            patient.setLoginMethod(LoginMethod.BOTH);  // Can login with password OR TOTP
            patientRepository.save(patient);

            return new MfaResponse("TOTP enabled successfully", LoginMethod.BOTH.name());
        } else {
            Doctor doctor = doctorRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor", userId));

            doctor.setTotpSecret(secret);
            doctor.setTotpEnabled(true);
            doctor.setLoginMethod(LoginMethod.BOTH);  // Can login with password OR TOTP
            doctorRepository.save(doctor);

            return new MfaResponse("TOTP enabled successfully", LoginMethod.BOTH.name());
        }
    }

    /**
     * Disable TOTP by userId
     * @param userId User's ID
     * @param userType "PATIENT" or "DOCTOR"
     * @return MfaResponse with success message
     */
    public MfaResponse disableTotpByUserId(Long userId, String userType) {
        if ("PATIENT".equals(userType)) {
            Patient patient = patientRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient", userId));

            if (!patient.isTotpEnabled()) {
                throw new TotpNotEnabledException("TOTP is not enabled for this account");
            }

            patient.setTotpSecret(null);
            patient.setTotpEnabled(false);
            patient.setLoginMethod(LoginMethod.PASSWORD);
            patientRepository.save(patient);

            return new MfaResponse("TOTP disabled successfully", LoginMethod.PASSWORD.name());
        } else {
            Doctor doctor = doctorRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor", userId));

            if (!doctor.isTotpEnabled()) {
                throw new TotpNotEnabledException("TOTP is not enabled for this account");
            }

            doctor.setTotpSecret(null);
            doctor.setTotpEnabled(false);
            doctor.setLoginMethod(LoginMethod.PASSWORD);
            doctorRepository.save(doctor);


            return new MfaResponse("TOTP disabled successfully", LoginMethod.PASSWORD.name());
        }
    }

    /**
     * Verify TOTP code for login
     * @param email User's email
     * @param code 6-digit TOTP code
     * @param userType "PATIENT" or "DOCTOR"
     * @return true if code is valid
     */
    public boolean verifyTotpCode(String email, String code, String userType) {
        String secret;
        boolean totpEnabled;

        if ("PATIENT".equals(userType)) {
            Patient patient = patientRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient", email));
            secret = patient.getTotpSecret();
            totpEnabled = patient.isTotpEnabled();
        } else {
            Doctor doctor = doctorRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor", email));
            secret = doctor.getTotpSecret();
            totpEnabled = doctor.isTotpEnabled();
        }

        if (!totpEnabled || secret == null) {
            throw new TotpNotEnabledException("TOTP is not enabled for this account");
        }

        return codeVerifier.isValidCode(secret, code);
    }
}
