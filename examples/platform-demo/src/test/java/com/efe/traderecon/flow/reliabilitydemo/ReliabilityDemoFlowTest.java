package com.efe.traderecon.flow.reliabilitydemo;

import com.efe.traderecon.reliability.DeadLetterQueue;
import com.efe.traderecon.reliability.ReliabilityAuditTrail;
import com.efe.traderecon.reliability.ReliabilityProperties;
import com.efe.traderecon.reliability.ReliabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Validates flow-level reliability semantics: transient failures are retried
 * with backoff and succeed, while permanent failures are routed to the DLQ
 * without retry.
 */
class ReliabilityDemoFlowTest {

    private ReliabilityProperties properties;
    private DeadLetterQueue dlq;
    private ReliabilityAuditTrail audit;
    private ReliabilityService service;
    private ReliabilityDemoFlowConfiguration.ReliabilityDemoProcessor processor;
    private ReliabilityDemoFlowConfiguration.ReliabilityDemoProducer producer;
    private ReliabilityDemoFlowConfiguration.ReliabilityDemoConsumer consumer;

    @BeforeEach
    void setUp() {
        properties = new ReliabilityProperties();
        properties.setMaxRetries(3);
        properties.setInitialDelayMs(0);
        properties.setMaxDelayMs(0);
        dlq = new DeadLetterQueue(properties);
        audit = new ReliabilityAuditTrail();
        service = new ReliabilityService(properties, dlq, audit);
        processor = new ReliabilityDemoFlowConfiguration.ReliabilityDemoProcessor();
        producer = new ReliabilityDemoFlowConfiguration.ReliabilityDemoProducer();
        consumer = new ReliabilityDemoFlowConfiguration.ReliabilityDemoConsumer();
    }

    @Test
    void transientFailureIsRetriedAndThenSucceeds() throws Exception {
        ReliabilityMessage msg = new ReliabilityMessage("MSG-1", "hello");

        // Simulate the flow: run the processor through reliability until success,
        // mirroring onConsumerEvent's wrapping behaviour.
        Object result = service.execute("MSG-1", "reliability-demo-flow", () -> processor.process(msg));

        assertThat(result).isSameAs(msg);
        assertThat(msg.isProcessed()).isTrue();
        assertThat(msg.getAttemptsTaken()).isEqualTo(2);
        assertThat(dlq.size()).isZero();
        assertThat(audit.snapshot()).extracting(record -> record.status())
                .containsExactly("RETRY", "SUCCESS");
    }

    @Test
    void permanentFailureGoesStraightToDlq() throws Exception {
        ReliabilityMessage msg = new ReliabilityMessage("MSG-2", "bad");
        msg.setFailingPermanent(true);

        assertThatThrownBy(() -> service.execute("MSG-2", "reliability-demo-flow", () -> processor.process(msg)))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(msg.isProcessed()).isFalse();
        assertThat(dlq.size()).isEqualTo(1);
        assertThat(dlq.snapshot().getFirst().status()).isEqualTo("DLQ");
        assertThat(dlq.snapshot().getFirst().attempts()).isEqualTo(1);
        assertThat(audit.snapshot()).extracting(record -> record.status())
                .containsExactly("DLQ");
    }

    @Test
    void exhaustedTransientFailureGoesToDlq() throws Exception {
        // Processor that always fails transiently -> all retries exhausted -> DLQ.
        ReliabilityMessage msg = new ReliabilityMessage("MSG-3", "flaky");
        processor.reset();
        // For this test, use a processor that always fails: subclass by configuring
        // required attempts unreachable via a custom supplier.
        assertThatThrownBy(() -> service.execute("MSG-3", "reliability-demo-flow", () -> {
            throw new RuntimeException("always down");
        })).isInstanceOf(RuntimeException.class);

        assertThat(dlq.snapshot().getFirst().attempts()).isEqualTo(4);
        assertThat(audit.snapshot()).extracting(record -> record.status())
                .containsExactly("RETRY", "RETRY", "RETRY", "DLQ");
    }
}
