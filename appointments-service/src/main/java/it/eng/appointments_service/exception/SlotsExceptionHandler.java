package it.eng.appointments_service.exception;

import it.eng.appointments_service.dto.ErrorResponse;
import it.eng.appointments_service.util.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;



@RestControllerAdvice
public class SlotsExceptionHandler {

    @ExceptionHandler(SlotAlreadyBookedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiError> handleSlotAlreadyBookedException(SlotAlreadyBookedException ex, HttpServletRequest request) {

        ApiError apiError = new ApiError(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);

    }

    @ExceptionHandler(SlotNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiError> handleSlotNotFoundException(SlotNotFoundException ex, HttpServletRequest request) {

        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(SlotInPastException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleSlotInPastException(SlotInPastException ex, HttpServletRequest request) {

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(SlotNotBookedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiError> handleSSlotNotBookedException(SlotNotBookedException ex, HttpServletRequest request) {

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }



}

