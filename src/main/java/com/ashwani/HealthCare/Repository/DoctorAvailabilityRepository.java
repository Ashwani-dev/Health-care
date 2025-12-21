package com.ashwani.HealthCare.Repository;

import com.ashwani.HealthCare.Entity.DoctorAvailability;
import com.ashwani.HealthCare.Entity.DoctorEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.util.List;


public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    @Query("SELECT da FROM DoctorAvailability da " +
           "JOIN FETCH da.doctor " +
           "WHERE da.doctor = :doctor AND da.dayOfWeek = :dayOfWeek")
    List<DoctorAvailability> findByDoctorAndDayOfWeek(@Param("doctor") DoctorEntity doctor,
                                                       @Param("dayOfWeek") DayOfWeek dayOfWeek);

    @Query("SELECT da FROM DoctorAvailability da " +
           "JOIN FETCH da.doctor " +
           "WHERE da.doctor.id = :id")
    List<DoctorAvailability> findByDoctorId(@Param("id") Long id);

    void deleteByDoctorAndId(DoctorEntity doctor, Long id);

    @Transactional
    void deleteByDoctor(DoctorEntity doctor);
}
