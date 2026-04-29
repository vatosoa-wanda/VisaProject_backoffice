package com.visa.backoffice.exception;

public class DemandeVerrouilleException extends BusinessException {

    public DemandeVerrouilleException(String message) {
        super(message);
    }

    public DemandeVerrouilleException(String message, Throwable cause) {
        super(message, cause);
    }
}
