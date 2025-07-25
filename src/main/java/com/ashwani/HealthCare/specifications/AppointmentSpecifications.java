package com.ashwani.HealthCare.specifications;

import com.ashwani.HealthCare.Entity.AppointmentEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentSpecifications {

    public static Specification<AppointmentEntity> hasDoctor(Long doctorId) {
        return (root, query, cb) ->
                cb.equal(root.get("doctor").get("id"), doctorId);
    }

    public static Specification<AppointmentEntity> hasPatient(Long patientId) {
        return (root, query, cb) ->
                cb.equal(root.get("patient").get("id"), patientId);
    }

    public static Specification<AppointmentEntity> hasAppointmentDate(LocalDate appointmentDate) {
        return (root, query, cb) ->
                appointmentDate == null ? null : cb.equal(root.get("appointmentDate"), appointmentDate);
    }

    public static Specification<AppointmentEntity> hasStartTime(LocalTime startTime) {
        return (root, query, cb) ->
                startTime == null ? null : cb.equal(root.get("startTime"), startTime);
    }

    public static Specification<AppointmentEntity> hasStatus(String status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }
}
