package com.ashwani.HealthCare.Service.Payment.Gateway;

import com.ashwani.HealthCare.DTO.Payment.PaymentRequest;
import com.ashwani.HealthCare.DTO.Payment.PaymentResponse;
import com.ashwani.HealthCare.DTO.Payment.PaymentWebhookPayload;
import com.cashfree.pg.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * Paytm Payment Gateway Implementation
 * Active for profile: prod
 * Supports UPI mode only for production
 *
 * Note: This is a simplified implementation for Paytm UPI payments.
 * For full Paytm integration, you may need to add the official Paytm SDK.
 *
 * CURRENTLY DISABLED - Uncomment @Component to enable when Paytm credentials are available
 */
//@Component  // DISABLED - Using Cashfree for all environments
@RequiredArgsConstructor
@Slf4j
//@Profile("prod")
@ConditionalOnProperty(
        prefix = "paytm",
        name = "merchant.id",
        matchIfMissing = false  // Don't create bean if property missing
)
public class PaytmPaymentGateway implements PaymentGateway {

    @Value("${paytm.merchant.id}")
    private String merchantId;

    @Value("${paytm.merchant.key}")
    private String merchantKey;

    @Value("${paytm.website:WEBSTAGING}")
    private String website;

    @Value("${paytm.industry.type:Retail}")
    private String industryType;

    @Value("${paytm.channel.id:WEB}")
    private String channelId;

