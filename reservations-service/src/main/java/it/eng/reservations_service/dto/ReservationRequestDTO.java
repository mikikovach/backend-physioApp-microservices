package it.eng.reservations_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for creating a reservation.
 * Contains the slot ID that the user wants to reserve.
 */
public record ReservationRequestDTO(
        @NotNull(message = "Slot ID is required")
        @Positive(message = "Slot ID must be a positive number")
        Long slotId
) {
}
