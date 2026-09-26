package com.thoth.adapter.in.rest.exception;

import com.thoth.application.exception.BusinessException;
import com.thoth.application.exception.EquipmentNotFoundException;
import com.thoth.application.exception.InvalidStatusTransitionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EquipmentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEquipmentNotFound(
            EquipmentNotFoundException ex, WebRequest request) {
        ApiErrorResponse error = ApiErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .error("NOT_FOUND")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidStatusTransition(
            InvalidStatusTransitionException ex, WebRequest request) {
        ApiErrorResponse error = ApiErrorResponse.builder()
            .status(HttpStatus.CONFLICT.value())
            .error("CONFLICT")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthorizationDenied(
            AuthorizationDeniedException ex, WebRequest request) {
        ApiErrorResponse error = ApiErrorResponse.builder()
            .status(HttpStatus.FORBIDDEN.value())
            .error("FORBIDDEN")
            .message("No tiene permisos para realizar esta accion")
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {
        ApiErrorResponse error = ApiErrorResponse.builder()
            .status(HttpStatus.FORBIDDEN.value())
            .error("FORBIDDEN")
            .message(ex.getMessage() != null ? ex.getMessage() : "No tiene permisos para realizar esta accion")
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(
            BusinessException ex, WebRequest request) {
        ApiErrorResponse error = ApiErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("BUSINESS_ERROR")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        ApiErrorResponse error = ApiErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("VALIDATION_ERROR")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        ApiErrorResponse error = ApiErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .error("NOT_FOUND")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .reduce((a, b) -> a + ", " + b)
            .orElse("Error de validacion");
        ApiErrorResponse error = ApiErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("VALIDATION_ERROR")
            .message(message)
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(
            Exception ex, WebRequest request) {
        log.error("Unhandled exception at {}: {}", request.getDescription(false), ex.getMessage(), ex);
        ApiErrorResponse error = ApiErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("INTERNAL_SERVER_ERROR")
            .message("An unexpected error occurred")
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}