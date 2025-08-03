package com.ashwani.HealthCare.Service;


import com.ashwani.HealthCare.DTO.Payment.PaymentRequest;
import com.ashwani.HealthCare.DTO.Payment.PaymentResponse;
import com.ashwani.HealthCare.DTO.Payment.PaymentWebhookPayload;
import com.ashwani.HealthCare.Entity.Payment;
import com.ashwani.HealthCare.Repository.PaymentRepository;
import com.cashfree.pg.ApiException;
import com.cashfree.pg.ApiResponse;
import com.cashfree.pg.Cashfree;
import com.cashfree.pg.model.CreateOrderRequest;
import com.cashfree.pg.model.CustomerDetails;
import com.cashfree.pg.model.OrderEntity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PaymentService {
    @Value("${cashfree.secretKey}")
    private String secretKey;

    private final Cashfree cashfree;
    private final PaymentRepository paymentRepository;

    public PaymentResponse initiatePayment(PaymentRequest paymentRequest) throws ApiException {
        // Construct the customer details
        CustomerDetails customerDetails = new CustomerDetails();
        customerDetails.setCustomerId(paymentRequest.getCustomerId());
        customerDetails.setCustomerPhone(paymentRequest.getCustomerPhone());
        customerDetails.setCustomerEmail(paymentRequest.getCustomerEmail());

        // Set up the order request
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOrderAmount(paymentRequest.getAmount());
        request.setOrderCurrency("INR");
        request.setCustomerDetails(customerDetails);
        // Optionally set up OrderMetaData for return/notify URLs

        // Create order via SDK
        ApiResponse<OrderEntity> response = cashfree.PGCreateOrder(request, null, null, null);
        String orderId = response.getData().getOrderId();
        String paymentSessionId = response.getData().getPaymentSessionId();

        // Return order and session info to the controller/caller
        return new PaymentResponse(orderId, paymentSessionId);
    }

    public void handleWebhook(PaymentWebhookPayload payload) {
        // 1. Validate the webhook signature
        if (!isSignatureValid(payload)) {
            // Optionally log warning and return without processing
            throw new SecurityException("Invalid webhook signature");
        }

        // 2. Find the payment/order record by orderId
        Payment payment = paymentRepository.findByOrderId(payload.getOrderId());
        if (payment == null) {
            // Optionally log and return—a production app might need to handle this
            throw new EntityNotFoundException("Order not found: " + payload.getOrderId());
        }

        // 3. Update payment status
        payment.setStatus(payload.getOrderStatus());
        payment.setReferenceId(payload.getReferenceId());
        payment.setPaymentMode(payload.getPaymentMode());
        payment.setTransactionTime(payload.getTxTime());

        // (Optionally, set more fields based on payload)

        // 4. Save the updated record
        paymentRepository.save(payment);

        // 5. Optionally log for audit
        // log.info("Payment webhook processed: {}", payload.getOrderId());
    }

    private boolean isSignatureValid(PaymentWebhookPayload payload) {
        // Assemble data string as per Cashfree documentation
        String data = payload.getOrderId() + payload.getOrderAmount() + payload.getReferenceId()
                + payload.getOrderStatus() + payload.getPaymentMode() + payload.getTxTime();
        String computedSignature = hmacSha256(data, secretKey);
        return computedSignature.equals(payload.getSignature());
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
}

