package it.eng.reservations_service.config;

import it.eng.reservations_service.dto.PhysioDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class PhysioServiceClient {

    private final WebClient webClient;

    public PhysioServiceClient( @Qualifier("physioWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public PhysioDto fetchPhysioByPhysioId(Long physioId) {

        log.info("Fetching physiotherapist details for physioId {}", physioId);
        return webClient.get()
                .uri("/physios/{physioId}", physioId)
                .retrieve()
                .bodyToMono(PhysioDto.class)
                .block();

    }


}
