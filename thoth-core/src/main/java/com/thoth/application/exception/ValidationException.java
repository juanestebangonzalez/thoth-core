package com.thoth.application.exception;

public class ValidationException extends BusinessException {
    public ValidationException(String message) {
        super("Error de validacion: " + message);
    }
}
