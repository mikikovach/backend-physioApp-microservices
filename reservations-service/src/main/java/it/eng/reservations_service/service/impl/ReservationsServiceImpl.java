package it.eng.reservations_service.service.impl;


import it.eng.reservations_service.config.PhysioServiceClient;
import it.eng.reservations_service.config.SlotServiceClient;
import it.eng.reservations_service.dto.PhysioDto;
import it.eng.reservations_service.dto.ReservationViewDTO;
import it.eng.reservations_service.dto.SlotDto;
import it.eng.reservations_service.entity.Reservation;
import it.eng.reservations_service.exception.ReservationNotFoundException;
import it.eng.reservations_service.exception.SlotAlreadyReservedInReservationContextException;
import it.eng.reservations_service.repository.ReservationsRepository;
import it.eng.reservations_service.service.ReservationsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;


@Service
@AllArgsConstructor
@Slf4j
public class ReservationsServiceImpl implements ReservationsService {

    private final ReservationsRepository reservationsRepository;
    private final SlotServiceClient slotServiceClient;
    private final PhysioServiceClient physioServiceClient;

    @Override
    public Mono<Reservation> createReservation(Long userId, Long slotId) {

        return slotServiceClient.reserveSlotRemotely(slotId)
                .then(Mono.fromCallable(() -> {
                    Reservation reservation = new Reservation();
                    reservation.setUserId(userId);
                    reservation.setSlotId(slotId);
                    reservation.setCreatedAt(LocalDateTime.now());

                    return reservationsRepository.save(reservation);

                }))
                .onErrorResume( ex -> {
                            if (ex instanceof SlotAlreadyReservedInReservationContextException) {
                                return Mono.error(ex);
                            }
                            return slotServiceClient.releaseSlot(slotId).then(Mono.error(ex));
                        }
                );
    }



    @Override
    public List<ReservationViewDTO> getMyReservations(Long userId) {

    return reservationsRepository.findByUserId(userId)
            .stream()
            .map(reservation -> {
                SlotDto slotResponse = slotServiceClient.fetchSlotBySlotId(reservation.getSlotId());
                PhysioDto physioResponse = physioServiceClient.fetchPhysioByPhysioId(slotResponse.physioId());

                return new ReservationViewDTO(reservation.getId(), slotResponse.id(), userId, physioResponse.physioId(),
                        physioResponse.firstName(), physioResponse.lastName(), slotResponse.startTime());
            }).toList();
    }

    @Override
    public void cancelReservation(Long id) {
        Reservation reservation = reservationsRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + id));

        reservationsRepository.delete(reservation);
        if(reservation.getSlotId() != null){
            log.info("Releasing slot with id: {} for canceled reservation id: {}", reservation.getSlotId(), id);
            slotServiceClient.releaseSlot(reservation.getSlotId()).subscribe();
        }


    }



//    public Mono<Void> reserveSlotRemotely(Long slotId) {
//        return slotWebClient.post()
//                .uri("/slots/reserve/{slotId}", slotId)
//                .retrieve()
//                // BIZNIS GREŠKE
//                .onStatus( status -> status.isSameCodeAs(HttpStatus.CONFLICT), response -> response.bodyToMono(String.class)
//                        .defaultIfEmpty("Slot already reserved")
//                        .map(err -> new SlotAlreadyReservedInReservationContextException("Slot with id " + slotId + " is already reserved")))
//                // KLIJENTSKE GREŠKE
//                .onStatus(
//                        HttpStatusCode::is4xxClientError,
//                        response -> response.bodyToMono(String.class)
//                                .defaultIfEmpty("Client error from Slot service")
//                                .map(err -> new SlotClientException(err))
//                )
//                // SERVER GREŠKE
//                .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(new SlotServiceUnavailableException(
//                        "Slot service unavailable")))
//                .bodyToMono(Void.class)
//                // NETWORK / TIMEOUT / DNS
//                .onErrorMap(
//                        WebClientRequestException.class,
//                        ex -> new SlotServiceUnavailableException("Slot service unavailable")
//                );
//    }


//    public List<ReservationDTO> fetchReservationsByUserId(Long userId) {
//
//        return reservationWebClient.get()
//                .uri("http://localhost:8084/reservations/my-reservations")
//                .header("X-User-Id", String.valueOf(userId))
//                .retrieve()
//                .bodyToFlux(ReservationDTO.class)
//                .collectList()
//                .block();
//    }

//    public SlotDto fetchSlotBySlotId(Long slotId) {
//
//        log.info("Fetching slot details for slotId {}", slotId);
//        return slotWebClient.get()
//                .uri("/slots/findSlot/{slotId}", slotId)
//                .retrieve()
//                .bodyToMono(SlotDto.class)
//                .block();
//    }
//    public PhysioDto fetchPhysioByPhysioId(Long physioId) {
//
//        log.info("Fetching physiotherapist details for physioId {}", physioId);
//        return physioWebClient.get()
//                .uri("/physios/{physioId}", physioId)
//                .retrieve()
//                .bodyToMono(PhysioDto.class)
//                .block();
//
//    }

}
