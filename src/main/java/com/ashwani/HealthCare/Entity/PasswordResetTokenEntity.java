package com.ashwani.HealthCare.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PasswordResetTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String userType; // "PATIENT" or "DOCTOR"

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean used = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PasswordResetTokenEntity(String token, String email, String userType, LocalDateTime expiryDate) {
        this.token = token;
        this.email = email;
        this.userType = userType;
        this.expiryDate = expiryDate;
        this.used = false;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}

