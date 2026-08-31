package it.eng.reservations_service.controller;

import it.eng.reservations_service.dto.ReservationRequestDTO;
import it.eng.reservations_service.dto.ReservationViewDTO;
import it.eng.reservations_service.service.ReservationsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/reservations")
@AllArgsConstructor
@Slf4j
@Validated
public class ReservationController {

    private ReservationsService reservationService;


    @PostMapping
    public ResponseEntity<Void> createReservation(
            @Valid @RequestBody ReservationRequestDTO reservationRequest,
            @NotNull(message = "User ID is required")
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Creating reservation for userId: {} and slotId: {}", userId, reservationRequest.slotId());
        reservationService.createReservation(userId, reservationRequest.slotId()).block();
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/my-reservations")
    public List<ReservationViewDTO> getMyReservations(
            @NotNull(message = "User ID is required")
            @RequestHeader("X-User-Id") String userId) {
        return reservationService.getMyReservations(Long.parseLong(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(
            @Positive(message = "Reservation ID must be a positive number")
            @PathVariable Long id) {
        reservationService.cancelReservation(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
