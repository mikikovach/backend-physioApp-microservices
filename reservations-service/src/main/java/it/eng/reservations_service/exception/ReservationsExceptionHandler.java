package it.eng.reservations_service.exception;

import it.eng.reservations_service.dto.ErrorResponse;
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


    @ExceptionHandler(ReservationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleReservationNotFoundException() {
        return new ErrorResponse("RESERVATION_NOT_FOUND");
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