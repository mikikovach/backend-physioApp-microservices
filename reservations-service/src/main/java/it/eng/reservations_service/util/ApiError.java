package it.eng.reservations_service.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
public class ApiError {

    private String error;
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, String> fieldErrors;

    /**
     * Creates an API error response.
     *
     * @param status HTTP status
     * @param message error message
     * @param path request path
     */
    public ApiError(HttpStatus status, String message, String path) {
        this.error = status.getReasonPhrase();
        this.message = message;
        this.status = status.value();
        this.timestamp = LocalDateTime.now();
        this.path = path;
        this.fieldErrors = Map.of();
    }

    /**
     * Creates an API error response with field-level errors.
     *
     * @param status HTTP status
     * @param message error message
     * @param path request path
     * @param fieldErrors map of field names to error messages
     */
    public ApiError(HttpStatus status, String message, String path, Map<String, String> fieldErrors) {
        this.error = status.getReasonPhrase();
        this.message = message;
        this.status = status.value();
        this.timestamp = LocalDateTime.now();
        this.path = path;
        this.fieldErrors = fieldErrors != null ? fieldErrors : Map.of();
    }
}
