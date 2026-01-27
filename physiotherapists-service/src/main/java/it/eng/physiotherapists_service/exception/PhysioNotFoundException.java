package it.eng.physiotherapists_service.exception;

public class PhysioNotFoundException extends RuntimeException {

    public PhysioNotFoundException(String message) {
        super(message);
    }

}
