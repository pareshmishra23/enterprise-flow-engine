package com.efe.traderecon.ikasan.model;

public class FlowElement {
    private final String name;
    private final ComponentType type;
    private final Object component;
    private long invocationCount = 0;
    private long errorCount = 0;
    private long lastExecutionTimeMs = 0;

    public FlowElement(String name, ComponentType type, Object component) {
        this.name = name;
        this.type = type;
        this.component = component;
    }

    public String getName() {
        return name;
    }

    public ComponentType getType() {
        return type;
    }

    public Object getComponent() {
        return component;
    }

    public synchronized void recordExecution(long durationMs, boolean success) {
        invocationCount++;
        lastExecutionTimeMs = durationMs;
        if (!success) {
            errorCount++;
        }
    }

    public synchronized long getInvocationCount() {
        return invocationCount;
    }

    public synchronized long getErrorCount() {
        return errorCount;
    }

    public synchronized long getLastExecutionTimeMs() {
        return lastExecutionTimeMs;
    }

    @Override
    public String toString() {
        return "FlowElement{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", invocationCount=" + invocationCount +
                '}';
    }
}
