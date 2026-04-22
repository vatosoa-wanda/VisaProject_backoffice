package com.visa.backoffice.exception;

/**
 * Exception levée pour une violation de règle métier
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
