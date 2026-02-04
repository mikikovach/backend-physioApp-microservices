package it.eng.reservations_service.exception;

public class SlotAlreadyReservedInReservationContextException extends RuntimeException {

    public SlotAlreadyReservedInReservationContextException(String message) {
        super(message);
    }

}
