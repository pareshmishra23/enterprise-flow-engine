package com.efe.traderecon.flow.reliabilitydemo;

import com.efe.traderecon.reliability.DeadLetterQueue;
import com.efe.traderecon.reliability.ReliabilityAuditTrail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST surface for the reliability demo flow. Demonstrates retry/backoff/DLQ
 * behaviour from the external boundary: submit a message, then inspect the
 * produced store, reliability audit trail, and dead letter queue.
 */
@RestController
@RequestMapping("/api/v1/reliability")
public class ReliabilityDemoController {
    private static final Logger log = LoggerFactory.getLogger(ReliabilityDemoController.class);

    private final ReliabilityDemoFlowConfiguration.ReliabilityDemoConsumer consumer;
    private final ReliabilityDemoFlowConfiguration.ReliabilityDemoProducer producer;
    private final DeadLetterQueue deadLetterQueue;
    private final ReliabilityAuditTrail auditTrail;

    public ReliabilityDemoController(
            ReliabilityDemoFlowConfiguration.ReliabilityDemoConsumer consumer,
            ReliabilityDemoFlowConfiguration.ReliabilityDemoProducer producer,
            DeadLetterQueue deadLetterQueue,
            ReliabilityAuditTrail auditTrail) {
        this.consumer = consumer;
        this.producer = producer;
        this.deadLetterQueue = deadLetterQueue;
        this.auditTrail = auditTrail;
    }

    @PostMapping("/messages")
    public ResponseEntity<Map<String, Object>> submitMessage(@RequestBody(required = false) Map<String, Object> payload) {
        String messageId = (payload != null && payload.get("messageId") != null)
                ? String.valueOf(payload.get("messageId")) : UUID.randomUUID().toString();
        boolean failingPermanent = payload != null && Boolean.TRUE.equals(payload.get("failingPermanent"));
        String content = (payload != null && payload.get("content") != null)
                ? String.valueOf(payload.get("content")) : "demo-message";

        ReliabilityMessage message = new ReliabilityMessage(messageId, content);
        message.setFailingPermanent(failingPermanent);

        log.info("Submitting reliability demo message [{}] (permanentFailure={})", messageId, failingPermanent);
        consumer.publish(message);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "status", "ACCEPTED",
                "messageId", messageId,
                "failingPermanent", failingPermanent,
                "note", "Message routed through reliability-demo-flow (retry/backoff/DLQ)"
        ));
    }

    @GetMapping("/messages")
    public ResponseEntity<Map<String, Object>> listProcessed() {
        List<ReliabilityMessage> processed = producer.getStore();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("flow", "reliability-demo-flow");
        body.put("processedCount", processed.size());
        body.put("processed", processed);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/dlq")
    public ResponseEntity<Map<String, Object>> listDeadLetter() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("dlqSize", deadLetterQueue.size());
        body.put("dlqCapacity", deadLetterQueue.capacity());
        body.put("records", deadLetterQueue.snapshot());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/audit")
    public ResponseEntity<Map<String, Object>> listAudit() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("auditCount", auditTrail.snapshot().size());
        body.put("records", auditTrail.snapshot());
        return ResponseEntity.ok(body);
    }
}
