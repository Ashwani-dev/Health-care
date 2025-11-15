package com.ashwani.HealthCare.Repository;

import com.ashwani.HealthCare.Entity.DoctorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends
        JpaRepository<DoctorEntity, Long>,
        JpaSpecificationExecutor<DoctorEntity> {
    Optional<DoctorEntity> findByUsername(String username);
    Optional<DoctorEntity> findByEmail(String email);
    
    @Query("SELECT d FROM DoctorEntity d WHERE d.license_number = :licenseNumber")
    Optional<DoctorEntity> findByLicenseNumber(@Param("licenseNumber") String licenseNumber);
    
    List<DoctorEntity> findAll();
}
