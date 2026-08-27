package com.efe.traderecon.ikasan.ui;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

/**
 * Bounded in-memory store of {@link WiretapEvent}s used to observe events as
 * they flow through Ikasan flows (wiretap/audit observability). A listener
 * returned by {@link #listener()} can be attached to any {@code IkasanFlow}
 * via {@code addWiretap(...)}. Keeps the most recent {@code capacity} events.
 */
@Component
public class FlowWiretapStore {

    private final int capacity;
    private final Deque<WiretapEvent> events = new ConcurrentLinkedDeque<>();

    public FlowWiretapStore() {
        this(200);
    }

    public FlowWiretapStore(int capacity) {
        this.capacity = Math.max(1, capacity);
    }

    /**
     * Returns a wiretap consumer that records each seen event into the store.
     */
    public Consumer<Object> listener() {
        return this::record;
    }

    public void record(Object payload) {
        if (payload == null) return;
        WiretapEvent event = new WiretapEvent(
                Instant.now(),
                payload.getClass().getSimpleName(),
                String.valueOf(payload));
        events.addLast(event);
        while (events.size() > capacity) {
            events.pollFirst();
        }
    }

    public List<WiretapEvent> snapshot() {
        return List.copyOf(events);
    }

    public int size() {
        return events.size();
    }

    public void clear() {
        events.clear();
    }
}
