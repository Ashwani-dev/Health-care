package com.ashwani.HealthCare.Repository;

import com.ashwani.HealthCare.Entity.VideoCallSessionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VideoCallSessionsRepository extends JpaRepository<VideoCallSessionsEntity, Long> {

    Optional<VideoCallSessionsEntity> findByAppointmentId(Long appointmentId);
}
