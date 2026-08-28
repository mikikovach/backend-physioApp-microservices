package it.eng.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;


public record UserDTO(
		Long userId,
		@NotBlank(message = "firstName is required")
		String firstName,
		@NotBlank(message = "lastName is required")
		String lastName,
		@NotBlank(message = "email is required")
		@Email(message = "email must be valid")
		String email,
		@NotBlank(message = "password is required")
		@Size(min = 8, message = "password must contain at least 8 characters")
		String password,
		@NotNull(message = "birthDate is required")
		LocalDate birthDate,
		@NotBlank(message = "street is required")
		String street,
		@NotBlank(message = "city is required")
		String city,
		@NotNull(message = "postalCode is required")
		Long postalCode) {
}
