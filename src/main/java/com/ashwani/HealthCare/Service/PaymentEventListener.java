package com.ashwani.HealthCare.Service;

import com.ashwani.HealthCare.DTO.Payment.PaymentCompletedEvent;
import com.ashwani.HealthCare.Entity.AppointmentEntity;
import com.ashwani.HealthCare.Entity.AppointmentHold;
import com.ashwani.HealthCare.Repository.AppointmentHoldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final AppointmentService appointmentService;
    private final AppointmentHoldRepository appointmentHoldRepository;

    @RabbitListener(queues = "payment.completed")
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        try {
            // 1. Get appointment details from hold service
            AppointmentHold hold = appointmentHoldRepository.findByHoldReference(event.getAppointmentHoldReference())
                    .orElseThrow(() -> new RuntimeException("Appointment hold not found or expired: " +
                            event.getAppointmentHoldReference()));

            // 2. Check if hold is still valid
            if (hold.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.error("Appointment hold expired: {}", event.getAppointmentHoldReference());
                // Handle expired hold - maybe refund?
                return;
            }

            // 3. Convert customerId to Long
            Long patientId = Long.parseLong(event.getCustomerId());

            // 4. Call your existing bookAppointment method
            AppointmentEntity appointment = appointmentService.bookAppointment(
                    patientId,
                    hold.getDoctorId(),
                    hold.getDate(),
                    hold.getStartTime(),
                    hold.getReason(),
                    event.getPaymentId()
            );

            // 5. Clean up the hold
            appointmentHoldRepository.deleteById(hold.getId());

        } catch (Exception e) {
            log.error("FAILED to create appointment for payment {}: {}",
                    event.getOrderId(), e.getMessage());
            // Implement retry or DLQ logic
        }
    }
}
