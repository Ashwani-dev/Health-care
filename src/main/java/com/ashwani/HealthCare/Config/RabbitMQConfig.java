package com.ashwani.HealthCare.Config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * Configure RabbitAdmin to manage RabbitMQ resources.
     * ignoreDeclarationExceptions=true allows the application to start even if
     * queues already exist with different properties (e.g., from previous runs).
     * This prevents PRECONDITION_FAILED errors when queue properties don't match.
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setIgnoreDeclarationExceptions(true);
        return admin;
    }
    
    /**
     * Configure RabbitListenerContainerFactory to auto-declare queues.
     * This ensures queues are created automatically if they don't exist.
     * 
     * Note: Queue declaration is now handled dynamically by the @RabbitListener
     * annotation in PaymentEventListener. This approach is more flexible as it:
     * 1. Works with existing queues (binds to them if they exist)
     * 2. Creates queues dynamically if they don't exist
     * 3. Avoids conflicts when queue properties differ
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setAutoStartup(true);
        return factory;
    }
}
