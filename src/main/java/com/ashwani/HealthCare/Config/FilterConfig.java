package com.ashwani.HealthCare.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FilterConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean // Makes ModelMapper available for dependency injection
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean // RestTemplate for making HTTP requests (used by payment gateways)
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean // ObjectMapper for JSON serialization/deserialization
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Register JavaTimeModule to handle Java 8 date/time types (LocalDate, LocalDateTime, etc.)
        mapper.registerModule(new JavaTimeModule());
        // Disable writing dates as timestamps (use ISO-8601 format instead)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
