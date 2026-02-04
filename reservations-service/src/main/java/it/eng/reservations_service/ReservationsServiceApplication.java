package it.eng.reservations_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class ReservationsServiceApplication {

	@Bean
	@LoadBalanced
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}

	public static void main(String[] args) {
		SpringApplication.run(ReservationsServiceApplication.class, args);
	}

}
