package it.eng.appointments_service.exception;

import it.eng.appointments_service.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;



@RestControllerAdvice
public class SlotsExceptionHandler {

    @ExceptionHandler(SlotAlreadyBookedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleSlotAlreadyBookedException() {
        return new ErrorResponse("SLOT_BOOKED");
    }

    @ExceptionHandler(SlotNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleSlotNotFoundException() {
        return new ErrorResponse("SLOT_NOT_FOUND");
    }

    @ExceptionHandler(SlotInPastException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleSlotInPastException() {
        return new ErrorResponse("SLOT_IN_PAST");
    }



}

