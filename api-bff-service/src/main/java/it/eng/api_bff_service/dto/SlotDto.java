package it.eng.api_bff_service.dto;

import java.time.LocalDateTime;

public record SlotDto(Long id, Long physioId, LocalDateTime startTime) {
}
