package com.efe.traderecon.management.jmx;

import com.efe.traderecon.flow.dispatch.ScheduledTaskConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@ManagedResource(
        objectName = "com.efe:type=Scheduler,name=reconciliation-dispatch",
        description = "EFE Dispatch Scheduler JMX Management"
)
public class EfeSchedulerMBean {

    private final ScheduledTaskConsumer scheduledTaskConsumer;
    private final long intervalMs;
    private volatile Instant lastExecution = Instant.now();

    public EfeSchedulerMBean(
            ScheduledTaskConsumer scheduledTaskConsumer,
            @Value("${scheduler.reconciliation-dispatch.interval-ms:5000}") long intervalMs) {
        this.scheduledTaskConsumer = scheduledTaskConsumer;
        this.intervalMs = intervalMs;
    }

    @ManagedAttribute(description = "Scheduler Running Status")
    public boolean isRunning() {
        return scheduledTaskConsumer.isRunning();
    }

    @ManagedAttribute(description = "Interval in Milliseconds")
    public long getIntervalMs() {
        return intervalMs;
    }

    @ManagedAttribute(description = "Last Execution Timestamp")
    public String getLastExecution() {
        return lastExecution != null ? lastExecution.toString() : "NEVER";
    }

    @ManagedAttribute(description = "Next Estimated Execution Timestamp")
    public String getNextExecution() {
        return lastExecution != null ? lastExecution.plusMillis(intervalMs).toString() : "UNKNOWN";
    }

    @ManagedOperation(description = "Trigger scheduled job immediately")
    public void triggerNow() {
        lastExecution = Instant.now();
        scheduledTaskConsumer.triggerManually();
    }

    @ManagedOperation(description = "Start scheduler")
    public void start() {
        scheduledTaskConsumer.start();
    }

    @ManagedOperation(description = "Stop scheduler")
    public void stop() {
        scheduledTaskConsumer.stop();
    }
}
