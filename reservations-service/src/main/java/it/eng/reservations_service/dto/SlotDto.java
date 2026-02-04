package it.eng.reservations_service.dto;

import java.time.LocalDateTime;

public record SlotDto(Long id, Long physioId, LocalDateTime startTime) {
}
