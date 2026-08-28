package it.eng.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginUserDto(
		@NotBlank(message = "email is required")
		@Email(message = "email must be valid")
		String email,
		@NotBlank(message = "password is required")
		String password) {
}
