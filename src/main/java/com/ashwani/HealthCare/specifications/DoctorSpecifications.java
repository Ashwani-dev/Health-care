package com.ashwani.HealthCare.specifications;

import com.ashwani.HealthCare.Entity.DoctorEntity;
import org.springframework.data.jpa.domain.Specification;

public class DoctorSpecifications {

    // Filter by specialization (null-safe, case-insensitive partial match)
    public static Specification<DoctorEntity> hasSpecialization(String specialization) {
        return (root, query, cb) ->
                specialization == null || specialization.isEmpty()
                        ? cb.conjunction()  // Returns no filtering condition if null/empty
                        : cb.like(
                        cb.lower(root.get("specialization")),
                        "%" + specialization.toLowerCase() + "%"
                );
    }

    // Filter by name (null-safe, case-insensitive partial match)
    public static Specification<DoctorEntity> nameContains(String fullName) {
        return (root, query, cb) ->
                fullName == null || fullName.isEmpty()
                        ? cb.conjunction()
                        : cb.like(
                        cb.lower(root.get("full_name")),  // Ensure field name matches your entity
                        "%" + fullName.toLowerCase() + "%"
                );
    }
}
