package com.ashwani.HealthCare.specifications;

import com.ashwani.HealthCare.Entity.DoctorEntity;
import org.springframework.data.jpa.domain.Specification;

public class DoctorSpecifications {

    // Filter by specialization
    public static Specification<DoctorEntity> hasSpecialization(String specialization) {
        return (root, query, cb) ->
                specialization == null ? null :
                        cb.equal(root.get("specialization"), specialization);
    }

    // Filter by name (case-insensitive partial match)
    public static Specification<DoctorEntity> nameContains(String full_name) {
        return (root, query, cb) ->
                full_name == null ? null :
                        cb.like(cb.lower(root.get("full_name")), "%" + full_name.toLowerCase() + "%");
    }
}
