package com.efe.traderecon.reliability;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "efe.reliability")
public class ReliabilityProperties {
    private int maxRetries = 3;
    private long initialDelayMs = 100;
    private double backoffMultiplier = 2.0;
    private long maxDelayMs = 10_000;
    private int dlqCapacity = 1_000;

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = Math.max(0, maxRetries); }
    public long getInitialDelayMs() { return initialDelayMs; }
    public void setInitialDelayMs(long initialDelayMs) { this.initialDelayMs = Math.max(0, initialDelayMs); }
    public double getBackoffMultiplier() { return backoffMultiplier; }
    public void setBackoffMultiplier(double backoffMultiplier) { this.backoffMultiplier = Math.max(1.0, backoffMultiplier); }
    public long getMaxDelayMs() { return maxDelayMs; }
    public void setMaxDelayMs(long maxDelayMs) { this.maxDelayMs = Math.max(0, maxDelayMs); }
    public int getDlqCapacity() { return dlqCapacity; }
    public void setDlqCapacity(int dlqCapacity) { this.dlqCapacity = Math.max(1, dlqCapacity); }
}
