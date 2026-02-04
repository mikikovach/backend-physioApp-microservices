package it.eng.reservations_service.service;

import it.eng.reservations_service.dto.ReservationViewDTO;
import it.eng.reservations_service.entity.Reservation;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReservationsService {

//    List<ReservationDTO> getAllReservations();

//    ReservationDTO getReservationById(Long id);

    public Mono<Reservation> createReservation(Long userId, Long slotId);

    List<ReservationViewDTO> getMyReservations(Long userId);

    void cancelReservation(Long id);
}
