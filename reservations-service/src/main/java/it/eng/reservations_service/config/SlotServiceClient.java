package it.eng.reservations_service.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
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
    private final Retry slotRetry;
    private final CircuitBreaker slotCircuitBreaker;

    public SlotServiceClient(@Qualifier("slotWebClient") WebClient webClient, Retry slotRetry, CircuitBreaker slotCircuitBreaker) {
        this.webClient = webClient;
        this.slotRetry = slotRetry;
        this.slotCircuitBreaker = slotCircuitBreaker;
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
                                .map(SlotClientException::new)
                )
                // SERVER GREŠKE
                .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(new SlotServiceUnavailableException(
                        "Slot service unavailable")))
                .bodyToMono(Void.class)

                // NETWORK / TIMEOUT / DNS
                .onErrorMap(
                        WebClientRequestException.class,
                        ex -> new SlotServiceUnavailableException("Slot service unavailable")
                )
//                .transformDeferred(RetryOperator.of(slotRetry))
                .transformDeferred(CircuitBreakerOperator.of(slotCircuitBreaker));



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
