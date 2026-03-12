package it.eng.reservations_service.controller;

import it.eng.reservations_service.dto.ReservationViewDTO;
import it.eng.reservations_service.service.ReservationsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/reservations")
@AllArgsConstructor
@Slf4j
public class ReservationController {

    ReservationsService reservationService;


    @PostMapping
    public ResponseEntity<Void> createReservation(@RequestBody Long slotId, @RequestHeader("X-User-Id") Long userId) {
        log.info("Creating reservation for userId: " + userId + " and slotId: " + slotId);
        reservationService.createReservation(userId, slotId).block();
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/my-reservations")
    public List<ReservationViewDTO> getMyReservations(@RequestHeader("X-User-Id") String userId) {
        return reservationService.getMyReservations(Long.parseLong(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
