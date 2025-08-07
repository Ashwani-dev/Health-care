package com.ashwani.HealthCare.Service;

import com.ashwani.HealthCare.DTO.Appointments.PatientAppointmentResponse;
import com.ashwani.HealthCare.DTO.VideoSession.VideoSession;
import com.ashwani.HealthCare.Entity.AppointmentEntity;
import com.ashwani.HealthCare.Entity.DoctorAvailability;
import com.ashwani.HealthCare.Entity.DoctorEntity;
import com.ashwani.HealthCare.Entity.PatientEntity;
import com.ashwani.HealthCare.Repository.AppointmentRepository;
import com.ashwani.HealthCare.Repository.DoctorAvailabilityRepository;
import com.ashwani.HealthCare.Repository.DoctorRepository;
import com.ashwani.HealthCare.Repository.PatientRepository;
import com.ashwani.HealthCare.Utility.TimeSlot;
import com.ashwani.HealthCare.specifications.AppointmentSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
                appointment.getId(),
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
                        !startTime.isBefore(av.getStartTime()) &&
                        !startTime.isAfter(av.getEndTime()));

        if (!isAvailable) {
            throw new Exception("Doctor is not available at this time");
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
        appointmentRepository.save(appointment);

        emailService.sendAppointmentConfirmation(
                doctor,
                patient,
                appointment.getId(),
                startTime,
                date,
                description
        );

        return appointment;
    }

    @Transactional
    public Page<PatientAppointmentResponse> getPatientAppointments(
            Long patientId,
            LocalDate appointmentDate,
            LocalTime startTime,
            String status,
            Pageable pageable) {

        if (!patientRepository.existsById(patientId)) {
            throw new RuntimeException("Patient not found with ID: " + patientId);
        }

        // Build specifications
        Specification<AppointmentEntity> spec = Specification.where(AppointmentSpecifications.hasPatient(patientId))
                .and(AppointmentSpecifications.hasAppointmentDate(appointmentDate))
                .and(AppointmentSpecifications.hasStartTime(startTime))
                .and(AppointmentSpecifications.hasStatus(status));

        // Get paginated results
        Page<AppointmentEntity> appointmentPage = appointmentRepository.findAll(spec, pageable);

        // Process status updates
        List<AppointmentEntity> updatedAppointments = appointmentPage.getContent().stream()
                .filter(apt -> apt.getStatus().equals("SCHEDULED") &&
                        (apt.getAppointmentDate().isBefore(LocalDate.now()) ||
                                (apt.getAppointmentDate().isEqual(LocalDate.now()) &&
                                        apt.getEndTime().isBefore(LocalTime.now()))))
                .peek(apt -> {
                    apt.setStatus("COMPLETED");
                    appointmentRepository.save(apt);
                })
                .toList();

        return appointmentPage.map(this::convertToResponse);
    }

    public Page<PatientAppointmentResponse> getDoctorAppointments(
            Long doctorId,
            LocalDate appointmentDate,
            LocalTime startTime,
            String status,
            Pageable pageable) {

        // Verify doctor exists using existsById (more efficient than findById)
        if (!doctorRepository.existsById(doctorId)) {
            throw new RuntimeException(
                    "Doctor not found with ID: " + doctorId
            );
        }

        // Combine all specifications
        Specification<AppointmentEntity> spec = Specification.where(AppointmentSpecifications.hasDoctor(doctorId))
                .and(AppointmentSpecifications.hasAppointmentDate(appointmentDate))
                .and(AppointmentSpecifications.hasStartTime(startTime))
                .and(AppointmentSpecifications.hasStatus(status));

        // Get paginated results
        Page<AppointmentEntity> appointmentPage = appointmentRepository.findAll(spec, pageable);

        return appointmentPage.map(this::convertToResponse);
    }

    public List<TimeSlot> getAvailableSlots(Long doctorId, LocalDate date) {
        // 1. Early validation
        DoctorEntity doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // 2. Batch fetch data
        List<DoctorAvailability> availabilities = doctorAvailabilityRepository
                .findByDoctorAndDayOfWeek(doctor, date.getDayOfWeek());

        Set<LocalTime> bookedSlots = appointmentRepository
                .findByDoctorAndAppointmentDate(doctor, date)
                .stream()
                .map(AppointmentEntity::getStartTime)
                .collect(Collectors.toSet());

        // 3. Process slots efficiently
        List<TimeSlot> availableSlots = new ArrayList<>();
        final int MAX_SLOTS = 100; // Safety limit

        for (DoctorAvailability availability : availabilities) {
            if (!availability.getIsAvailable()) continue;

            LocalTime current = availability.getStartTime();
            LocalTime endTime = availability.getEndTime();

            while (current.isBefore(endTime) && availableSlots.size() < MAX_SLOTS) {
                if (!bookedSlots.contains(current)) {
                    availableSlots.add(new TimeSlot(current, current.plusMinutes(30)));
                }
                current = current.plusMinutes(30);
            }
        }
        return availableSlots;
    }

    @Transactional
    public void cancelAppointment(Long appointmentId, Long userId) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        vaildateCancellation(appointment, userId);
        appointment.cancel(userId);
        appointmentRepository.save(appointment);
    }

    private void vaildateCancellation(AppointmentEntity appointment, Long userId) {
        if (!appointment.belongsToPatient(userId)){
            throw new AccessDeniedException("Not your appointment");
        }
        if (appointment.isPastCancellationDeadline()){
            throw new RuntimeException("24-hour cancellation required");
        }
    }
}
