package it.eng.api_bff_service.service;

import it.eng.api_bff_service.dto.PhysioDto;
import it.eng.api_bff_service.dto.ReservationDto;
import it.eng.api_bff_service.dto.ReservationViewDTO;
import it.eng.api_bff_service.dto.SlotDto;
import it.eng.api_bff_service.error.ApiError;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@AllArgsConstructor
@Slf4j
@Service
public class BffReservationService {

    private final WebClient reservationWebClient;
    private final WebClient physioWebClient;
    private final WebClient slotWebClient;

    public List<ReservationViewDTO> aggregateData(Long userId) {

        List<ReservationDto> reservations = fetchReservationsByUserId(userId);
        log.info("Fetched {} reservations for userId {}", reservations.size(), userId);

        return reservations.stream().map(reservation -> {
            SlotDto slotResponse = fetchSlotsBySlotId(reservation.slotId());
            PhysioDto physioResponse = fetchPhysioByPhysioId(slotResponse.physioId());

            return new ReservationViewDTO(reservation.id(), slotResponse.id(), userId, physioResponse.physioId(),
                      physioResponse.firstName(), physioResponse.lastName(), slotResponse.startTime());
        }).toList();

    }

    public List<ReservationDto> fetchReservationsByUserId(Long userId) {

        return reservationWebClient.get()
                .uri("http://localhost:8084/reservations/my-reservations")
                .header("X-User-Id", String.valueOf(userId))
                .retrieve()
                .bodyToFlux(ReservationDto.class)
                .collectList()
                .block();
    }

    public SlotDto fetchSlotsBySlotId(Long slotId) {

        log.info("Fetching slot details for slotId {}", slotId);
        return slotWebClient.get()
                .uri("http://localhost:8083/slots/findSlot/{slotId}", slotId)
                .retrieve()
                .bodyToMono(SlotDto.class)
                .block();
    }

    public PhysioDto fetchPhysioByPhysioId(Long physioId) {

        log.info("Fetching physiotherapist details for physioId {}", physioId);
       return physioWebClient.get()
                .uri("http://localhost:8082/physios/{physioId}", physioId)
                .retrieve()
                .bodyToMono(PhysioDto.class)
                .block();

    }
}
