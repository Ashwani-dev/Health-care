package com.ashwani.HealthCare.DTO.Payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentWebhookPayload {
    private WebhookData data;
    private String type;
    private String event_time;

    // Main accessor methods
    public String getOrderId() {
        return data != null && data.order != null ? data.order.order_id : null;
    }

    // Nested DTO classes (package-private)
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class WebhookData {
        private Order order;
        private Payment payment;
        private CustomerDetails customer_details;
        private PaymentGatewayDetails payment_gateway_details;
        private Object payment_offers;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Order {
        private String order_id;
        private BigDecimal order_amount;
        private String order_currency;
        private Object order_tags;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Payment {
        private Long cf_payment_id;
        private String payment_status;
        private BigDecimal payment_amount;
        private String payment_currency;
        private String payment_time;
        private String payment_message; 
        private String bank_reference; 
        private String auth_id; 
        private PaymentMethod payment_method;
        private String payment_group;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class PaymentMethod {
        private Card card;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Card {
        private String channel; 
        private String card_number;
        private String card_network;
        private String card_type;
        private String card_sub_type; 
        private String card_country; 
        private String card_bank_name; 
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class CustomerDetails {
        private String customer_name; 
        private String customer_id;
        private String customer_email;
        private String customer_phone;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class PaymentGatewayDetails {
        private String gateway_name;
        private String gateway_order_id;
        private String gateway_payment_id; 
        private String gateway_status_code; 
        private String gateway_order_reference_id; 
        private String gateway_settlement; 
    }

    // Helper methods
    public String getOrderStatus() {
        return getData() != null && getData().getPayment() != null
                ? getData().getPayment().getPayment_status()
                : null;
    }

    public String getReferenceId() {
        return getData() != null && getData().getPayment() != null
                ? getData().getPayment().getCf_payment_id().toString()
                : null;
    }

    public String getPaymentMode() {
        return getData() != null && getData().getPayment() != null
                ? getData().getPayment().getPayment_group()
                : null;
    }

    public String getTxTime() {
        return getData() != null && getData().getPayment() != null
                ? getData().getPayment().getPayment_time()
                : null;
    }

    public BigDecimal getOrderAmount() {
        return getData() != null && getData().getPayment() != null
                ? getData().getPayment().getPayment_amount()
                : null;
    }
}