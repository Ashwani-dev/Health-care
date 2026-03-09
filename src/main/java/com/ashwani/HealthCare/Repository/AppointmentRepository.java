package com.ashwani.HealthCare.Repository;

import com.ashwani.HealthCare.Entity.Appointment;
import com.ashwani.HealthCare.Entity.Doctor;
import com.ashwani.HealthCare.Entity.Patient;
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
        JpaRepository<Appointment, Long>,
        JpaSpecificationExecutor<Appointment> {

    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.doctor " +
           "WHERE a.doctor = :doctor AND a.appointmentDate = :date")
    List<Appointment> findByDoctorAndAppointmentDate(@Param("doctor") Doctor doctor,
                                                     @Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.doctor " +
           "JOIN FETCH a.patient " +
           "WHERE a.patient = :patient")
    List<Appointment> findByPatient(@Param("patient") Patient patient);

    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.patient " +
           "JOIN FETCH a.doctor " +
           "WHERE a.doctor = :doctor")
    List<Appointment> findByDoctor(@Param("doctor") Doctor doctor);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Appointment a " +
           "WHERE a.doctor = :doctor AND a.appointmentDate = :date AND a.startTime = :startTime")
    boolean existsByDoctorAndAppointmentDateAndStartTime(@Param("doctor") Doctor doctor,
                                                         @Param("date") LocalDate date,
                                                         @Param("startTime") LocalTime startTime);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Appointment a " +
           "WHERE a.doctor = :doctor AND a.appointmentDate = :date AND a.startTime = :startTime AND a.id != :appointmentId")
    boolean existsByDoctorAndAppointmentDateAndStartTimeAndIdNot(@Param("doctor") Doctor doctor,
                                                                  @Param("date") LocalDate date,
                                                                  @Param("startTime") LocalTime startTime,
                                                                  @Param("appointmentId") Long appointmentId);

    @EntityGraph(attributePaths = {"patient", "doctor", "paymentDetails"})
    @NonNull
    Optional<Appointment> findById(@NonNull Long id);

    @Query("SELECT a FROM Appointment a WHERE a.paymentDetails.id = :paymentId")
    Optional<Appointment> findByPaymentId(@Param("paymentId") Long paymentId);
}
