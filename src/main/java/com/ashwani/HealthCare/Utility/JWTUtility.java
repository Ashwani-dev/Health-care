package com.ashwani.HealthCare.Utility;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JWTUtility {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration.ms}")
    private int expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Generate token with role
    public String generateToken(String userId, String role) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Validate token and return claims
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token expired", e);
        } catch (UnsupportedJwtException e) {
            throw new RuntimeException("Token format not supported", e);
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Invalid token structure", e);
        } catch (SecurityException e) {
            throw new RuntimeException("Invalid token signature", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Token is null/empty", e);
        } catch (Exception e) {
            throw new RuntimeException("Token validation failed", e);
        }
    }

    // Get user ID from token
    public String getUserIdFromToken(String token) {
        return validateToken(token).getSubject();
    }

    // Get role from token
    public String getRoleFromToken(String token) {
        return validateToken(token).get("role", String.class);
    }
}