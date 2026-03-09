package com.ashwani.HealthCare.Service.Payment.Event;

import com.ashwani.HealthCare.DTO.Payment.PaymentCompletedEvent;
import com.ashwani.HealthCare.Entity.Appointment;
import com.ashwani.HealthCare.Entity.AppointmentHold;
import com.ashwani.HealthCare.ExceptionHandlers.common.ResourceNotFoundException;
import com.ashwani.HealthCare.ExceptionHandlers.token.TokenExpiredException;
import com.ashwani.HealthCare.Repository.AppointmentHoldRepository;
import com.ashwani.HealthCare.Repository.AppointmentRepository;
import com.ashwani.HealthCare.Service.Appointment.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final AppointmentService appointmentService;
    private final AppointmentHoldRepository appointmentHoldRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * Dynamically declare and listen to the payment.completed queue.
     * 
     * This approach handles both scenarios:
     * 1. Queue doesn't exist: Creates it with durable=true
     * 2. Queue exists with different properties: RabbitAdmin's ignoreDeclarationExceptions
     *    will ignore the declaration error and bind to the existing queue
     * 
     * This is the best approach as it works in all environments:
     * - Local development (queue may not exist)
     * - Production/Cloud (queue may already exist with different properties)
     */
    @RabbitListener(
        queuesToDeclare = @Queue(
            name = "payment.completed",
            durable = "true"
        ),
        autoStartup = "true",
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        try {
            // 0. IDEMPOTENCY CHECK: Check if appointment already exists for this payment
            Optional<Appointment> existingAppointment = appointmentRepository.findByPaymentId(event.getPaymentId());
            if (existingAppointment.isPresent()) {
                log.info("⚠️ IDEMPOTENCY: Appointment already exists for payment {} (appointmentId: {}). Skipping duplicate creation.",
                        event.getOrderId(), existingAppointment.get().getId());
                return; // Acknowledge message and skip processing
            }

            // 1. Get appointment details from hold service
            AppointmentHold hold = appointmentHoldRepository.findByHoldReference(event.getAppointmentHoldReference())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment hold",
                            event.getAppointmentHoldReference()));

            // 2. Check if hold is still valid
            if (hold.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.error("Appointment hold expired: {}", event.getAppointmentHoldReference());
                throw new TokenExpiredException("Appointment hold has expired", "APPOINTMENT_HOLD");
            }

            // 3. Convert customerId to Long
            Long patientId = Long.parseLong(event.getCustomerId());

            // 4. Call bookAppointment with hold reference to skip duplicate slot check
            // The slot was already validated during hold creation, so we can safely book it
            Appointment appointment = appointmentService.bookAppointment(
                    patientId,
                    hold.getDoctorId(),
                    hold.getDate(),
                    hold.getStartTime(),
                    hold.getReason(),
                    event.getPaymentId(),
                    event.getAppointmentHoldReference()  // Pass hold reference
            );

            // 5. Clean up the hold
            appointmentHoldRepository.deleteById(hold.getId());

            log.info("✅ Successfully created appointment {} for payment {} from hold {}",
                    appointment.getId(), event.getOrderId(), event.getAppointmentHoldReference());

        } catch (Exception e) {
            log.error("FAILED to create appointment for payment {}: {}",
                    event.getOrderId(), e.getMessage());
            // Implement retry or DLQ logic
        }
    }
}
