package it.eng.reservations_service.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ResilienceConfig {

    @Bean
    public Retry slotRetry(RetryRegistry registry) {
        return registry.retry("slot-service");
    }

    @Bean
    public CircuitBreaker slotCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("slot-service");
    }


}
