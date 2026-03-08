package com.ashwani.HealthCare.ExceptionHandlers;

import com.ashwani.HealthCare.ExceptionHandlers.appointment.AppointmentCancellationException;
import com.ashwani.HealthCare.ExceptionHandlers.appointment.InvalidAppointmentStateException;
import com.ashwani.HealthCare.ExceptionHandlers.appointment.SlotNotAvailableException;
import com.ashwani.HealthCare.ExceptionHandlers.auth.*;
import com.ashwani.HealthCare.ExceptionHandlers.common.DuplicateResourceException;
import com.ashwani.HealthCare.ExceptionHandlers.common.ResourceNotFoundException;
import com.ashwani.HealthCare.ExceptionHandlers.communication.EmailSendingException;
import com.ashwani.HealthCare.ExceptionHandlers.communication.QrCodeGenerationException;
import com.ashwani.HealthCare.ExceptionHandlers.communication.VideoCallException;
import com.ashwani.HealthCare.ExceptionHandlers.payment.PaymentException;
import com.ashwani.HealthCare.ExceptionHandlers.payment.WebhookValidationException;
import com.ashwani.HealthCare.ExceptionHandlers.token.InvalidTokenException;
import com.ashwani.HealthCare.ExceptionHandlers.token.TokenAlreadyUsedException;
import com.ashwani.HealthCare.ExceptionHandlers.token.TokenExpiredException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // =========================
    // VALIDATION EXCEPTIONS
    // =========================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "VALIDATION_ERROR");
        response.put("message", "Invalid input data");
        response.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage());
    }

    // =========================
    // RESOURCE EXCEPTIONS
    // =========================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResource(DuplicateResourceException ex) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", ex.getMessage());
    }

    // =========================
    // AUTHENTICATION & AUTHORIZATION
    // =========================

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Invalid credentials attempt");
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        log.warn("Unauthorized access: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "UNAUTHORIZED_ACCESS", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage());
    }

    // =========================
    // TOTP/MFA EXCEPTIONS
    // =========================

    @ExceptionHandler(TotpNotEnabledException.class)
    public ResponseEntity<Map<String, Object>> handleTotpNotEnabled(TotpNotEnabledException ex) {
        log.warn("TOTP not enabled: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "TOTP_NOT_ENABLED", ex.getMessage());
    }

    @ExceptionHandler(TotpAlreadyEnabledException.class)
    public ResponseEntity<Map<String, Object>> handleTotpAlreadyEnabled(TotpAlreadyEnabledException ex) {
        log.warn("TOTP already enabled: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "TOTP_ALREADY_ENABLED", ex.getMessage());
    }

    @ExceptionHandler(InvalidTotpCodeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTotpCode(InvalidTotpCodeException ex) {
        log.warn("Invalid TOTP code attempt");
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_TOTP_CODE", ex.getMessage());
    }

    @ExceptionHandler(LoginMethodMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleLoginMethodMismatch(LoginMethodMismatchException ex) {
        log.warn("Login method mismatch: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "LOGIN_METHOD_MISMATCH", ex.getMessage());
    }

    @ExceptionHandler(QrCodeGenerationException.class)
    public ResponseEntity<Map<String, Object>> handleQrCodeGeneration(QrCodeGenerationException ex) {
        log.error("QR code generation failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "QR_CODE_GENERATION_FAILED",
                "Failed to generate QR code. Please try again.");
    }

    // =========================
    // TOKEN EXCEPTIONS
    // =========================

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidToken(InvalidTokenException ex) {
        log.warn("Invalid token: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", ex.getMessage());
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleTokenExpired(TokenExpiredException ex) {
        log.warn("Token expired: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.GONE, "TOKEN_EXPIRED", ex.getMessage());
    }

    @ExceptionHandler(TokenAlreadyUsedException.class)
    public ResponseEntity<Map<String, Object>> handleTokenAlreadyUsed(TokenAlreadyUsedException ex) {
        log.warn("Token already used: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "TOKEN_ALREADY_USED", ex.getMessage());
    }

    // =========================
    // APPOINTMENT EXCEPTIONS
    // =========================

    @ExceptionHandler(SlotNotAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleSlotNotAvailable(SlotNotAvailableException ex) {
        log.warn("Slot not available: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "SLOT_NOT_AVAILABLE", ex.getMessage());
    }

    @ExceptionHandler(AppointmentCancellationException.class)
    public ResponseEntity<Map<String, Object>> handleAppointmentCancellation(AppointmentCancellationException ex) {
        log.warn("Appointment cancellation failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "APPOINTMENT_CANCELLATION_FAILED", ex.getMessage());
    }

    @ExceptionHandler(InvalidAppointmentStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAppointmentState(InvalidAppointmentStateException ex) {
        log.warn("Invalid appointment state: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_APPOINTMENT_STATE", ex.getMessage());
    }

    // =========================
    // PAYMENT EXCEPTIONS
    // =========================

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentException(PaymentException ex) {
        log.error("Payment error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "PAYMENT_ERROR", ex.getMessage());
    }

    @ExceptionHandler(WebhookValidationException.class)
    public ResponseEntity<Map<String, Object>> handleWebhookValidation(WebhookValidationException ex) {
        log.error("Webhook validation failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "WEBHOOK_VALIDATION_FAILED", ex.getMessage());
    }

    // =========================
    // COMMUNICATION EXCEPTIONS
    // =========================

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<Map<String, Object>> handleEmailSending(EmailSendingException ex) {
        log.error("Email sending failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_SENDING_FAILED",
                "Failed to send email. Please try again later.");
    }

    @ExceptionHandler(VideoCallException.class)
    public ResponseEntity<Map<String, Object>> handleVideoCall(VideoCallException ex) {
        log.error("Video call error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "VIDEO_CALL_ERROR", ex.getMessage());
    }

    // =========================
    // SPRING FRAMEWORK EXCEPTIONS
    // =========================

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        log.warn("Response status exception: {}", ex.getReason());
        return buildErrorResponse(
                (HttpStatus) ex.getStatusCode(),
                "ERROR",
                ex.getReason() != null ? ex.getReason() : ex.getMessage()
        );
    }

    // =========================
    // GENERIC EXCEPTIONS
    // =========================

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "ILLEGAL_STATE", ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please contact support.");
    }

    // =========================
    // HELPER METHOD
    // =========================

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String errorCode, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", errorCode);
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}
