package it.eng.appointments_service.exception;

public class SlotInPastException extends RuntimeException {
    public SlotInPastException(String message) {
        super(message);
    }
}
