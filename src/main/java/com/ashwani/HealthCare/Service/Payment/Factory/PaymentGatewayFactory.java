package com.ashwani.HealthCare.Service.Payment.Factory;

import com.ashwani.HealthCare.Service.Payment.Gateway.PaymentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Payment Gateway Factory
 * Returns the active payment gateway based on Spring profile
 *
 * Profile-based gateway selection:
 * - dev, docker: CashfreePaymentGateway (all payment modes)
 * - prod: PaytmPaymentGateway (UPI only)
 *
 * The appropriate gateway bean is automatically injected by Spring
 * based on the @Profile annotation on each gateway implementation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayFactory {

    private final PaymentGateway paymentGateway;

    /**
     * Get the active payment gateway based on the current Spring profile
     *
     * @return Payment gateway implementation (injected by Spring based on profile)
     */
    public PaymentGateway getPaymentGateway() {
        log.info("Using {} Payment Gateway", paymentGateway.getGatewayName());
        return paymentGateway;
    }

    /**
     * Get the active gateway name
     */
    public String getActiveGatewayName() {
        return paymentGateway.getGatewayName();
    }
}
