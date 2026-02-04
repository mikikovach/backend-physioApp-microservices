package it.eng.reservations_service.exception;

public class SlotClientException extends RuntimeException {
    public SlotClientException(String msg) {
        super(msg);
    }
}
