package com.ashwani.HealthCare.Service;

import com.ashwani.HealthCare.DTO.DoctorDto;
import com.ashwani.HealthCare.Repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public List<DoctorDto> getAllDoctors(){
        return doctorRepository.findAll()
                .stream()
                .map(doctor-> modelMapper.map(doctor, DoctorDto.class))
                .collect(Collectors.toList());
    }

    public List<DoctorDto> searchDoctors(String specialization){
        return doctorRepository.findBySpecializationContainingIgnoreCase(specialization)
                .stream()
                .map(doctor-> modelMapper.map(doctor, DoctorDto.class))
                .collect((Collectors.toList()));
    }
}
