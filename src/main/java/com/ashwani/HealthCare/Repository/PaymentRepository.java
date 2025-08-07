package com.ashwani.HealthCare.Repository;

import com.ashwani.HealthCare.Entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    PaymentEntity findByOrderId(String orderId);
}

