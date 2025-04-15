package com.ashwani.HealthCare.Config;
import com.ashwani.HealthCare.Filter.JwtFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

public class FilterConfig {
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter filter) {
        FilterRegistrationBean<JwtFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/api/*"); // Protect all /api endpoints
        return registration;
    }
}
