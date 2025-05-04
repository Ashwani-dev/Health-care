package com.ashwani.HealthCare.Service;

import com.ashwani.HealthCare.DTO.DoctorDto;
import com.ashwani.HealthCare.Repository.AppointmentRepository;
import com.ashwani.HealthCare.Repository.DoctorAvailabilityRepository;
import com.ashwani.HealthCare.Repository.DoctorRepository;
import com.ashwani.HealthCare.Repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PatientService {

    @Autowired
    private final PatientRepository patientRepository;

    @Autowired
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private final DoctorRepository doctorRepository;

    @Autowired
    private final AppointmentRepository appointmentRepository;

    @Autowired
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;


    public PatientService(PatientRepository patientRepository,
                          BCryptPasswordEncoder passwordEncoder, DoctorRepository doctorRepository, AppointmentRepository appointmentRepository, DoctorAvailabilityRepository doctorAvailabilityRepository) {
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
    }
}
