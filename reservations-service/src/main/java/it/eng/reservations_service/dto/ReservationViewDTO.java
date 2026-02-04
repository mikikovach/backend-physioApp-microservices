package it.eng.reservations_service.dto;

import java.time.LocalDateTime;

public record ReservationViewDTO(Long id, Long slotId, Long userId, Long therapistId, String therapistName, String therapistSurname, LocalDateTime startTime) {
}
