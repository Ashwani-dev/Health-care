package com.ashwani.HealthCare.Repository;

import com.ashwani.HealthCare.Entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<PatientEntity, Long> {
    Optional<PatientEntity> findByUsername(String username);
    Optional<PatientEntity> findByEmail(String email);
    Optional<PatientEntity> findById(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
