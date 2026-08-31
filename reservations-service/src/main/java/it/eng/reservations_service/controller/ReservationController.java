package it.eng.reservations_service.controller;

import it.eng.reservations_service.dto.ReservationRequestDTO;
import it.eng.reservations_service.dto.ReservationViewDTO;
import it.eng.reservations_service.service.ReservationsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@RestController
@RequestMapping("/reservations")
@AllArgsConstructor
@Slf4j
@Validated
public class ReservationController {

    private static final String POSITIVE_NUMERIC_USER_ID_PATTERN = "^[1-9]\\d*$";
    private static final String USER_ID_REQUIRED_MESSAGE = "User ID is required";
    private static final String USER_ID_POSITIVE_MESSAGE = "User ID must be a positive number";

    private final ReservationsService reservationService;


    /**
     * Creates a new reservation for the authenticated user.
     *
     * @param reservationRequest reservation payload containing the slot identifier
     * @param userIdHeader authenticated user identifier propagated by the gateway
     * @return empty response with HTTP 201 status when the reservation is created
     */
    @PostMapping
    public ResponseEntity<Void> createReservation(
            @Valid @RequestBody ReservationRequestDTO reservationRequest,
            @NotBlank(message = USER_ID_REQUIRED_MESSAGE)
            @Pattern(regexp = POSITIVE_NUMERIC_USER_ID_PATTERN, message = USER_ID_POSITIVE_MESSAGE)
            @RequestHeader("X-User-Id") String userIdHeader) {
        Long userId = parseUserId(userIdHeader);
        log.info("Creating reservation for userId: {} and slotId: {}", userId, reservationRequest.slotId());
        reservationService.createReservation(userId, reservationRequest.slotId()).block();
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Returns reservations that belong to the authenticated user.
     *
     * @param userIdHeader authenticated user identifier propagated by the gateway
     * @return all reservations owned by the authenticated user
     */
    @GetMapping("/my-reservations")
    public List<ReservationViewDTO> getMyReservations(
            @NotBlank(message = USER_ID_REQUIRED_MESSAGE)
            @Pattern(regexp = POSITIVE_NUMERIC_USER_ID_PATTERN, message = USER_ID_POSITIVE_MESSAGE)
            @RequestHeader("X-User-Id") String userIdHeader) {
        return reservationService.getMyReservations(parseUserId(userIdHeader));
    }

    /**
     * Cancels an existing reservation by its identifier.
     *
     * @param id reservation identifier
     * @return empty response with HTTP 204 status when the reservation is canceled
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(
            @Positive(message = "Reservation ID must be a positive number")
            @PathVariable Long id) {
        reservationService.cancelReservation(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private Long parseUserId(String userIdHeader) {
        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_ID_POSITIVE_MESSAGE, ex);
        }
    }
}
