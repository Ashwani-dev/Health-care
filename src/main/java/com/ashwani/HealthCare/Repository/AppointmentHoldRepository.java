package com.ashwani.HealthCare.Repository;

import com.ashwani.HealthCare.Entity.AppointmentHold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppointmentHoldRepository extends JpaRepository<AppointmentHold, Long> {
    // Add this custom query method
    Optional<AppointmentHold> findByHoldReference(String holdReference);
}
