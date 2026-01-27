package it.eng.service_registry_eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryEurekaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceRegistryEurekaApplication.class, args);
	}

}
