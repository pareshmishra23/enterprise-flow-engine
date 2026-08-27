package com.efe.traderecon.flow.core;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EfeCoreEvent {
    private String eventId;
    private String correlationId;
    private String type;
    private Double expectedQuantity;
    private Double actualQuantity;
    private String status; // "MATCH", "BREAK", "VALIDATION_FAILED"
    private Long processedAt;
    private String errorMessage;

    public EfeCoreEvent() {
    }

    public EfeCoreEvent(String eventId, String correlationId, String type, Double expectedQuantity, Double actualQuantity) {
        this.eventId = eventId;
        this.correlationId = correlationId;
        this.type = type;
        this.expectedQuantity = expectedQuantity;
        this.actualQuantity = actualQuantity;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getExpectedQuantity() { return expectedQuantity; }
    public void setExpectedQuantity(Double expectedQuantity) { this.expectedQuantity = expectedQuantity; }

    public Double getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(Double actualQuantity) { this.actualQuantity = actualQuantity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getProcessedAt() { return processedAt; }
    public void setProcessedAt(Long processedAt) { this.processedAt = processedAt; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    @Override
    public String toString() {
        return "EfeCoreEvent{" +
                "eventId='" + eventId + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", type='" + type + '\'' +
                ", expected=" + expectedQuantity +
                ", actual=" + actualQuantity +
                ", status='" + status + '\'' +
                '}';
    }
}
