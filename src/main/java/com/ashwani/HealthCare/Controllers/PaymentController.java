package com.ashwani.HealthCare.Controllers;

import com.ashwani.HealthCare.DTO.Payment.PaymentRequest;
import com.ashwani.HealthCare.DTO.Payment.PaymentResponse;
import com.ashwani.HealthCare.DTO.Payment.PaymentWebhookPayload;
import com.ashwani.HealthCare.Entity.PaymentEntity;
import com.ashwani.HealthCare.Service.PaymentService;
import com.cashfree.pg.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
@Slf4j
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/initiate")
    /**
     * Initiate a new payment order with Cashfree
     * @param request Payment initiation payload (customer + amount)
     * @return PaymentResponse with orderId and paymentSessionId
     */
    public ResponseEntity<PaymentResponse> initiatePayment(@RequestBody PaymentRequest request) throws ApiException {
        return ResponseEntity.ok(paymentService.initiatePayment(request));
    }

    @PostMapping("/webhook/cashfree")
    /**
     * Handle incoming Cashfree webhook callbacks
     * @param rawBody Raw JSON payload from Cashfree
     * @param signature Optional header x-webhook-signature for validation
     * @return 200 OK on success
     */
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
    /**
     * Get latest known status for an order
     * @param orderId Cashfree order ID
     * @return Human readable status string
     */
    public ResponseEntity<String> getPaymentStatus(@PathVariable String orderId) throws ApiException {
        String status = paymentService.getPaymentStatus(orderId);
        return ResponseEntity.status(200).body("Your payment status is " + status);
    }

    @GetMapping("/debug/orders")
    /**
     * Debug endpoint to fetch all payment rows
     * @return List of all payments
     */
    public ResponseEntity<?> debugOrders() {
        try {
            var orders = paymentService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error getting orders", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/debug/config")
    /**
     * Debug endpoint to check Cashfree configuration (without exposing secrets)
     * @return Configuration status
     */
    public ResponseEntity<?> debugConfig() {
        try {
            return ResponseEntity.ok(paymentService.getConfigStatus());
        } catch (Exception e) {
            log.error("Error getting config status", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Get paginated list of payments with optional filtering
     * @param assembler Page number (0-based, defaults to 0)
     * @param pageable Page size (defaults to 10)
     * @param status Payment status filter (optional)
     * @param paymentMode Payment mode filter (optional)
     * @param id Patient ID (path variable)
     * @param minAmount Minimum amount filter (optional)
     * @param maxAmount Maximum amount filter (optional)
     * @return Page of PaymentEntity objects
     */
    @GetMapping("/payment-details/{id}")
    public ResponseEntity<PagedModel<EntityModel<PaymentEntity>>> getPayments(
            @PathVariable("id") Long id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMode,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            PagedResourcesAssembler<PaymentEntity> assembler) {

        try {
            Page<PaymentEntity> payments = paymentService.getPaginatedPayments(
                    status, paymentMode, id, minAmount, maxAmount, pageable);

            return ResponseEntity.ok(assembler.toModel(payments));
        } catch (Exception e) {
            log.error("Error getting paginated payments", e);
            return ResponseEntity.status(500).build();
        }
    }
}

