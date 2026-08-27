package com.efe.traderecon.reliability;

import java.time.Instant;

public record ReliabilityRecord(
        String eventId,
        String flowName,
        String status,
        int attempts,
        String errorType,
        String errorMessage,
        Instant occurredAt) {
}
