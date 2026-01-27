package it.eng.reservations_service.repository;

import it.eng.reservations_service.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationsRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);




}
