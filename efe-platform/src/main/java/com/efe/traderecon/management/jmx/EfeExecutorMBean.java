package com.efe.traderecon.management.jmx;

import com.efe.traderecon.execution.EfeExecutorService;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource(
        objectName = "com.efe:type=Executor,name=worker-pool",
        description = "EFE Bounded Worker Pool JMX Management"
)
public class EfeExecutorMBean {

    private final EfeExecutorService executorService;

    public EfeExecutorMBean(EfeExecutorService executorService) {
        this.executorService = executorService;
    }

    @ManagedAttribute(description = "Active Worker Threads")
    public int getActiveThreads() {
        return executorService.getActiveThreads();
    }

    @ManagedAttribute(description = "Pool Size")
    public int getPoolSize() {
        return executorService.getPoolSize();
    }

    @ManagedAttribute(description = "Queue Depth")
    public int getQueueSize() {
        return executorService.getQueueSize();
    }

    @ManagedAttribute(description = "Completed Tasks")
    public long getCompletedTasks() {
        return executorService.getCompletedTasks();
    }

    @ManagedAttribute(description = "Rejected Tasks Count")
    public long getRejectedTasks() {
        return executorService.getRejectedTasks();
    }

    @ManagedAttribute(description = "Total Task Count Submitted")
    public long getTotalTasks() {
        return executorService.getTaskCount();
    }
}
