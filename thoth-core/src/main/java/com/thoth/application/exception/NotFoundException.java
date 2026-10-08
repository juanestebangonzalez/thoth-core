package com.thoth.application.exception;

/** Recurso no encontrado (se responde con HTTP 404). */
public class NotFoundException extends BusinessException {
    public NotFoundException(String message) {
        super(message);
    }
}
