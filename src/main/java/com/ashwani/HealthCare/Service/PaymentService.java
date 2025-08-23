package com.ashwani.HealthCare.Service;


import com.ashwani.HealthCare.DTO.Payment.PaymentCompletedEvent;
import com.ashwani.HealthCare.DTO.Payment.PaymentRequest;
import com.ashwani.HealthCare.DTO.Payment.PaymentResponse;
import com.ashwani.HealthCare.DTO.Payment.PaymentWebhookPayload;
import com.ashwani.HealthCare.Entity.PaymentEntity;
import com.ashwani.HealthCare.Repository.PaymentRepository;
import com.cashfree.pg.ApiException;
import com.cashfree.pg.ApiResponse;
import com.cashfree.pg.Cashfree;
import com.cashfree.pg.model.CreateOrderRequest;
import com.cashfree.pg.model.CustomerDetails;
import com.cashfree.pg.model.OrderEntity;
import com.cashfree.pg.model.OrderMeta;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    @Value("${cashfree.secretKey}")
    private String secretKey;
    
    @Value("${cashfree.webhook.signature.validation.enabled:true}")
    private boolean signatureValidationEnabled;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.backend-url}")
    private String backendUrl;

    private final Cashfree cashfree;
    private final PaymentRepository paymentRepository;
    private final RabbitTemplate rabbitTemplate;

    public PaymentResponse initiatePayment(PaymentRequest paymentRequest) throws ApiException {
        // Construct the customer details
        CustomerDetails customerDetails = new CustomerDetails();
        customerDetails.setCustomerId(paymentRequest.getCustomerId());
        customerDetails.setCustomerName(paymentRequest.getCustomerName());
        customerDetails.setCustomerPhone(paymentRequest.getCustomerPhone());
        customerDetails.setCustomerEmail(paymentRequest.getCustomerEmail());

        // Set up the order request
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOrderAmount(paymentRequest.getAmount());
        request.setOrderCurrency("INR");
        request.setCustomerDetails(customerDetails);

        OrderMeta orderMeta = new OrderMeta();
        orderMeta.setReturnUrl(frontendUrl + "/payment-status?order_id={order_id}");
        request.setOrderMeta(orderMeta);

        // Create order via SDK
        ApiResponse<OrderEntity> response = cashfree.PGCreateOrder(request, null, null, null);
        String orderId = response.getData().getOrderId();
        String paymentSessionId = response.getData().getPaymentSessionId();

        PaymentEntity payment = new PaymentEntity();
        payment.setOrderId(response.getData().getOrderId());
        payment.setStatus("PENDING");
        payment.setPatientId(Long.parseLong(paymentRequest.getCustomerId()));
        payment.setAppointmentHoldReference(paymentRequest.getAppointmentHoldReference());

        paymentRepository.save(payment);

        // Return order and session info to the controller/caller
        return new PaymentResponse(orderId, paymentSessionId);
    }

    /**
     * Returns latest payment status we know for a given order.
     * For now, reads from our DB which is updated by webhook. In future,
     * can be extended to call Cashfree's order/status API for real-time status.
     */
    public String getPaymentStatus(String orderId) throws ApiException {
        PaymentEntity payment = paymentRepository.findByOrderId(orderId);
        if (payment == null) {
            return "PENDING";
        }
        return payment.getStatus() == null ? "PENDING" : payment.getStatus();
    }

    @Transactional
    public void handleWebhook(PaymentWebhookPayload payload, String signature, String rawBody) {
        try {
            // Check if this is a test webhook (contains test data)
            if (isTestWebhook(payload)) {
                return;
            }

            // 1. Validate the webhook signature (with configurable enforcement)
            if (signatureValidationEnabled) {
                boolean signatureValid = isSignatureValid(payload, signature, rawBody);
                if (!signatureValid) {
                    log.error("Invalid webhook signature for order: {}", payload.getOrderId());
                    throw new SecurityException("Invalid webhook signature");
                }
            }

            // 2. Find the payment/order record by orderId
            PaymentEntity payment = paymentRepository.findByOrderId(payload.getOrderId());
            if (payment == null) {
                log.error("Order not found in database: {}", payload.getOrderId());
                throw new EntityNotFoundException("Order not found: " + payload.getOrderId());
            }

            // 3. Update payment status
            String previousStatus = payment.getStatus();
            String newStatus = payload.getOrderStatus();

            payment.setStatus(payload.getOrderStatus());
            payment.setReferenceId(payload.getReferenceId());
            payment.setPaymentMode(payload.getPaymentMode());
            payment.setTransactionTime(payload.getTxTime());
            payment.setOrderAmount(payload.getOrderAmount());

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
            // Re-throw the exception so Cashfree knows the webhook failed and will retry
            throw e;
        }
    }

    public List<PaymentEntity> getAllOrders() {
        return paymentRepository.findAll();
    }

    private boolean isSignatureValid(PaymentWebhookPayload payload, String signature, String rawBody) {
        try {
            if (signature == null || signature.isEmpty()) {
                return false;
            }

            if (secretKey == null || secretKey.isEmpty()) {
                log.error("Secret key is null or empty!");
                return false;
            }

            // Try multiple signature computation methods
            String computedSignature1 = hmacSha256(rawBody, secretKey);
            String computedSignature2 = hmacSha256(rawBody.trim(), secretKey);
            
            // Try with normalized line endings
            String normalizedBody = rawBody.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
            String computedSignature3 = hmacSha256(normalizedBody, secretKey);
            
            // Check if any of the computed signatures match
            return computedSignature1.equals(signature) || 
                   computedSignature2.equals(signature) || 
                   computedSignature3.equals(signature);
            
        } catch (Exception e) {
            log.error("Error during signature validation: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean isTestWebhook(PaymentWebhookPayload payload) {
        // Check if orderId is null or empty (test webhooks might not have real order data)
        if (payload.getOrderId() == null || payload.getOrderId().isEmpty()) {
            return true;
        }
        
        // Check if this is a test webhook by looking for test indicators in the order ID
        if (payload.getOrderId().contains("test") || payload.getOrderId().contains("demo")) {
            return true;
        }
        
        // Check if the webhook type indicates it's a test
        if (payload.getType() != null && payload.getType().toLowerCase().contains("test")) {
            return true;
        }
        
        return false;
    }

    private String hmacSha256(String data, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signedBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new RuntimeException("Failed to compute HMAC SHA256", ex);
        }
    }

    // Helper method to define what constitutes a "successful" payment
    private boolean isSuccessfulStatus(String status) {
        if (status == null) return false;
        return status.equalsIgnoreCase("PAID") ||
                status.equalsIgnoreCase("SUCCESS");
    }
}


