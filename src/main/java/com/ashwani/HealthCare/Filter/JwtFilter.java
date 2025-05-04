package com.ashwani.HealthCare.Filter;
import com.ashwani.HealthCare.Utility.JWTUtility;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.security.Principal;
/*
@Component
public class JwtFilter implements Filter {
    private final JWTUtility jwtUtil;

    public JwtFilter(JWTUtility jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip filter for login/register endpoints
        if (httpRequest.getServletPath().startsWith("/api/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        // Get token from header
        String token = httpRequest.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid token");
            return;
        }

        try {
            // Validate token
            String userId = jwtUtil.validateTokenAndGetUserId(token.substring(7));
            httpRequest.setAttribute("userId", userId);
            chain.doFilter(request, response);
        } catch (RuntimeException e) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        }
    }
}
 */

@Component
public class JwtFilter implements Filter {
    private final JWTUtility jwtUtil;

    public JwtFilter(JWTUtility jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip filter for auth endpoints
        if (httpRequest.getServletPath().startsWith("/api/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        // Get token from header
        String token = httpRequest.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            sendUnauthorizedError(httpResponse, "Missing or invalid token");
            return;
        }

        try {
            String userId = jwtUtil.validateTokenAndGetUserId(token.substring(7));
            httpRequest.setAttribute("userId", userId);
            // Create authenticated request
            AuthenticatedRequest authenticatedRequest = new AuthenticatedRequest(httpRequest, userId);

            // Continue with the authenticated request
            chain.doFilter(authenticatedRequest, response);
        } catch (RuntimeException e) {
            sendUnauthorizedError(httpResponse, "Invalid token: " + e.getMessage());
        }
    }

    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(message);
        response.getWriter().flush();
    }

    // Custom HttpServletRequestWrapper to provide Principal
    private static class AuthenticatedRequest extends HttpServletRequestWrapper {
        private final String userId;

        public AuthenticatedRequest(HttpServletRequest request, String userId) {
            super(request);
            this.userId = userId;
        }

        @Override
        public Principal getUserPrincipal() {
            return () -> userId; // Simple Principal implementation
        }
    }
}
