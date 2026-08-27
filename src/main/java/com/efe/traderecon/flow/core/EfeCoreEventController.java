package com.efe.traderecon.flow.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/core")
public class EfeCoreEventController {
    private static final Logger log = LoggerFactory.getLogger(EfeCoreEventController.class);

    private final EfeCoreFlowConfiguration.EfeCoreEventConsumer consumer;
    private final ObjectMapper objectMapper;

    public EfeCoreEventController(EfeCoreFlowConfiguration.EfeCoreEventConsumer consumer, ObjectMapper objectMapper) {
        this.consumer = consumer;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/events")
    public ResponseEntity<Map<String, Object>> submitCoreEvent(
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            @RequestBody Map<String, Object> eventPayload) {

        String corrId = (correlationId != null && !correlationId.isBlank()) ? correlationId : UUID.randomUUID().toString();
        log.info("Received POST /api/v1/core/events with correlationId [{}]", corrId);

        try {
            eventPayload.putIfAbsent("correlationId", corrId);
            String json = objectMapper.writeValueAsString(eventPayload);
            consumer.publish(json);

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                    "status", "ACCEPTED",
                    "eventId", String.valueOf(eventPayload.get("eventId")),
                    "correlationId", corrId,
                    "message", "Event successfully submitted into efe-core-flow"
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Validation error on /api/v1/core/events: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "ERROR",
                    "correlationId", corrId,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Failed processing /api/v1/core/events: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "FAILED",
                    "correlationId", corrId,
                    "message", e.getMessage()
            ));
        }
    }
}
