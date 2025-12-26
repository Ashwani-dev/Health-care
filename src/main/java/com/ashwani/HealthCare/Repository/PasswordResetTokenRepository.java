package com.ashwani.HealthCare.Repository;

import com.ashwani.HealthCare.Entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
    Optional<PasswordResetTokenEntity> findByToken(String token);

    Optional<PasswordResetTokenEntity> findByEmailAndUserType(String email, String userType);

    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity p WHERE p.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity p WHERE p.email = :email AND p.userType = :userType")
    void deleteByEmailAndUserType(@Param("email") String email, @Param("userType") String userType);
}

