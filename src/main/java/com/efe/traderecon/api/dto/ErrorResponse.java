package com.efe.traderecon.api.dto;

import java.time.Instant;

public class ErrorResponse {
    private String errorCode;
    private String message;
    private String path;
    private Instant timestamp;
    private String correlationId;

    public ErrorResponse() {
        this.timestamp = Instant.now();
    }

    public ErrorResponse(String errorCode, String message, String path, String correlationId) {
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.correlationId = correlationId;
        this.timestamp = Instant.now();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
