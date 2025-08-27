package com.ashwani.HealthCare.specifications;

import com.ashwani.HealthCare.Entity.PaymentEntity;
import org.springframework.data.jpa.domain.Specification;

public class PaymentSpecifications {

    // Filter by payment status (null-safe, case-insensitive exact match)
    public static Specification<PaymentEntity> hasStatus(String status) {
        return (root, query, cb) ->
                status == null || status.isEmpty()
                        ? cb.conjunction()
                        : cb.equal(cb.lower(root.get("status")), status.toLowerCase());
    }

    // Filter by payment mode (null-safe, case-insensitive partial match)
    public static Specification<PaymentEntity> hasPaymentMode(String paymentMode) {
        return (root, query, cb) ->
                paymentMode == null || paymentMode.isEmpty()
                        ? cb.conjunction()
                        : cb.like(
                        cb.lower(root.get("paymentMode")),
                        "%" + paymentMode.toLowerCase() + "%"
                );
    }

    // Filter by patient ID
    public static Specification<PaymentEntity> hasPatientId(Long patientId) {
        return (root, query, cb) ->
                patientId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("patientId"), patientId);
    }

    // Filter by order amount range
    public static Specification<PaymentEntity> amountBetween(java.math.BigDecimal minAmount, java.math.BigDecimal maxAmount) {
        return (root, query, cb) -> {
            if (minAmount == null && maxAmount == null) {
                return cb.conjunction();
            }
            
            if (minAmount != null && maxAmount != null) {
                return cb.between(root.get("orderAmount"), minAmount, maxAmount);
            } else if (minAmount != null) {
                return cb.greaterThanOrEqualTo(root.get("orderAmount"), minAmount);
            } else {
                return cb.lessThanOrEqualTo(root.get("orderAmount"), maxAmount);
            }
        };
    }
}
