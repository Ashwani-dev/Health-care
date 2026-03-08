package com.ashwani.HealthCare.Service.Appointment;

import com.ashwani.HealthCare.DTO.Appointments.PatientAppointmentResponse;
import com.ashwani.HealthCare.Entity.*;
import com.ashwani.HealthCare.ExceptionHandlers.appointment.AppointmentCancellationException;
import com.ashwani.HealthCare.ExceptionHandlers.appointment.InvalidAppointmentStateException;
import com.ashwani.HealthCare.ExceptionHandlers.appointment.SlotNotAvailableException;
import com.ashwani.HealthCare.ExceptionHandlers.auth.UnauthorizedAccessException;
import com.ashwani.HealthCare.ExceptionHandlers.common.ResourceNotFoundException;
import com.ashwani.HealthCare.ExceptionHandlers.payment.PaymentException;
import com.ashwani.HealthCare.Repository.*;
import com.ashwani.HealthCare.Service.Communication.EmailService;
import com.ashwani.HealthCare.Utility.TimeSlot;
import com.ashwani.HealthCare.specifications.AppointmentSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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


    private PatientAppointmentResponse convertToResponse(Appointment appointment) {
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
                                        LocalTime startTime, String description) {

        // Validate doctor exists and slot is available
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        validateSlotNotBooked(doctor, date, startTime);
        validateDoctorAvailability(doctor, date, startTime);

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
    public Appointment bookAppointment(Long patientId, Long doctorId, LocalDate date,
                                       LocalTime startTime, String description, Long paymentId,
                                       String holdReference) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        if(!Objects.equals(payment.getStatus(), "SUCCESS")){
            throw new PaymentException("Payment is not completed");
        }

        // Only check for existing bookings if NOT booking from a valid hold
        // When holdReference is provided, the slot was already validated during hold creation
        if (holdReference == null) {
            validateSlotNotBooked(doctor, date, startTime);
        } else {
            log.info("Booking from hold reference: {}, skipping duplicate slot check", holdReference);
        }

        // Validate doctor availability
        validateDoctorAvailability(doctor, date, startTime);

        // Default duration - could be parameterized
        LocalTime endTime = startTime.plusMinutes(30);

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(date);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus("SCHEDULED");
        appointment.setDescription(description);
        appointment.setPaymentDetails(payment);
        appointmentRepository.save(appointment);

        log.info("✅ SUCCESS: Appointment saved successfully with appointmentId: {}", appointment.getId());

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
            LocalDate appointmentStartDate,
            LocalDate appointmentEndDate,
            LocalTime startTime,
            LocalTime endTime,
            String status,
            Pageable pageable) {

        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", patientId);
        }

        // Build specifications
        Specification<Appointment> spec = Specification.where(AppointmentSpecifications.hasPatient(patientId))
                .and(AppointmentSpecifications.hasAppointmentDateRange(appointmentStartDate, appointmentEndDate))
                .and(AppointmentSpecifications.hasTimeRange(startTime, endTime))
                .and(AppointmentSpecifications.hasStatus(status));

        // Get paginated results
        Page<Appointment> appointmentPage = appointmentRepository.findAll(spec, pageable);

        // Process and convert to response DTOs
        return processAppointmentPage(appointmentPage);
    }

    @Transactional
    public Page<PatientAppointmentResponse> getDoctorAppointments(
            Long doctorId,
            LocalDate appointmentStartDate,
            LocalDate appointmentEndDate,
            LocalTime startTime,
            LocalTime endTime,
            String status,
            Pageable pageable) {

        // Verify doctor exists using existsById (more efficient than findById)
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor", doctorId);
        }

        // Combine all specifications
        Specification<Appointment> spec = Specification.where(AppointmentSpecifications.hasDoctor(doctorId))
                .and(AppointmentSpecifications.hasAppointmentDateRange(appointmentStartDate, appointmentEndDate))
                .and(AppointmentSpecifications.hasTimeRange(startTime, endTime))
                .and(AppointmentSpecifications.hasStatus(status));

        // Get paginated results
        Page<Appointment> appointmentPage = appointmentRepository.findAll(spec, pageable);

        // Process and convert to response DTOs
        return processAppointmentPage(appointmentPage);
    }

    @Transactional(readOnly = true)
    public List<TimeSlot> getAvailableSlots(Long doctorId, LocalDate date) {
        // 1. Early validation
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        // 2. Batch fetch data
        List<DoctorAvailability> availabilities = doctorAvailabilityRepository
                .findByDoctorAndDayOfWeek(doctor, date.getDayOfWeek());

        Set<LocalTime> bookedSlots = appointmentRepository
                .findByDoctorAndAppointmentDate(doctor, date)
                .stream()
                .map(Appointment::getStartTime)
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
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        validateCancellation(appointment, userId);
        appointment.cancel(userId);
        appointmentRepository.save(appointment);

        log.info("Appointment {} cancelled by user {}", appointmentId, userId);
    }

    @Transactional
    public PatientAppointmentResponse updateAppointment(Long appointmentId, LocalDate appointmentDate,
                                                        LocalTime startTime, LocalTime endTime) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        // Only allow updates for SCHEDULED appointments
        if (!"SCHEDULED".equals(appointment.getStatus())) {
            throw new InvalidAppointmentStateException(
                    "Can only update appointments with SCHEDULED status",
                    appointment.getStatus(),
                    "UPDATE"
            );
        }

        Doctor doctor = appointment.getDoctor();

        // Validate the new slot
        if (appointmentDate != null && startTime != null) {
            validateDoctorAvailability(doctor, appointmentDate, startTime);

            // Check if the new slot is not booked (excluding current appointment)
            boolean isSlotBooked = appointmentRepository.existsByDoctorAndAppointmentDateAndStartTimeAndIdNot(
                    doctor, appointmentDate, startTime, appointmentId);

            if (isSlotBooked) {
                throw new SlotNotAvailableException("Time slot already booked", appointmentDate, startTime);
            }
        }

        // Update appointment details
        if (appointmentDate != null) {
            appointment.setAppointmentDate(appointmentDate);
        }
        if (startTime != null) {
            appointment.setStartTime(startTime);
        }
        if (endTime != null) {
            appointment.setEndTime(endTime);
        }

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        log.info("Updated appointment ID: {} to date: {}, startTime: {}, endTime: {}",
                appointmentId, appointmentDate, startTime, endTime);

        return convertToResponse(updatedAppointment);
    }

    private void validateCancellation(Appointment appointment, Long userId) {
        // Check if user is either the patient or the doctor
        boolean isPatient = appointment.belongsToPatient(userId);
        boolean isDoctor = appointment.belongsToDoctor(userId);

        if (!isPatient && !isDoctor) {
            throw new UnauthorizedAccessException("You are not authorized to cancel this appointment", userId, "Appointment");
        }

        // Only patients need to respect the 24-hour cancellation deadline
        // Doctors can cancel anytime for emergency or scheduling conflicts
        if (isPatient && appointment.isPastCancellationDeadline()) {
            throw new AppointmentCancellationException("Cancellation requires 24-hour advance notice");
        }
    }

    /**
     * Validates if a time slot is within doctor's availability schedule
     */
    private void validateDoctorAvailability(Doctor doctor, LocalDate date, LocalTime startTime) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorAvailability> availabilities = doctorAvailabilityRepository.findByDoctorAndDayOfWeek(doctor, dayOfWeek);

        boolean isAvailable = availabilities.stream()
                .anyMatch(av -> av.getIsAvailable() &&
                        !startTime.isBefore(av.getStartTime()) &&
                        !startTime.isAfter(av.getEndTime()));

        if (!isAvailable) {
            throw new SlotNotAvailableException("Doctor is not available at this time", date, startTime);
        }
    }

    /**
     * Checks if a time slot is already booked for a doctor
     */
    private void validateSlotNotBooked(Doctor doctor, LocalDate date, LocalTime startTime) {
        if (appointmentRepository.existsByDoctorAndAppointmentDateAndStartTime(doctor, date, startTime)) {
            throw new SlotNotAvailableException("Time slot already booked", date, startTime);
        }
    }

    /**
     * Updates appointment status to COMPLETED if past end time
     * Returns updated appointment or original if no update needed
     */
    private Appointment updateAppointmentStatusIfNeeded(Appointment appointment) {
        if (appointment.getStatus().equals("SCHEDULED") &&
                (appointment.getAppointmentDate().isBefore(LocalDate.now()) ||
                        (appointment.getAppointmentDate().isEqual(LocalDate.now()) &&
                                appointment.getEndTime().isBefore(LocalTime.now())))) {
            appointment.setStatus("COMPLETED");
            return appointmentRepository.save(appointment);
        }
        return appointment;
    }

    /**
     * Processes a page of appointments, updating statuses and converting to responses
     */
    private Page<PatientAppointmentResponse> processAppointmentPage(Page<Appointment> appointmentPage) {
        // Update statuses for all appointments in the page
        List<Appointment> updatedContent = appointmentPage.getContent().stream()
                .map(this::updateAppointmentStatusIfNeeded)
                .toList();

        // Convert to response DTOs
        return appointmentPage.map(apt -> {
            Appointment updated = updatedContent.stream()
                    .filter(u -> u.getId().equals(apt.getId()))
                    .findFirst()
                    .orElse(apt);
            return convertToResponse(updated);
        });
    }
}
