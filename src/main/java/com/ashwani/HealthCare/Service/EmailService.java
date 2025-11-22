package com.ashwani.HealthCare.Service;

import com.ashwani.HealthCare.Entity.DoctorEntity;
import com.ashwani.HealthCare.Entity.PatientEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;


    private String generatePatientJoinLink(Long appointmentId) {
        return "https://yourclinic.com/session/" + appointmentId;

    }

    private String generateDoctorJoinLink(Long appointmentId) {
        return "https://yourclinic.com/session/" + appointmentId;
    }

    private void sendPatientEmail(DoctorEntity doctor, PatientEntity patient,
                                  LocalTime time, LocalDate date, String patientJoinLink) {
        try {
            Context context = new Context();
            context.setVariable("patientName", patient.getFull_name());
            context.setVariable("doctorName", doctor.getFull_name());
            context.setVariable("date", date);
            context.setVariable("time", time);
            context.setVariable("joinLink", patientJoinLink);
            context.setVariable("cancelLink", "https://yourclinic.com/cancel?token=12345");

            String htmlContent = templateEngine.process("email/patient-appointment", context);
            sendEmail(patient.getEmail(), "Appointment Confirmation", htmlContent);
        } catch (MessagingException e) {
            log.error("❌ Failed to send patient email to: {}", patient.getEmail(), e);
            throw new RuntimeException("Failed to send patient email", e);
        }
    }

    private void sendDoctorEmail(DoctorEntity doctor, PatientEntity patient,
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
            context.setVariable("cancelLink", "https://yourclinic.com/cancel?token=12345");

            String htmlContent = templateEngine.process("email/doctor-notification", context);
            sendEmail(doctor.getEmail(), "New Appointment Scheduled", htmlContent);
        } catch (MessagingException e) {
            log.error("❌ Failed to send patient email to: {}", doctor.getEmail(), e);
            throw new RuntimeException("Failed to send patient email", e);
        }
    }

    private void sendEmail(String to, String subject, String htmlContent)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            helper.setFrom("itsmeashwi786@gmail.com", "Thera-Connect");
        } catch (UnsupportedEncodingException e) {
            helper.setFrom("itsemashwi786@gmail.com");
        }
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = isHTML

        mailSender.send(message);
    }
    @Async
    public void sendAppointmentConfirmation(DoctorEntity doctor, PatientEntity patient, Long appointmentId, LocalTime appointmentTime, LocalDate date, String description){
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
}
