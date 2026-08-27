package com.efe.traderecon.ikasan.ui;

import java.time.Instant;

/**
 * A single wiretapped event observed passing through an Ikasan flow. Captured
 * for audit/observability via the flow's wiretap mechanism.
 */
public record WiretapEvent(
        Instant occurredAt,
        String payloadType,
        String payload) {
}
