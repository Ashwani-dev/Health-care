package com.ashwani.HealthCare.Service;

import com.ashwani.HealthCare.Entity.DoctorEntity;
import com.ashwani.HealthCare.Entity.PatientEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private void sendPatientEmail(DoctorEntity doctor, PatientEntity patient, LocalTime time, LocalDate date) {
        String subject = "Appointment Confirmation";
        String text = String.format("Dear %s,\n\nYour appointment with Dr. %s is scheduled at %s on %s",
                patient.getFull_name(), doctor.getFull_name(), time, date);
        sendEmail(patient.getEmail(), subject, text);
    }

    private void sendDoctorEmail(DoctorEntity doctor, PatientEntity patient, LocalTime time, LocalDate date) {
        String subject = "New Appointment";
        String text = String.format("Dr. %s,\n\nNew appointment with %s at %s on %s\nPatient email: %s",
                doctor.getFull_name(), patient.getFull_name(), time, date, patient.getEmail());
        sendEmail(doctor.getEmail(), subject, text);
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    @Async
    public void sendAppointmentConfirmation(DoctorEntity doctor, PatientEntity patient, LocalTime appointmentTime, LocalDate date){
        sendPatientEmail(doctor, patient, appointmentTime, date);
        sendDoctorEmail(doctor, patient, appointmentTime, date);
    }
}
