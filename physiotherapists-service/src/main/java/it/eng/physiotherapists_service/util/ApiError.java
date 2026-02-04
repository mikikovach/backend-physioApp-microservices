package it.eng.physiotherapists_service.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ApiError {

    private String error;
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private String path;

    public ApiError(HttpStatus status, String message, String path) {
        this.error = status.getReasonPhrase();
        this.message = message;
        this.status = status.value();
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }
}
