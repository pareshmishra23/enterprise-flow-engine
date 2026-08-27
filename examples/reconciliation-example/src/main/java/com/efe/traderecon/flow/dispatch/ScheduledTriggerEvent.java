package com.efe.traderecon.flow.dispatch;

import java.time.Instant;

public record ScheduledTriggerEvent(String scheduleName, Instant timestamp) {
}
