package it.eng.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateUserDTO(
		@NotBlank(message = "firstName is required")
		String firstName,
		@NotBlank(message = "lastName is required")
		String lastName,
		@NotBlank(message = "email is required")
		@Email(message = "email must be valid")
		String email,
		@NotNull(message = "birthDate is required")
		LocalDate birthDate,
		@NotBlank(message = "street is required")
		String street,
		@NotBlank(message = "city is required")
		String city,
		@NotNull(message = "postalCode is required")
		Long postalCode) {
}
