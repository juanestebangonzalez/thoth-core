package com.thoth.application.exception;

public class InvalidStatusTransitionException extends BusinessException {
    public InvalidStatusTransitionException(String message) {
        super("Invalid status transition: " + message);
    }
}
