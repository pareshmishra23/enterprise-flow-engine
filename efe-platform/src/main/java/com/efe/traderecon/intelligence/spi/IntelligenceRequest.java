package com.efe.traderecon.intelligence.spi;

import java.util.Map;

/**
 * EFE Intelligence Request — generic envelope for an AI analysis request.
 * Business processors submit this to the Intelligence SPI without knowing
 * which provider (Ollama, OpenAI, local model, etc.) will handle it.
 */
public class IntelligenceRequest {

    private String requestId;
    private IntelligenceType intelligenceType;
    private String eventType;
    private String correlationId;
    private Map<String, Object> payload;

    public IntelligenceRequest() {}

    public IntelligenceRequest(String requestId,
                               IntelligenceType intelligenceType,
                               String eventType,
                               String correlationId,
                               Map<String, Object> payload) {
        this.requestId = requestId;
        this.intelligenceType = intelligenceType;
        this.eventType = eventType;
        this.correlationId = correlationId;
        this.payload = payload;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public IntelligenceType getIntelligenceType() { return intelligenceType; }
    public void setIntelligenceType(IntelligenceType intelligenceType) { this.intelligenceType = intelligenceType; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    @Override
    public String toString() {
        return "IntelligenceRequest{requestId='" + requestId + "', type=" + intelligenceType + "}";
    }
}
