package com.efe.traderecon.management.jmx;

import com.efe.traderecon.messaging.inmemory.InMemoryQueue;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource(
        objectName = "com.efe:type=Messaging,name=inmemory",
        description = "EFE In-Memory Messaging JMX Management"
)
public class EfeMessagingMBean {

    private final InMemoryQueue inMemoryQueue;

    public EfeMessagingMBean(InMemoryQueue inMemoryQueue) {
        this.inMemoryQueue = inMemoryQueue;
    }

    @ManagedAttribute(description = "Current Queue Depth")
    public int getQueueDepth() {
        return inMemoryQueue.getTotalQueueSize();
    }

    @ManagedAttribute(description = "Queue Capacity")
    public int getCapacity() {
        return inMemoryQueue.getDefaultCapacity();
    }
}
