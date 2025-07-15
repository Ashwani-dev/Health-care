package com.ashwani.HealthCare.Service;

import com.ashwani.HealthCare.DTO.DoctorDto;
import com.ashwani.HealthCare.Entity.DoctorEntity;
import com.ashwani.HealthCare.Repository.DoctorRepository;
import com.ashwani.HealthCare.specifications.DoctorSpecifications;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final ModelMapper modelMapper;

    private DoctorDto convertToDto(DoctorEntity doctor) {
        return modelMapper.map(doctor, DoctorDto.class);
    }

    private Specification<DoctorEntity> buildSearchSpecification(String specialization, String name) {
        return Specification.where(DoctorSpecifications.hasSpecialization(specialization))
                .and(DoctorSpecifications.nameContains(name));
    }

    public List<DoctorDto> searchDoctors(String specialization, String name) {
        Specification<DoctorEntity> spec = buildSearchSpecification(specialization, name);
        return doctorRepository.findAll(spec)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}
