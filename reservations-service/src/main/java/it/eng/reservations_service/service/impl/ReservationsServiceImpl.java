package it.eng.reservations_service.service.impl;


import it.eng.reservations_service.dto.CanReserveResponse;
import it.eng.reservations_service.dto.ReservationDTO;
import it.eng.reservations_service.entity.Reservation;
import it.eng.reservations_service.exception.ReservationNotFoundException;
import it.eng.reservations_service.mapper.ReservationsMapper;
import it.eng.reservations_service.repository.ReservationsRepository;
import it.eng.reservations_service.service.ReservationsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;


@Service
@AllArgsConstructor
@Slf4j
public class ReservationsServiceImpl implements ReservationsService {

    private final ReservationsMapper reservationsMapper;
    private final ReservationsRepository reservationsRepository;
    private WebClient slotWebClient;

//    private final AppointmentSlotRepository slotRepository;
//    @Override
//    public List<ReservationDTO> getAllReservations() {
//    return reservationsRepository.findAll()
//            .stream()
//            .map(reservationsMapper::toDto)
//            .toList();
//
//    }

//    @Override
//    public ReservationDTO getReservationById(Long id) {
//        return reservationsRepository.findById(id)
//                .map(reservationsMapper::toDto)
//                .orElseThrow(() -> new NotFoundException("Reservation not found with id: " + id));
//    }

    @Transactional
    @Override
    public void createReservation(Long userId, Long slotId) {

           reserveSlotRemotely(slotId).block();

           Reservation reservation = new Reservation();
           reservation.setUserId(userId);
           reservation.setSlotId(slotId);
           reservation.setCreatedAt(LocalDateTime.now());

           reservationsRepository.save(reservation);


    }
    @Override
    public List<ReservationDTO> getMyReservations(Long userId) {

    return reservationsRepository.findByUserId(userId)
            .stream()
            .map(reservationsMapper::entitytoDto)
            .toList();
    }

    @Override
    public void cancelReservation(Long id) {
        Reservation reservation = reservationsRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + id));

        reservationsRepository.delete(reservation);
    }

    public CanReserveResponse checkAvailability(Long slotId) {
        log.info("Check availability metoda u reservation servisu pozvana za slotId: {}", slotId);

        CanReserveResponse response = slotWebClient.get()
                .uri("http://localhost:8083/slots/availability/" + slotId)
                .retrieve()
                .bodyToMono(CanReserveResponse.class)
                .block();

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to check slot availability");
        }

        return response;
    }

    public Mono<Void> reserveSlotRemotely(Long slotId) {
        return slotWebClient.post()
                .uri("http://localhost:8083/slots/reserve/{slotId}", slotId)
                .retrieve()
//                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
//                        .map(body -> new ResponseStatusException(response.statusCode(), body)))
//                .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(new ResponseStatusException(
//                        HttpStatus.SERVICE_UNAVAILABLE,
//                        "Slot service unavailable")))
                .onStatus(HttpStatusCode::isError,response -> response.createException().flatMap(Mono::error))
                .bodyToMono(Void.class);
    }


}
