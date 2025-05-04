package com.ashwani.HealthCare.Repository;

import com.ashwani.HealthCare.Entity.DoctorAvailability;
import com.ashwani.HealthCare.Entity.DoctorEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;


public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    List<DoctorAvailability> findByDoctorAndDayOfWeek(DoctorEntity doctor, DayOfWeek dayOfWeek);
    List<DoctorAvailability> findByDoctorId(Long id);
    void deleteByDoctorAndId(DoctorEntity doctor, Long id);
    @Transactional
    void deleteByDoctor(DoctorEntity doctor);
}
