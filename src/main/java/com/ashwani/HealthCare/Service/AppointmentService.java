package com.ashwani.HealthCare.Service;

import com.ashwani.HealthCare.DTO.PatientAppointmentResponse;
import com.ashwani.HealthCare.Entity.AppointmentEntity;
import com.ashwani.HealthCare.Entity.DoctorAvailability;
import com.ashwani.HealthCare.Entity.DoctorEntity;
import com.ashwani.HealthCare.Entity.PatientEntity;
import com.ashwani.HealthCare.Repository.AppointmentRepository;
import com.ashwani.HealthCare.Repository.DoctorAvailabilityRepository;
import com.ashwani.HealthCare.Repository.DoctorRepository;
import com.ashwani.HealthCare.Repository.PatientRepository;
import com.ashwani.HealthCare.Utility.TimeSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private EmailService emailService;

    private PatientAppointmentResponse convertToResponse(AppointmentEntity appointment) {
        return new PatientAppointmentResponse(
                appointment.getPatient().getId(),
                appointment.getPatient().getFull_name(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getFull_name(),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus(),
                appointment.getDescription(),
                appointment.getCreatedAt()
        );
    }

    @Transactional
    public AppointmentEntity bookAppointment(Long patientId, Long doctorId, LocalDate date,
                                             LocalTime startTime, String description) throws Exception {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient Not Found"));

        DoctorEntity doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor Not Found"));

        // Check if slot is available
        if (appointmentRepository.existsByDoctorAndAppointmentDateAndStartTime(doctor, date, startTime)) {
            throw new Exception("Time slot already booked");
        }

        // Check if slot is within doctor's availability
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorAvailability> availabilities = doctorAvailabilityRepository.findByDoctorAndDayOfWeek(doctor, dayOfWeek);

        boolean isAvailable = availabilities.stream()
                .anyMatch(av -> av.getIsAvailable() &&
                        startTime.isAfter(av.getStartTime()) &&
                        startTime.isBefore(av.getEndTime()));

        if (!isAvailable) {
            throw new Exception("Doctor is not at this time");
        }

        // Default duration - could be parameterized
        LocalTime endTime = startTime.plusMinutes(30);

        AppointmentEntity appointment = new AppointmentEntity();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(date);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus("SCHEDULED");
        appointment.setDescription(description);

        emailService.sendAppointmentConfirmation(doctor, patient, startTime, date);

        return appointmentRepository.save(appointment);
    }

    public List<PatientAppointmentResponse> getPatientAppointments(Long patientId) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        List<AppointmentEntity> appointments = appointmentRepository.findByPatient(patient);
        return appointments.stream()
                .map(this ::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<PatientAppointmentResponse> getDoctorAppointments(Long doctorId, LocalDate date) {
        DoctorEntity doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        List<AppointmentEntity> appointments = appointmentRepository.findByDoctorAndAppointmentDate(doctor, date);
        return appointments.stream()
                .map(this ::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<TimeSlot> getAvailableSlots(Long doctorId, LocalDate date) {
        DoctorEntity doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorAvailability> availabilities = doctorAvailabilityRepository
                .findByDoctorAndDayOfWeek(doctor, dayOfWeek);

        List<AppointmentEntity> existingAppointments = appointmentRepository
                .findByDoctorAndAppointmentDate(doctor, date);

        List<TimeSlot> availableSlots = new ArrayList<>();

        for (DoctorAvailability availability : availabilities) {
            if (availability.getIsAvailable()) {
                LocalTime current = availability.getStartTime();
                while (current.plusMinutes(30).isBefore(availability.getEndTime()) ||
                        current.plusMinutes(30).isAfter(availability.getStartTime())) {
                    LocalTime finalCurrent = current;
                    boolean isBooked = existingAppointments.stream()
                            .anyMatch(app -> app.getStartTime().equals(finalCurrent));

                    if (!isBooked) {
                        availableSlots.add(new TimeSlot(current, current.plusMinutes(30)));
                    }

                    current = current.plusMinutes(30);
                }
            }
        }
        return availableSlots;
    }
}
