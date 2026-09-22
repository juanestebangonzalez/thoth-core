package com.thoth.application.exception;

public class ValidationException extends BusinessException {
    public ValidationException(String message) {
        super("Validation failed: " + message);
    }
}
