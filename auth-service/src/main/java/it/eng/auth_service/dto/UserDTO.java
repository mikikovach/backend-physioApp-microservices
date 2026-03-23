package it.eng.auth_service.dto;

import java.time.LocalDate;


public record UserDTO(Long userId, String firstName, String lastName, String email, String password, LocalDate birthDate, String street, String city, Long postalCode) {
}
