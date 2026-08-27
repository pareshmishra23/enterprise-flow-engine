package com.efe.traderecon.api.controller;

import com.efe.traderecon.api.dto.ErrorResponse;
import com.efe.traderecon.api.exception.ResourceNotFoundException;
import com.efe.traderecon.api.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private String getCorrelationId(HttpServletRequest request) {
        String corrId = request.getHeader("X-Correlation-ID");
        if (corrId == null || corrId.isBlank()) {
            corrId = (String) request.getAttribute("X-Correlation-ID");
        }
        if (corrId == null || corrId.isBlank()) {
            corrId = "CORR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        return corrId;
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, HttpServletRequest request) {
        log.warn("Validation error on path [{}]: {} ({})", request.getRequestURI(), ex.getMessage(), ex.getErrorCode());
        ErrorResponse err = new ErrorResponse(
                ex.getErrorCode() != null ? ex.getErrorCode() : "EFE-VAL-001",
                ex.getMessage(),
                request.getRequestURI(),
                getCorrelationId(request)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found on path [{}]: {} ({})", request.getRequestURI(), ex.getMessage(), ex.getErrorCode());
        ErrorResponse err = new ErrorResponse(
                ex.getErrorCode() != null ? ex.getErrorCode() : "EFE-JOB-404",
                ex.getMessage(),
                request.getRequestURI(),
                getCorrelationId(request)
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument on path [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse err = new ErrorResponse(
                "EFE-VAL-001",
                ex.getMessage(),
                request.getRequestURI(),
                getCorrelationId(request)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Internal error on path [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        ErrorResponse err = new ErrorResponse(
                "EFE-SYS-500",
                "Internal server error: " + ex.getMessage(),
                request.getRequestURI(),
                getCorrelationId(request)
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}
