package com.ashwani.HealthCare.Repository;

import com.ashwani.HealthCare.Entity.DoctorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends
        JpaRepository<DoctorEntity, Long>,
        JpaSpecificationExecutor<DoctorEntity> {
    Optional<DoctorEntity> findByUsername(String username);
    Optional<DoctorEntity> findByEmail(String email);
    List<DoctorEntity> findAll();
}
