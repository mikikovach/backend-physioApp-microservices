package it.eng.physiotherapists_service.exception;


import it.eng.physiotherapists_service.util.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class PhysiosExceptionHandler {

    @ExceptionHandler(PhysioNotFoundException.class)
    public ResponseEntity<ApiError> handlePhysioNotFoundException(PhysioNotFoundException ex, HttpServletRequest request) {

        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }


}

