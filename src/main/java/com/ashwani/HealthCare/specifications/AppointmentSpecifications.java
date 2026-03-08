package com.ashwani.HealthCare.specifications;

import com.ashwani.HealthCare.Entity.Appointment;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentSpecifications {

    public static Specification<Appointment> hasDoctor(Long doctorId) {
        return (root, query, cb) -> {
            // Add fetch joins to prevent N+1 queries
            if (query.getResultType().equals(Appointment.class)) {
                root.fetch("doctor", JoinType.LEFT);
                root.fetch("patient", JoinType.LEFT);
                root.fetch("paymentDetails", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.equal(root.get("doctor").get("id"), doctorId);
        };
    }

    public static Specification<Appointment> hasPatient(Long patientId) {
        return (root, query, cb) -> {
            // Add fetch joins to prevent N+1 queries
            if (query.getResultType().equals(Appointment.class)) {
                root.fetch("patient", JoinType.LEFT);
                root.fetch("doctor", JoinType.LEFT);
                root.fetch("paymentDetails", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.equal(root.get("patient").get("id"), patientId);
        };
    }

    public static Specification<Appointment> hasAppointmentDate(LocalDate appointmentDate) {
        return (root, query, cb) ->
                appointmentDate == null ? null : cb.equal(root.get("appointmentDate"), appointmentDate);
    }

    public static Specification<Appointment> hasAppointmentDateRange(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) {
                return null;
            }
            if (startDate != null && endDate != null) {
                return cb.between(root.get("appointmentDate"), startDate, endDate);
            }
            if (startDate != null) {
                return cb.greaterThanOrEqualTo(root.get("appointmentDate"), startDate);
            }
            return cb.lessThanOrEqualTo(root.get("appointmentDate"), endDate);
        };
    }

    public static Specification<Appointment> hasStartTime(LocalTime startTime) {
        return (root, query, cb) ->
                startTime == null ? null : cb.equal(root.get("startTime"), startTime);
    }

    public static Specification<Appointment> hasTimeRange(LocalTime startTime, LocalTime endTime) {
        return (root, query, cb) -> {
            if (startTime == null && endTime == null) {
                return null;
            }
            if (startTime != null && endTime != null) {
                return cb.between(root.get("startTime"), startTime, endTime);
            }
            if (startTime != null) {
                return cb.greaterThanOrEqualTo(root.get("startTime"), startTime);
            }
            return cb.lessThanOrEqualTo(root.get("startTime"), endTime);
        };
    }

    public static Specification<Appointment> hasStatus(String status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }
}