    @Value("${paytm.api.url:https://securegw-stage.paytm.in}")
    private String paytmApiUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.backend-url}")
    private String backendUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public PaymentResponse initiatePayment(PaymentRequest paymentRequest) throws ApiException {
        try {
            String orderId = generateOrderId();
            String txnAmount = paymentRequest.getAmount().toString();
            String customerId = paymentRequest.getCustomerId();

            log.info("[Paytm] Initiating UPI payment for customer: {}, amount: {}, orderId: {}",
                    customerId, txnAmount, orderId);

            // Build payment request body
            Map<String, Object> body = new HashMap<>();
            Map<String, String> head = new HashMap<>();
            head.put("requestTimestamp", String.valueOf(System.currentTimeMillis()));
            head.put("version", "v1");
            head.put("channelId", channelId);

            Map<String, Object> bodyContent = new HashMap<>();
            bodyContent.put("requestType", "Payment");
            bodyContent.put("mid", merchantId);
            bodyContent.put("websiteName", website);
            bodyContent.put("orderId", orderId);
            bodyContent.put("callbackUrl", backendUrl + "/api/payments/webhook/paytm");

            Map<String, String> txnInfo = new HashMap<>();
            txnInfo.put("CHANNEL_ID", channelId);
            txnInfo.put("ORDER_ID", orderId);
            txnInfo.put("TXN_AMOUNT", txnAmount);
            txnInfo.put("CUST_ID", customerId);
            txnInfo.put("INDUSTRY_TYPE_ID", industryType);
            txnInfo.put("WEBSITE", website);
            txnInfo.put("MOBILE_NO", paymentRequest.getCustomerPhone());
            txnInfo.put("EMAIL", paymentRequest.getCustomerEmail());

            // Add UPI payment mode
            txnInfo.put("PAYMENT_MODE_ONLY", "UPI");

            bodyContent.put("txnAmount", Map.of("value", txnAmount, "currency", "INR"));
            bodyContent.put("userInfo", Map.of(
                "custId", customerId,
                "mobile", paymentRequest.getCustomerPhone(),
                "email", paymentRequest.getCustomerEmail()
            ));

            body.put("head", head);
            body.put("body", bodyContent);

            // Generate checksum
            String checksum = generateChecksum(bodyContent, merchantKey);
            head.put("signature", checksum);

            // Make API call to Paytm
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            String url = paytmApiUrl + "/theia/api/v1/initiateTransaction?mid=" + merchantId + "&orderId=" + orderId;

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                JsonNode bodyNode = responseJson.get("body");

                if (bodyNode != null && "SUCCESS".equals(bodyNode.get("resultInfo").get("resultStatus").asText())) {
                    String txnToken = bodyNode.get("txnToken").asText();

                    log.info("[Paytm] Successfully initiated payment. OrderId: {}, TxnToken: {}", orderId, txnToken);

                    // Return order ID and transaction token (similar to payment session ID)
                    return new PaymentResponse(orderId, txnToken);
                } else {
                    String errorMsg = bodyNode != null ? bodyNode.get("resultInfo").get("resultMsg").asText() : "Unknown error";
                    log.error("[Paytm] Payment initiation failed: {}", errorMsg);
                    throw new ApiException("Paytm payment initiation failed: " + errorMsg);
                }
            } else {
                log.error("[Paytm] API returned non-OK status: {}", response.getStatusCode());
                throw new ApiException("Paytm API error: " + response.getStatusCode());
            }

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Paytm] Error initiating payment", e);
            throw new ApiException("Paytm payment error: " + e.getMessage());
        }
    }

    @Override
    public boolean validateWebhookSignature(PaymentWebhookPayload payload, String signature, String rawBody) {
        try {
            if (signature == null || signature.isEmpty()) {
                log.warn("[Paytm] No signature provided in webhook");
                return false;
            }

            // Parse the webhook body to extract parameters
            Map<String, String> params = new HashMap<>();
            JsonNode jsonNode = objectMapper.readTree(rawBody);
            jsonNode.fields().forEachRemaining(entry -> {
                params.put(entry.getKey(), entry.getValue().asText());
            });

            // Generate checksum and validate
            String computedChecksum = generateChecksum(params, merchantKey);
            boolean isValid = computedChecksum.equals(signature);

            if (!isValid) {
                log.warn("[Paytm] Webhook signature mismatch. Expected: {}, Got: {}",
                        maskSignature(computedChecksum), maskSignature(signature));
            }

            return isValid;
        } catch (Exception e) {
            log.error("[Paytm] Error validating webhook signature", e);
            return false;
        }
    }

    @Override
    public boolean isTestWebhook(PaymentWebhookPayload payload) {
        // Check if orderId is null or empty
        if (payload.getOrderId() == null || payload.getOrderId().isEmpty()) {
            return true;
        }

        // Check for test indicators
        String orderId = payload.getOrderId().toLowerCase();
        return orderId.contains("test") || orderId.contains("demo") || orderId.contains("sample");
    }

    @Override
    public Map<String, Object> getConfigStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("gateway", "Paytm");
        status.put("merchantIdConfigured", merchantId != null && !merchantId.trim().isEmpty());
        status.put("merchantKeyConfigured", merchantKey != null && !merchantKey.trim().isEmpty());
        status.put("website", website);
        status.put("channelId", channelId);
        status.put("apiUrl", paytmApiUrl);
        status.put("frontendUrl", frontendUrl);
        status.put("backendUrl", backendUrl);
        status.put("paymentMode", "UPI Only");
        return status;
    }

    @Override
    public String getGatewayName() {
        return "Paytm";
    }

    /**
     * Generate a unique order ID
     */
    private String generateOrderId() {
        return "ORDER_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Generate Paytm checksum
     * This is a simplified version. For production, use Paytm's official checksum utility.
     */
    private String generateChecksum(Map<String, ?> params, String key) {
        try {
            // Sort parameters
            TreeMap<String, String> sortedParams = new TreeMap<>();
            params.forEach((k, v) -> sortedParams.put(k, v != null ? v.toString() : ""));

            // Build parameter string
            StringBuilder paramStr = new StringBuilder();
            sortedParams.forEach((k, v) -> {
                if (!k.equals("CHECKSUMHASH")) {
                    paramStr.append(k).append("=").append(v).append("|");
                }
            });
            paramStr.append(key);

            // Generate SHA256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(paramStr.toString().getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            log.error("[Paytm] Error generating checksum", e);
            throw new RuntimeException("Failed to generate checksum", e);
        }
    }

    /**
     * Masks a signature for logging
     */
    private String maskSignature(String value) {
        if (value == null) return "null";
        if (value.length() <= 8) return value;
        return value.substring(0, 4) + "..." + value.substring(value.length() - 4);
    }
}
