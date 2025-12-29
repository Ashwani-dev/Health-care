package com.ashwani.HealthCare.Repository;

import com.ashwani.HealthCare.Entity.AppointmentEntity;
import com.ashwani.HealthCare.Entity.DoctorEntity;
import com.ashwani.HealthCare.Entity.PatientEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;


public interface AppointmentRepository extends
        JpaRepository<AppointmentEntity, Long>,
        JpaSpecificationExecutor<AppointmentEntity> {

    @Query("SELECT a FROM AppointmentEntity a " +
           "JOIN FETCH a.doctor " +
           "WHERE a.doctor = :doctor AND a.appointmentDate = :date")
    List<AppointmentEntity> findByDoctorAndAppointmentDate(@Param("doctor") DoctorEntity doctor,
                                                           @Param("date") LocalDate date);

    @Query("SELECT a FROM AppointmentEntity a " +
           "JOIN FETCH a.doctor " +
           "JOIN FETCH a.patient " +
           "WHERE a.patient = :patient")
    List<AppointmentEntity> findByPatient(@Param("patient") PatientEntity patient);

    @Query("SELECT a FROM AppointmentEntity a " +
           "JOIN FETCH a.patient " +
           "JOIN FETCH a.doctor " +
           "WHERE a.doctor = :doctor")
    List<AppointmentEntity> findByDoctor(@Param("doctor") DoctorEntity doctor);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AppointmentEntity a " +
           "WHERE a.doctor = :doctor AND a.appointmentDate = :date AND a.startTime = :startTime")
    boolean existsByDoctorAndAppointmentDateAndStartTime(@Param("doctor") DoctorEntity doctor,
                                                         @Param("date") LocalDate date,
                                                         @Param("startTime") LocalTime startTime);

    @EntityGraph(attributePaths = {"patient", "doctor", "paymentDetails"})
    @NonNull
    Optional<AppointmentEntity> findById(@NonNull Long id);
}
