package it.eng.appointments_service.dto;



import java.time.LocalDateTime;

public record AppointmentSlotDTO(Long id, LocalDateTime startTime, boolean reserved, Long physioId) {

}
