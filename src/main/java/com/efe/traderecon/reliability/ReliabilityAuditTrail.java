package com.efe.traderecon.reliability;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Component
public class ReliabilityAuditTrail implements Consumer<ReliabilityRecord> {
    private final List<ReliabilityRecord> records = new CopyOnWriteArrayList<>();

    @Override
    public void accept(ReliabilityRecord record) {
        records.add(record);
    }

    public List<ReliabilityRecord> snapshot() { return List.copyOf(records); }
    public void clear() { records.clear(); }
}
