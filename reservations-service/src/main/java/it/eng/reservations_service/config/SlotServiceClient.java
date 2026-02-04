package it.eng.reservations_service.config;

import it.eng.reservations_service.dto.SlotDto;
import it.eng.reservations_service.exception.SlotAlreadyReservedInReservationContextException;
import it.eng.reservations_service.exception.SlotClientException;
import it.eng.reservations_service.exception.SlotServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class SlotServiceClient {

    private final WebClient webClient;

    public SlotServiceClient(@Qualifier("slotWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Void> reserveSlotRemotely(Long slotId) {
        return webClient.post()
                .uri("/slots/reserve/{slotId}", slotId)
                .retrieve()
                // BIZNIS GREŠKE
                .onStatus( status -> status.isSameCodeAs(HttpStatus.CONFLICT), response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("Slot already reserved")
                        .map(err -> new SlotAlreadyReservedInReservationContextException("Slot with id " + slotId + " is already reserved")))
                // KLIJENTSKE GREŠKE
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("Client error from Slot service")
                                .map(err -> new SlotClientException(err))
                )
                // SERVER GREŠKE
                .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(new SlotServiceUnavailableException(
                        "Slot service unavailable")))
                .bodyToMono(Void.class)
                // NETWORK / TIMEOUT / DNS
                .onErrorMap(
                        WebClientRequestException.class,
                        ex -> new SlotServiceUnavailableException("Slot service unavailable")
                );
    }

    public SlotDto fetchSlotBySlotId(Long slotId) {

        log.info("Fetching slot details for slotId {}", slotId);
        return webClient.get()
                .uri("/slots/findSlot/{slotId}", slotId)
                .retrieve()
                .bodyToMono(SlotDto.class)
                .block();
    }

    public Mono<Void> releaseSlot(Long slotId) {
        return webClient.post()
                .uri("/slots/release/{slotId}", slotId)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(ex -> {
                    log.error("Failed to release slot with id {}: {}", slotId, ex.getMessage());
                    return Mono.empty();
                });
    }



}
