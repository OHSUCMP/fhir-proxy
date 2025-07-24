package edu.ohsu.cmp.fhirproxy.exception;

public class ClientInfoNotFoundException extends Exception {
    public ClientInfoNotFoundException() {
        super();
    }

    public ClientInfoNotFoundException(String message) {
        super(message);
    }

    public ClientInfoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientInfoNotFoundException(Throwable cause) {
        super(cause);
    }
}
