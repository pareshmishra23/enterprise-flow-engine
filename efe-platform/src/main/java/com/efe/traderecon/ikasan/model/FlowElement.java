package com.efe.traderecon.ikasan.model;

import java.util.HashMap;
import java.util.Map;

public class FlowElement {
    private final String name;
    private final ComponentType type;
    private final Object component;
    private final Map<String, IkasanProducer<?>> routes = new HashMap<>();
    private long invocationCount = 0;
    private long errorCount = 0;
    private long totalExecutionTimeMs = 0;

    private long lastExecutionTimeMs = 0;

    public FlowElement(String name, ComponentType type, Object component) {
        this.name = name;
        this.type = type;
        this.component = component;
    }

    public void addRoute(String routeName, IkasanProducer<?> producer) {
        this.routes.put(routeName, producer);
    }

    public IkasanProducer<?> getRoute(String routeName) {
        return this.routes.get(routeName);
    }

    public Map<String, IkasanProducer<?>> getRoutes() {
        return routes;
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
        this.invocationCount++;
        this.lastExecutionTimeMs = durationMs;
        this.totalExecutionTimeMs += durationMs;
        if (!success) {
            this.errorCount++;
        }
    }

    public synchronized long getLastExecutionTimeMs() {
        return lastExecutionTimeMs;
    }

    public synchronized long getInvocationCount() {
        return invocationCount;
    }

    public synchronized long getErrorCount() {
        return errorCount;
    }

    public synchronized long getTotalExecutionTimeMs() {
        return totalExecutionTimeMs;
    }

    public synchronized double getAverageExecutionTimeMs() {
        return invocationCount == 0 ? 0.0 : (double) totalExecutionTimeMs / invocationCount;
    }
}
