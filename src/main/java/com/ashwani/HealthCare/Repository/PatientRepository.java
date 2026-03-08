package com.ashwani.HealthCare.Repository;

import com.ashwani.HealthCare.Entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUsername(String username);
    Optional<Patient> findByEmail(String email);
    Optional<Patient> findById(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
