package com.ashwani.HealthCare.Service;

import com.ashwani.HealthCare.DTO.Appointments.PatientAppointmentResponse;
import com.ashwani.HealthCare.Entity.*;
import com.ashwani.HealthCare.Repository.*;
import com.ashwani.HealthCare.Utility.TimeSlot;
import com.ashwani.HealthCare.specifications.AppointmentSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final EmailService emailService;
    private final AppointmentHoldRepository appointmentHoldRepository;
    private final PaymentRepository paymentRepository;


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

    public String createAppointmentHold(Long patientId, Long doctorId, LocalDate date,
                                        LocalTime startTime, String description) throws Exception {

        // Validate availability first
        DoctorEntity doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor Not Found"));

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

        // Create and save the hold entity
        AppointmentHold hold = new AppointmentHold();
        hold.setPatientId(patientId);
        hold.setDoctorId(doctorId);
        hold.setDate(date);
        hold.setStartTime(startTime);
        hold.setReason(description);
        hold.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        // The @PrePersist will generate the holdReference automatically
        AppointmentHold savedHold = appointmentHoldRepository.save(hold);

        log.info("Creating appointment hold for patient: {}, doctor: {}, at: {} {}",
                patientId, doctorId, date, startTime);

        // Return the readable reference (e.g., "hold_a1b2c3d4")
        return savedHold.getHoldReference();
    }

    @Transactional
    public AppointmentEntity bookAppointment(Long patientId, Long doctorId, LocalDate date,
                                             LocalTime startTime, String description, Long paymentId) throws Exception {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient Not Found"));

        DoctorEntity doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor Not Found"));

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment is not found"));

        if(!Objects.equals(payment.getStatus(), "SUCCESS")){
            throw new Exception("Payment is not completed");
        }

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
        appointment.setPaymentDetails(payment);
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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
