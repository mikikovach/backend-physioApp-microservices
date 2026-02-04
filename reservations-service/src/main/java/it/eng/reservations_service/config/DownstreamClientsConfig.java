package it.eng.reservations_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class DownstreamClientsConfig {

    private final WebClient.Builder webClientBuilder;


    public DownstreamClientsConfig(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Bean
    public WebClient slotWebClient() {
        return webClientBuilder
                .baseUrl("http://APPOINTMENTS-SERVICE")
                .build();
    }



    @Bean
    public WebClient physioWebClient() {
        return webClientBuilder
                .baseUrl("http://PHYSIO-SERVICE")
                .build();
    }
}