package com.efe.traderecon.messaging.spi;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessagingMessage<T> implements Serializable {
    private final String messageId;
    private final String correlationId;
    private final T payload;
    private final Map<String, Object> headers;
    private final Instant timestamp;

    public MessagingMessage(T payload) {
        this(UUID.randomUUID().toString(), UUID.randomUUID().toString(), payload, new HashMap<>());
    }

    public MessagingMessage(String messageId, String correlationId, T payload, Map<String, Object> headers) {
        this.messageId = messageId != null ? messageId : UUID.randomUUID().toString();
        this.correlationId = correlationId != null ? correlationId : UUID.randomUUID().toString();
        this.payload = payload;
        this.headers = headers != null ? new HashMap<>(headers) : new HashMap<>();
        this.timestamp = Instant.now();
    }

    public String getMessageId() {
        return messageId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public T getPayload() {
        return payload;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public MessagingMessage<T> setHeader(String key, Object value) {
        this.headers.put(key, value);
        return this;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "MessagingMessage{" +
                "messageId='" + messageId + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", payload=" + payload +
                ", timestamp=" + timestamp +
                '}';
    }
}
