package it.eng.reservations_service.exception;

import it.eng.reservations_service.dto.ErrorResponse;
import it.eng.reservations_service.util.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;


@RestControllerAdvice
public class ReservationsExceptionHandler {

//    @ExceptionHandler(ResponseStatusException.class)
//    public ResponseEntity<String> handle(ResponseStatusException ex) {
//        return ResponseEntity
//                .status(ex.getStatusCode())
//                .body(ex.getReason());
//    }


//    @ExceptionHandler(ReservationNotFoundException.class)
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public ErrorResponse handleReservationNotFoundException() {
//        return new ErrorResponse("RESERVATION_NOT_FOUND");
//    }


    @ExceptionHandler(ReservationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiError> handleReservationNotFoundException(ReservationNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI()));
    }


    @ExceptionHandler(SlotAlreadyReservedInReservationContextException.class)
    public ResponseEntity<ApiError> handleWebClientException(
            SlotAlreadyReservedInReservationContextException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(HttpStatus.CONFLICT, ex.getMessage(), "/slots/reserve"));
    }

    @ExceptionHandler(SlotServiceUnavailableException.class)
    public ResponseEntity<ApiError> handleSlotServiceUnavailableException(
            SlotServiceUnavailableException ex, HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(SlotClientException.class)
    public ResponseEntity<ApiError> handleSlotClientException(
            SlotClientException ex, HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI()));
    }


    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<String> handleWebClientException(
            WebClientResponseException ex) {

        return ResponseEntity
                .status(ex.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ex.getResponseBodyAsString());
    }
}