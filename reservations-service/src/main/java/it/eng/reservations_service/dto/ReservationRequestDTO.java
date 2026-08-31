package it.eng.reservations_service.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating a reservation.
 * Contains the slot ID that the user wants to reserve.
 */
public record ReservationRequestDTO(
        @NotNull(message = "Slot ID is required")
        Long slotId
) {
}
