package it.eng.reservations_service.service;

import it.eng.reservations_service.dto.ReservationDTO;

import java.util.List;

public interface ReservationsService {

//    List<ReservationDTO> getAllReservations();

//    ReservationDTO getReservationById(Long id);

    void createReservation(Long userId, Long slotId);

    List<ReservationDTO> getMyReservations(Long userId);

    void cancelReservation(Long id);
}
