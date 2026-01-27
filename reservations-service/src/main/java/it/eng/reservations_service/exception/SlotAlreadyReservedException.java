package it.eng.reservations_service.exception;

public class SlotAlreadyReservedException extends RuntimeException {

    public SlotAlreadyReservedException(String message) {
        super(message);
    }

}
