package com.ashwani.HealthCare.Repository;

import com.ashwani.HealthCare.Entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends
        JpaRepository<Doctor, Long>,
        JpaSpecificationExecutor<Doctor> {
    Optional<Doctor> findByUsername(String username);
    Optional<Doctor> findByEmail(String email);
    
    @Query("SELECT d FROM Doctor d WHERE d.license_number = :licenseNumber")
    Optional<Doctor> findByLicenseNumber(@Param("licenseNumber") String licenseNumber);
    
    List<Doctor> findAll();
}
