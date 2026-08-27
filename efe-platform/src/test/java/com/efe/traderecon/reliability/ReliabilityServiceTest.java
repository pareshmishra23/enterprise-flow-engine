package com.efe.traderecon.reliability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReliabilityServiceTest {
    private ReliabilityProperties properties;
    private DeadLetterQueue dlq;
    private ReliabilityAuditTrail audit;
    private ReliabilityService service;

    @BeforeEach
    void setUp() {
        properties = new ReliabilityProperties();
        properties.setMaxRetries(3);
        properties.setInitialDelayMs(0);
        properties.setMaxDelayMs(0);
        dlq = new DeadLetterQueue(properties);
        audit = new ReliabilityAuditTrail();
        service = new ReliabilityService(properties, dlq, audit);
    }

    @Test
    void retriesTransientFailureAndSucceeds() throws Exception {
        AtomicInteger attempts = new AtomicInteger();
        String result = service.execute("evt-1", "flow-1", () -> {
            if (attempts.incrementAndGet() == 1) throw new RuntimeException("temporary");
            return "ok";
        });
        assertThat(result).isEqualTo("ok");
        assertThat(attempts).hasValue(2);
        assertThat(dlq.size()).isZero();
        assertThat(audit.snapshot()).extracting(ReliabilityRecord::status)
                .containsExactly("RETRY", "SUCCESS");
    }

    @Test
    void sendsPermanentFailureToDlqWithoutRetry() {
        assertThatThrownBy(() -> service.execute("evt-2", "flow-1", () -> {
            throw new IllegalArgumentException("bad payload");
        })).isInstanceOf(IllegalArgumentException.class);
        assertThat(dlq.size()).isEqualTo(1);
        assertThat(dlq.snapshot().getFirst().attempts()).isEqualTo(1);
        assertThat(dlq.snapshot().getFirst().status()).isEqualTo("DLQ");
        assertThat(audit.snapshot()).extracting(ReliabilityRecord::status)
                .containsExactly("DLQ");
    }

    @Test
    void sendsExhaustedTransientFailureToDlq() {
        assertThatThrownBy(() -> service.execute("evt-3", "flow-1", () -> {
            throw new RuntimeException("downstream unavailable");
        })).isInstanceOf(RuntimeException.class);
        assertThat(dlq.snapshot().getFirst().attempts()).isEqualTo(4);
        assertThat(audit.snapshot()).extracting(ReliabilityRecord::status)
                .containsExactly("RETRY", "RETRY", "RETRY", "DLQ");
    }
}
