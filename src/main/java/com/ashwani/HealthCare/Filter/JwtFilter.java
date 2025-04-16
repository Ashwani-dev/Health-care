package com.ashwani.HealthCare.Filter;
import com.ashwani.HealthCare.Utility.JWTUtility;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;

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
        if (httpRequest.getServletPath().startsWith("/api/auth/") ||
                httpRequest.getServletPath().equals("/api/users/register")) {
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
