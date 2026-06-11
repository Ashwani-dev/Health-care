package com.ashwani.HealthCare.Service.Communication;

import com.ashwani.HealthCare.Entity.Doctor;
import com.ashwani.HealthCare.Entity.Patient;
import com.ashwani.HealthCare.ExceptionHandlers.communication.EmailSendingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final SesClient sesClient;
    private final TemplateEngine templateEngine;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${mail.from.do-not-reply}")
    private String doNotReplyEmail;

    @Value("${mail.support}")
    private String mailSupport;

    private String generatePatientJoinLink(Long appointmentId) {
        return frontendUrl + "/video-preview/" + appointmentId + "/PATIENT";
    }

    private String generateDoctorJoinLink(Long appointmentId) {
        return frontendUrl + "/video-preview/" + appointmentId + "/DOCTOR";
    }

    private void sendPatientEmail(Doctor doctor, Patient patient,
                                  LocalTime time, LocalDate date, String patientJoinLink) {
        try {
            Context context = new Context();
            context.setVariable("patientName", patient.getFull_name());
            context.setVariable("doctorName", doctor.getFull_name());
            context.setVariable("date", date);
            context.setVariable("time", time);
            context.setVariable("joinLink", patientJoinLink);

            String htmlContent = templateEngine.process("email/patient-appointment", context);
            sendEmail(patient.getEmail(), "Appointment Confirmation", htmlContent);
        } catch (Exception e) {
            log.error("❌ Failed to send patient email to: {}", patient.getEmail(), e);
            throw new EmailSendingException("Failed to send appointment confirmation to patient", patient.getEmail(), e);
        }
    }

    private void sendDoctorEmail(Doctor doctor, Patient patient,
                                 LocalTime time, LocalDate date, String description, String doctorJoinLink) {
        try {
            Context context = new Context();
            context.setVariable("doctorName", doctor.getFull_name());
            context.setVariable("patientName", patient.getFull_name());
            context.setVariable("patientEmail", patient.getEmail());
            context.setVariable("date", date);
            context.setVariable("time", time);
            context.setVariable("description", description);
            context.setVariable("joinLink", doctorJoinLink);

            String htmlContent = templateEngine.process("email/doctor-notification", context);
            sendEmail(doctor.getEmail(), "New Appointment Scheduled", htmlContent);
        } catch (Exception e) {
            log.error("❌ Failed to send doctor email to: {}", doctor.getEmail(), e);
            throw new EmailSendingException("Failed to send appointment notification to doctor", doctor.getEmail(), e);
        }
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .destination(Destination.builder().toAddresses(to).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).build())
                            .body(Body.builder()
                                    .html(Content.builder().data(htmlContent).build())
                                    .build())
                            .build())
                    .source(doNotReplyEmail)
                    .build();

            sesClient.sendEmail(request);
            log.info("Successfully sent email to: {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send email to: {}", to, e);
            throw new EmailSendingException("Failed to send email", to, e);
        }
    }

    @Async
    public void sendAppointmentConfirmation(Doctor doctor, Patient patient, Long appointmentId, LocalTime appointmentTime, LocalDate date, String description){
        String doctorEmail = doctor.getEmail();
        String patientEmail = patient.getEmail();
        log.info("📧 Sending appointment confirmation to: {} and {}", doctorEmail, patientEmail);
        try {
            String patientJoinLink = generatePatientJoinLink(appointmentId);
            String doctorJoinLink = generateDoctorJoinLink(appointmentId);
            sendPatientEmail(doctor, patient, appointmentTime, date, patientJoinLink);
            sendDoctorEmail(doctor, patient, appointmentTime, date, description, doctorJoinLink);
            log.info("✅ SUCCESS: All emails sent for appointment: {}", appointmentId);
        } catch (Exception e) {
            log.error("❌ COMPLETE FAILURE: Email sending failed for appointment: {}", appointmentId, e);
        }
    }

    /**
     * Send password reset email to user
     * @param email User's email address
     * @param userName User's full name
     * @param userType Type of user (PATIENT or DOCTOR)
     * @param resetToken Password reset token
     */
    @Async
    public void sendPasswordResetEmail(String email, String userName, String userType, String resetToken) {
        log.info("📧 Sending password reset email to: {}", email);
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("userType", userType);
            context.setVariable("resetLink", frontendUrl + "/reset-password?token=" + resetToken);

            String htmlContent = templateEngine.process("email/password-reset", context);
            sendEmail(email, "Password Reset Request - HealthCare", htmlContent);
            log.info("✅ SUCCESS: Password reset email sent to: {}", email);
        } catch (Exception e) {
            log.error("❌ FAILURE: Password reset email failed for: {}", email, e);
            throw new EmailSendingException("Failed to send password reset email", email, e);
        }
    }

    /**
     * Send support/contact request email to the support inbox
     * @param name User's name
     * @param fromEmail User's email address
     * @param subject Message subject
     * @param messageContent Message content
     */
    @Async
    public void sendSupportMessageEmail(String name, String fromEmail, String subject, String messageContent) {
        log.info("📧 Sending support message email from: {} - Subject: {}", fromEmail, subject);
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("email", fromEmail);
            context.setVariable("subject", subject);
            context.setVariable("message", messageContent);

            String htmlContent = templateEngine.process("email/support-message", context);
            String recipient = (mailSupport != null && !mailSupport.isBlank()) ? mailSupport : "support@theraconnect.app";

            SendEmailRequest request = SendEmailRequest.builder()
                    .destination(Destination.builder().toAddresses(recipient).build())
                    .message(Message.builder()
                            .subject(Content.builder().data("Support Request: " + subject).build())
                            .body(Body.builder()
                                    .html(Content.builder().data(htmlContent).build())
                                    .build())
                            .build())
                    .replyToAddresses(fromEmail)
                    .source(doNotReplyEmail)
                    .build();

            sesClient.sendEmail(request);
            log.info("✅ SUCCESS: Support message email sent successfully to: {}", recipient);
        } catch (Exception e) {
            log.error("❌ FAILURE: Support message email failed from: {}", fromEmail, e);
            throw new EmailSendingException("Failed to send support message email", fromEmail, e);
        }
    }
}
