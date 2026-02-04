package it.eng.reservations_service.exception;

public class SlotServiceUnavailableException extends RuntimeException {
    public SlotServiceUnavailableException(String message) {
        super(message);
    }
}
