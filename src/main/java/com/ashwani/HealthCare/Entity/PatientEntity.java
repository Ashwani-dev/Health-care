package com.ashwani.HealthCare.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Setter
@Getter
@EntityListeners(AuditingEntityListener.class)
public class PatientEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 20, message = "Full name must be between 3 and 20 characters")
    private String full_name;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @Column(nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Contact number must be 10-15 digits")
    private String contact_number;

    @Column(nullable = false)
    @NotBlank(message = "Address is required")
    private String address;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
