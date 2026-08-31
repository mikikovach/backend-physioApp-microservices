package it.eng.appointments_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record AppointmentSlotInsertRequest(
        @NotNull(message = "startTime is required")
        @Future(message = "startTime must be in the future")
        LocalDateTime startTime,

        @NotNull(message = "physioId is required")
        @Positive(message = "physioId must be greater than 0")
        Long physioId
) {
}
