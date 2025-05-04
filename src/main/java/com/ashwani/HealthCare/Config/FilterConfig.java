package com.ashwani.HealthCare.Config;
import com.ashwani.HealthCare.Filter.JwtFilter;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter filter) {
        FilterRegistrationBean<JwtFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/api/*"); // Protect all /api endpoints
        return registration;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean // Makes ModelMapper available for dependency injection
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
