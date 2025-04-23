package com.ashwani.HealthCare.Service;

import com.ashwani.HealthCare.Repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DoctorService {

    @Autowired
    private final DoctorRepository doctorRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public DoctorService(DoctorRepository doctorRepository,
                         BCryptPasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
    }
}
