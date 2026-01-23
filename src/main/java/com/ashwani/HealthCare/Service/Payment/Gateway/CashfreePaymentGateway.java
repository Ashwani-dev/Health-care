package com.ashwani.HealthCare.Service.Payment.Gateway;

import com.ashwani.HealthCare.DTO.Payment.PaymentRequest;
import com.ashwani.HealthCare.DTO.Payment.PaymentResponse;
import com.ashwani.HealthCare.DTO.Payment.PaymentWebhookPayload;
import com.cashfree.pg.ApiException;
import com.cashfree.pg.ApiResponse;
import com.cashfree.pg.Cashfree;
import com.cashfree.pg.model.CreateOrderRequest;
import com.cashfree.pg.model.CustomerDetails;
import com.cashfree.pg.model.OrderEntity;
import com.cashfree.pg.model.OrderMeta;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

/**
 * Cashfree Payment Gateway Implementation
 * Active for profiles: dev, docker
 * Supports all payment modes (UPI, Cards, Net Banking, Wallets, etc.)
 */
@Component
@RequiredArgsConstructor
@Slf4j
//@Profile({"default", "dev", "docker"})
public class CashfreePaymentGateway implements PaymentGateway {

    @Value("${cashfree.appId}")
    private String appId;

    @Value("${cashfree.secretKey}")
    private String secretKey;

    @Value("${cashfree.webhook.signature.validation.enabled}")
    private boolean signatureValidationEnabled;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${cashfree.env}")
    private String cashfreeEnvironment;

    private final Cashfree cashfree;

    @Override
    public PaymentResponse initiatePayment(PaymentRequest paymentRequest) throws ApiException {
        try {
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

            log.info("[Cashfree] Attempting to create order for customer: {}, amount: {}",
                    paymentRequest.getCustomerId(), paymentRequest.getAmount());

            // Create order via SDK
            ApiResponse<OrderEntity> response = cashfree.PGCreateOrder(request, null, null, null);
            String orderId = response.getData().getOrderId();
            String paymentSessionId = response.getData().getPaymentSessionId();

            log.info("[Cashfree] Successfully created order: {}", orderId);

            // Return order and session info to the controller/caller
            return new PaymentResponse(orderId, paymentSessionId);
        } catch (ApiException e) {
            log.error("[Cashfree] API error while creating order. HTTP Code: {}, Message: {}, Response: {}",
                    e.getCode(), e.getMessage(), e.getResponseBody(), e);

            // Provide more helpful error message
            if (e.getCode() == 401) {
                throw new ApiException(
                    "Cashfree authentication failed. Please verify APP_ID and SECRET_KEY are correctly set in environment variables. " +
                    "Ensure you're using the correct credentials for the environment (SANDBOX vs PRODUCTION).",
                    e.getCode(),
                    e.getResponseHeaders(),
                    e.getResponseBody()
                );
            }
            throw e;
        } catch (Exception e) {
            log.error("[Cashfree] Unexpected error while initiating payment", e);
            throw new ApiException(e.getMessage());
        }
    }

    @Override
    public boolean validateWebhookSignature(PaymentWebhookPayload payload, String signature, String rawBody) {
        if (!signatureValidationEnabled) {
            log.debug("[Cashfree] Webhook signature validation is disabled");
            return true;
        }

        try {
            if (signature == null || signature.isEmpty()) {
                return false;
            }

            String signingKey = secretKey != null ? secretKey.trim() : null;
            if (signingKey == null || signingKey.isEmpty()) {
                log.error("[Cashfree] Secret key is null or empty!");
                return false;
            }

            // Try multiple signature computation methods
            String computedSignature1 = hmacSha256(rawBody, signingKey);
            String computedSignature2 = hmacSha256(rawBody.trim(), signingKey);

            // Try with normalized line endings
            String normalizedBody = rawBody.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
            String computedSignature3 = hmacSha256(normalizedBody, signingKey);

            // Check if any of the computed signatures match
            boolean match = computedSignature1.equals(signature) ||
                   computedSignature2.equals(signature) ||
                   computedSignature3.equals(signature);

            if (!match) {
                log.warn("[Cashfree] Webhook signature mismatch for order {}. Provided={}, Computed1={}, Computed2={}, Computed3={}",
                        payload.getOrderId(),
                        maskSignature(signature),
                        maskSignature(computedSignature1),
                        maskSignature(computedSignature2),
                        maskSignature(computedSignature3));
            }
            return match;

        } catch (Exception e) {
            log.error("[Cashfree] Error during signature validation: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isTestWebhook(PaymentWebhookPayload payload) {
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

    @Override
    public Map<String, Object> getConfigStatus() {
        Map<String, Object> status = new java.util.HashMap<>();
        status.put("gateway", "Cashfree");
        status.put("cashfreeEnvironment", cashfreeEnvironment);
        status.put("appIdConfigured", appId != null && !appId.trim().isEmpty());
        status.put("secretKeyConfigured", secretKey != null && !secretKey.trim().isEmpty());
        status.put("appIdLength", appId != null ? appId.length() : 0);
        status.put("secretKeyLength", secretKey != null ? secretKey.length() : 0);
        status.put("frontendUrl", frontendUrl);
        status.put("appIdStartsWith", appId != null && appId.length() > 3 ? appId.substring(0, 3) + "..." : "N/A");
        status.put("secretKeyStartsWith", secretKey != null && secretKey.length() > 3 ? secretKey.substring(0, 3) + "..." : "N/A");
        return status;
    }

    @Override
    public String getGatewayName() {
        return "Cashfree";
    }

    /**
     * Computes HMAC SHA256 signature for webhook validation
     * @param data The data to sign
     * @param secret The secret key
     * @return Base64 encoded signature
     */
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

    /**
     * Masks a signature for logging (shows first 4 and last 4 characters)
     * @param value The signature to mask
     * @return Masked signature string
     */
    private String maskSignature(String value) {
        if (value == null) {
            return "null";
        }
        if (value.length() <= 8) {
            return value;
        }
        return value.substring(0, 4) + "..." + value.substring(value.length() - 4);
    }
}
