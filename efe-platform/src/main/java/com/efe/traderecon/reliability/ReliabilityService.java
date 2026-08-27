package com.efe.traderecon.reliability;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@Service
public class ReliabilityService {
    private final ReliabilityProperties properties;
    private final DeadLetterQueue deadLetterQueue;
    private final FailureClassifier classifier;
    private final Consumer<ReliabilityRecord> wiretap;

    @Autowired
    public ReliabilityService(ReliabilityProperties properties, DeadLetterQueue deadLetterQueue,
                              ReliabilityAuditTrail auditTrail) {
        this(properties, deadLetterQueue, FailureClassifier.defaultClassifier(), auditTrail);
    }

    public ReliabilityService(ReliabilityProperties properties, DeadLetterQueue deadLetterQueue,
                              FailureClassifier classifier, Consumer<ReliabilityRecord> wiretap) {
        this.properties = Objects.requireNonNull(properties);
        this.deadLetterQueue = Objects.requireNonNull(deadLetterQueue);
        this.classifier = Objects.requireNonNull(classifier);
        this.wiretap = Objects.requireNonNull(wiretap);
    }

    public <T> T execute(String eventId, String flowName, Callable<T> operation) throws Exception {
        Throwable last = null;
        for (int attempt = 1; attempt <= properties.getMaxRetries() + 1; attempt++) {
            try {
                T result = operation.call();
                record(eventId, flowName, "SUCCESS", attempt, null);
                return result;
            } catch (Throwable failure) {
                last = failure;
                boolean retryable = classifier.isRetryable(failure);
                if (!retryable || attempt > properties.getMaxRetries()) {
                    ReliabilityRecord record = record(eventId, flowName, "DLQ", attempt, failure);
                    deadLetterQueue.enqueue(record);
                    if (failure instanceof Exception exception) throw exception;
                    throw new RuntimeException(failure);
                }
                record(eventId, flowName, "RETRY", attempt, failure);
                long delay = Math.min(properties.getMaxDelayMs(),
                        (long) (properties.getInitialDelayMs() * Math.pow(properties.getBackoffMultiplier(), attempt - 1)));
                if (delay > 0) Thread.sleep(delay);
            }
        }
        throw new IllegalStateException("Reliability execution ended without outcome", (Exception) last);
    }

    private ReliabilityRecord record(String eventId, String flowName, String status, int attempts, Throwable failure) {
        ReliabilityRecord record = new ReliabilityRecord(eventId, flowName, status, attempts,
                failure == null ? null : failure.getClass().getName(),
                failure == null ? null : failure.getMessage(), Instant.now());
        wiretap.accept(record);
        return record;
    }
}
