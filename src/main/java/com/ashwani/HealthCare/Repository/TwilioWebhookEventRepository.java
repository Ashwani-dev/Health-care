package com.ashwani.HealthCare.Repository;

import com.ashwani.HealthCare.Entity.TwilioWebhookEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TwilioWebhookEventRepository extends JpaRepository <TwilioWebhookEventEntity, Long> {
}
