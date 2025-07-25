package com.ashwani.HealthCare.Repository;

import com.ashwani.HealthCare.Entity.AppointmentEntity;
import com.ashwani.HealthCare.Entity.DoctorEntity;
import com.ashwani.HealthCare.Entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


public interface AppointmentRepository extends
        JpaRepository<AppointmentEntity, Long>,
        JpaSpecificationExecutor<AppointmentEntity> {
    List<AppointmentEntity> findByDoctorAndAppointmentDate(DoctorEntity doctor, LocalDate date);
    List<AppointmentEntity> findByPatient(PatientEntity patient);
    List<AppointmentEntity> findByDoctor(DoctorEntity doctor);
    boolean existsByDoctorAndAppointmentDateAndStartTime(DoctorEntity doctor, LocalDate date,
                                                         LocalTime startTime);
}
