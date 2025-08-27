package com.ashwani.HealthCare.Repository;

import com.ashwani.HealthCare.Entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long>, JpaSpecificationExecutor<PaymentEntity> {
    PaymentEntity findByOrderId(String orderId);
}

