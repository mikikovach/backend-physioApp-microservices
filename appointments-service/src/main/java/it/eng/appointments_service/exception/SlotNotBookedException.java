package it.eng.appointments_service.exception;

public class SlotNotBookedException extends RuntimeException {
    public SlotNotBookedException(String message) {
        super(message);
    }
}
