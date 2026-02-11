package it.eng.reservations_service.exception;

public class PhysioServiceUnavailableException extends RuntimeException {

    public PhysioServiceUnavailableException(String message) {
        super(message);
    }
}
