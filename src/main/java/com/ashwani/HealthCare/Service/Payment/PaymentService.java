package com.ashwani.HealthCare.Service.Payment;


import com.ashwani.HealthCare.DTO.Payment.PaymentCompletedEvent;
import com.ashwani.HealthCare.DTO.Payment.PaymentRequest;
import com.ashwani.HealthCare.DTO.Payment.PaymentResponse;
import com.ashwani.HealthCare.DTO.Payment.PaymentWebhookPayload;
import com.ashwani.HealthCare.Entity.Payment;
import com.ashwani.HealthCare.ExceptionHandlers.common.ResourceNotFoundException;
import com.ashwani.HealthCare.ExceptionHandlers.payment.PaymentException;
import com.ashwani.HealthCare.ExceptionHandlers.payment.WebhookValidationException;
import com.ashwani.HealthCare.Repository.PaymentRepository;
import com.ashwani.HealthCare.Service.Payment.Gateway.PaymentGateway;
import com.ashwani.HealthCare.Service.Payment.Factory.PaymentGatewayFactory;
import com.cashfree.pg.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.ashwani.HealthCare.specifications.PaymentSpecifications;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentGatewayFactory paymentGatewayFactory;
    private final PaymentRepository paymentRepository;
    private final RabbitTemplate rabbitTemplate;

    public PaymentResponse initiatePayment(PaymentRequest paymentRequest) {
        try {
            // Get the appropriate payment gateway
            PaymentGateway gateway = paymentGatewayFactory.getPaymentGateway();
            log.info("Initiating payment with {} gateway for customer: {}, amount: {}",
                    gateway.getGatewayName(), paymentRequest.getCustomerId(), paymentRequest.getAmount());

            // Create order via the selected gateway
            PaymentResponse response = gateway.initiatePayment(paymentRequest);

            log.info("Successfully created order: {} using {} gateway",
                    response.getOrderId(), gateway.getGatewayName());

            // Save payment entity
            Payment payment = new Payment();
            payment.setOrderId(response.getOrderId());
            payment.setStatus("PENDING");
            payment.setPatientId(Long.parseLong(paymentRequest.getCustomerId()));
            payment.setAppointmentHoldReference(paymentRequest.getAppointmentHoldReference());

            paymentRepository.save(payment);

            // Return order and session info to the controller/caller
            return response;
        } catch (ApiException e) {
            log.error("Payment gateway error while initiating payment", e);
            throw new PaymentException("Payment gateway error: " + e.getMessage(), null, "GATEWAY_ERROR");
        } catch (Exception e) {
            log.error("Unexpected error while initiating payment", e);
            throw new PaymentException("Failed to initiate payment: " + e.getMessage());
        }
    }

    /**
     * Returns latest payment status we know for a given order.
     * For now, reads from our DB which is updated by webhook. In future,
     * can be extended to call Cashfree's order/status API for real-time status.
     */
    public String getPaymentStatus(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId);
        if (payment == null) {
            return "PENDING";
        }
        return payment.getStatus() == null ? "PENDING" : payment.getStatus();
    }

    @Transactional
    public void handleWebhook(PaymentWebhookPayload payload, String signature, String rawBody) {
        try {
            log.info("Received webhook for order: {}, type: {}", payload.getOrderId(), payload.getType());
            
            // Get the appropriate payment gateway
            PaymentGateway gateway = paymentGatewayFactory.getPaymentGateway();

            // Check if this is a test webhook (contains test data)
            if (gateway.isTestWebhook(payload)) {
                log.info("Test webhook detected for order: {}, skipping processing", payload.getOrderId());
                return;
            }

            // Validate webhook signature if enabled
            if (!gateway.validateWebhookSignature(payload, signature, rawBody)) {
                log.error("Invalid webhook signature for order: {} using {} gateway",
                        payload.getOrderId(), gateway.getGatewayName());
                throw new WebhookValidationException("Invalid webhook signature", payload.getOrderId());
            }

            log.info("Processing webhook for order: {} using {} gateway",
                    payload.getOrderId(), gateway.getGatewayName());

            // Find the payment/order record by orderId
            Payment payment = paymentRepository.findByOrderId(payload.getOrderId());
            if (payment == null) {
                log.error("Order not found in database: {}", payload.getOrderId());
                throw new ResourceNotFoundException("Payment order", payload.getOrderId());
            }

            // Update payment status
            String previousStatus = payment.getStatus();
            String newStatus = payload.getOrderStatus();
            
            log.info("Updating payment status for order: {} from {} to {}", 
                    payload.getOrderId(), previousStatus, newStatus);

            payment.setStatus(payload.getOrderStatus());
            payment.setReferenceId(payload.getReferenceId());
            payment.setPaymentMode(payload.getPaymentMode());
            payment.setTransactionTime(payload.getTxTime());
            payment.setOrderAmount(payload.getOrderAmount());
            
            log.info("Payment updated - OrderId: {}, Status: {}, ReferenceId: {}, PaymentMode: {}", 
                    payload.getOrderId(), newStatus, payload.getReferenceId(), payload.getPaymentMode());

            // 4. Save the updated record
            paymentRepository.save(payment);

            // Only publish an event if the payment has just been successfully completed
            if (isSuccessfulStatus(newStatus) && !isSuccessfulStatus(previousStatus)){
                try {
                    // Create the event object with all necessary data
                    PaymentCompletedEvent event = new PaymentCompletedEvent();
                    event.setOrderId(payload.getOrderId());
                    event.setReferenceId(payload.getReferenceId());
                    event.setCustomerId(payment.getPatientId().toString());
                    event.setOrderAmount(payload.getOrderAmount());
                    event.setPaymentMode(payload.getPaymentMode());
                    event.setAppointmentHoldReference(payment.getAppointmentHoldReference());
                    event.setPaymentId(payment.getId());

                    // Publish the event to RabbitMQ
                    // When sending the message, set TTL (10 minutes = 600000 milliseconds)
                    MessageProperties props = MessagePropertiesBuilder.newInstance()
                            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                            .setExpiration("600000")
                            .build();

                    Message message = rabbitTemplate.getMessageConverter().toMessage(event, props);
                    rabbitTemplate.send("payment.completed", message);
                    // PaymentCompletedEvent published successfully
                } catch (Exception e) {
                    // Log the error but don't re-throw it and cause the webhook to fail.
                    // Cashfree will retry the webhook if we return an error code.
                    // We don't want that just because our messaging is down.
                    log.error("Failed to publish RabbitMQ event for order: {}", payload.getOrderId(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing webhook for orderId: {}", payload.getOrderId(), e);
            // Re-throw the exception so the gateway knows the webhook failed and will retry
            throw e;
        }
    }

    public List<Payment> getAllOrders() {
        return paymentRepository.findAll();
    }

    /**
     * Get paginated list of payments with optional filtering
     * @param status Payment status filter (optional)
     * @param paymentMode Payment mode filter (optional)
     * @param patientId Patient ID filter (optional)
     * @param minAmount Minimum amount filter (optional)
     * @param maxAmount Maximum amount filter (optional)
     * @param pageable Pagination and sorting information
     * @return Page of Payment objects
     */
    @Transactional(readOnly = true)
    public Page<Payment> getPaginatedPayments(
            String status,
            String paymentMode,
            Long patientId,
            java.math.BigDecimal minAmount,
            java.math.BigDecimal maxAmount,
            Pageable pageable) {
        
        // Build specification based on filters
        Specification<Payment> spec = Specification.where(null);
        
        if (status != null && !status.isEmpty()) {
            spec = spec.and(PaymentSpecifications.hasStatus(status));
        }
        
        if (paymentMode != null && !paymentMode.isEmpty()) {
            spec = spec.and(PaymentSpecifications.hasPaymentMode(paymentMode));
        }
        
        if (patientId != null) {
            spec = spec.and(PaymentSpecifications.hasPatientId(patientId));
        }
        
        if (minAmount != null || maxAmount != null) {
            spec = spec.and(PaymentSpecifications.amountBetween(minAmount, maxAmount));
        }
        
        // Return paginated results ordered by ID descending (latest first)
        return paymentRepository.findAll(spec, pageable);
    }

    /**
     * Get configuration status for debugging (without exposing secrets)
     */
    public Map<String, Object> getConfigStatus() {
        try {
            PaymentGateway gateway = paymentGatewayFactory.getPaymentGateway();
            Map<String, Object> status = gateway.getConfigStatus();
            status.put("activeGateway", gateway.getGatewayName());
            return status;
        } catch (Exception e) {
            Map<String, Object> errorStatus = new java.util.HashMap<>();
            errorStatus.put("error", "Failed to get gateway configuration: " + e.getMessage());
            return errorStatus;
        }
    }

    // Helper method to define what constitutes a "successful" payment
    private boolean isSuccessfulStatus(String status) {
        if (status == null) return false;
        return status.equalsIgnoreCase("PAID") ||
                status.equalsIgnoreCase("SUCCESS");
    }
}


