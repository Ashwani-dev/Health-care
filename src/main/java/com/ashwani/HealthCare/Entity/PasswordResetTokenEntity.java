package com.ashwani.HealthCare.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
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

