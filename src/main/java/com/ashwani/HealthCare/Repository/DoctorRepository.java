package com.ashwani.HealthCare.Repository;

import com.ashwani.HealthCare.Entity.DoctorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<DoctorEntity, Long> {
    Optional<DoctorEntity> findByUsername(String username);
    Optional<DoctorEntity> findByEmail(String email);
    List<DoctorEntity> findAll();
    List<DoctorEntity> findBySpecializationContainingIgnoreCase(String specialization);
}
