package com.ashwani.HealthCare.Controllers;

import com.ashwani.HealthCare.DTO.Payment.PaymentRequest;
import com.ashwani.HealthCare.DTO.Payment.PaymentResponse;
import com.ashwani.HealthCare.DTO.Payment.PaymentWebhookPayload;
import com.ashwani.HealthCare.Service.PaymentService;
import com.cashfree.pg.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Slf4j
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(@RequestBody PaymentRequest request) throws ApiException {
        return ResponseEntity.ok(paymentService.initiatePayment(request));
    }

    @PostMapping("/webhook/cashfree")
    public ResponseEntity<?> paymentWebhook(@RequestBody String rawBody,
                                            @RequestHeader(value = "x-webhook-signature", required = false) String signature) {
        try {
            // Parse the JSON payload
            ObjectMapper objectMapper = new ObjectMapper();
            PaymentWebhookPayload payload = objectMapper.readValue(rawBody, PaymentWebhookPayload.class);
            
            paymentService.handleWebhook(payload, signature, rawBody);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.status(500).body("Error processing webhook: " + e.getMessage());
        }
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<String> getPaymentStatus(@PathVariable String orderId) throws ApiException {
        String status = paymentService.getPaymentStatus(orderId);
        return ResponseEntity.status(200).body("Your payment status is " + status);
    }

    @GetMapping("/debug/orders")
    public ResponseEntity<?> debugOrders() {
        try {
            var orders = paymentService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error getting orders", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}

