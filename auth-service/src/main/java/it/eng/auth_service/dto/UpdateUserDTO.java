package it.eng.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

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
		@Past(message = "birthDate must be in the past")
		LocalDate birthDate,
		@NotBlank(message = "street is required")
		String street,
		@NotBlank(message = "city is required")
		String city,
		@NotNull(message = "postalCode is required")
		@Positive(message = "postalCode must be positive")
		Long postalCode) {
}
