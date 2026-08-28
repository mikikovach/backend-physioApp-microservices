package it.eng.auth_service.dto;

import java.util.Map;

public record ErrorResponse(String errorMessage, Map<String, String> fieldErrors) {

	public ErrorResponse(String errorMessage) {
		this(errorMessage, Map.of());
	}
}
