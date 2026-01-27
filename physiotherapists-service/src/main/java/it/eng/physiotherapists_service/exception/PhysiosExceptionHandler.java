package it.eng.physiotherapists_service.exception;


import it.eng.physiotherapists_service.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class PhysiosExceptionHandler {

    @ExceptionHandler(PhysioNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleSlotNotFoundException() {
        return new ErrorResponse("PHYSIO_NOT_FOUND");
    }


}

