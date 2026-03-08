package com.ashwani.HealthCare.Repository;

import com.ashwani.HealthCare.Entity.VideoCallSessions;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VideoCallSessionsRepository extends JpaRepository<VideoCallSessions, Long> {

    @EntityGraph(attributePaths = {"appointment", "appointment.patient", "appointment.doctor"})
    Optional<VideoCallSessions> findByAppointmentId(Long appointmentId);

    /**
     * Gets patient access token if the user is authorized (is the patient for this appointment)
     * Combines authorization check with data retrieval in single query
     */
    @Query("SELECT v.patientAccessToken FROM VideoCallSessions v " +
            "WHERE v.appointment.id = :appointmentId " +
            "AND v.appointment.patient.id = :userId " +
            "AND v.patientAccessToken IS NOT NULL")
    Optional<String> findPatientAccessToken(@Param("appointmentId") Long appointmentId,
                                            @Param("userId") Long userId);

    /**
     * Gets doctor access token if the user is authorized (is the doctor for this appointment)
     * Combines authorization check with data retrieval in single query
     */
    @Query("SELECT v.doctorAccessToken FROM VideoCallSessions v " +
            "WHERE v.appointment.id = :appointmentId " +
            "AND v.appointment.doctor.id = :userId " +
            "AND v.doctorAccessToken IS NOT NULL")
    Optional<String> findDoctorAccessToken(@Param("appointmentId") Long appointmentId,
                                           @Param("userId") Long userId);

    /**
     * Checks if a video session exists for the appointment and the user has access to it
     * Useful for detailed error handling
     */
    @Query("SELECT COUNT(v) > 0 FROM VideoCallSessions v " +
            "WHERE v.appointment.id = :appointmentId " +
            "AND (v.appointment.patient.id = :userId OR v.appointment.doctor.id = :userId)")
    boolean existsByAppointmentIdAndUserId(@Param("appointmentId") Long appointmentId,
                                           @Param("userId") Long userId);

    /**
     * Checks if video session exists for appointment (regardless of user authorization)
     * Useful for distinguishing between "session not found" vs "unauthorized access"
     */
    @Query("SELECT COUNT(v) > 0 FROM VideoCallSessions v " +
            "WHERE v.appointment.id = :appointmentId")
    boolean existsByAppointmentId(@Param("appointmentId") Long appointmentId);
}
