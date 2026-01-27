package it.eng.auth_service.dto;

import java.time.LocalDate;

public record UserResponseDTO(Long userId, String firstName, String lastName, String email, LocalDate birthDate, String street, String city, Long postalCode) {
}
