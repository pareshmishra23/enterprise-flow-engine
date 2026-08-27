package com.efe.traderecon.reliability;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class DeadLetterQueue {
    private final BlockingQueue<ReliabilityRecord> records;
    private final ReliabilityProperties properties;

    public DeadLetterQueue(ReliabilityProperties properties) {
        this.properties = properties;
        this.records = new LinkedBlockingQueue<>(properties.getDlqCapacity());
    }

    public void enqueue(ReliabilityRecord record) {
        if (!records.offer(record)) {
            throw new IllegalStateException("EFE DLQ is full");
        }
    }

    public List<ReliabilityRecord> snapshot() {
        return List.copyOf(new ArrayList<>(records));
    }

    public ReliabilityRecord poll() {
        return records.poll();
    }

    public int size() { return records.size(); }
    public int capacity() { return properties.getDlqCapacity(); }
    public void clear() { records.clear(); }
}
