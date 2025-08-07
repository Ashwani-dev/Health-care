package com.ashwani.HealthCare.Filter;
import com.ashwani.HealthCare.Utility.JWTUtility;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JWTUtility jwtUtility;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Skip JWT validation for webhook endpoints
        if (isWebhookEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String jwt = getJwtFromRequest(request);

            if (jwt != null) {
                // Validate token and extract claims once
                Claims claims = jwtUtility.validateToken(jwt);
                String userId = claims.getSubject();
                String role = claims.get("role", String.class);

                /*
                // Validate role against endpoint
                if (isDoctorEndpoint(request) && !"DOCTOR".equals(role)) {
                    throw new AccessDeniedException("Doctor access required");
                }

                if (isPatientEndpoint(request) && !"PATIENT".equals(role)) {
                    throw new AccessDeniedException("Patient access required");
                }
                 */

                UserDetails userDetails = createUserDetails(userId, role);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            throw new ServletException("JWT validation failed", ex);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isDoctorEndpoint(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/doctor/");
    }

    private boolean isPatientEndpoint(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/patient/");
    }

    private boolean isWebhookEndpoint(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/payments/webhook/") ||
               request.getRequestURI().startsWith("/api/video-call/webhook");
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private UserDetails createUserDetails(String userId, String role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        return new org.springframework.security.core.userdetails.User(
                userId, "", authorities);
    }
}
