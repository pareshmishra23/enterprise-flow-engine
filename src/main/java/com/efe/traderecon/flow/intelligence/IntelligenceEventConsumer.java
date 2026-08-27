package com.efe.traderecon.flow.intelligence;

import com.efe.traderecon.ikasan.model.IkasanConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * EFE Intelligence Event Consumer — Ikasan Consumer for the intelligence-audit-flow.
 *
 * Polls an in-memory event queue for trade events to analyse.
 * External code submits events via {@link #submit(Map)}.
 *
 * EFE-011 (Kafka) and EFE-013 (JMS) will provide real event-driven consumers
 * that replace this in-memory implementation.
 */
@Component
public class IntelligenceEventConsumer implements IkasanConsumer<Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(IntelligenceEventConsumer.class);

    private final BlockingQueue<Map<String, Object>> eventQueue = new LinkedBlockingQueue<>(1000);
    private volatile boolean running = false;
    private Consumer<Map<String, Object>> listener;
    private Thread pollingThread;

    @Override
    public String getName() {
        return "intelligence-event-consumer";
    }

    @Override
    public void start() {
        if (running) return;
        running = true;
        pollingThread = new Thread(this::pollLoop, "ikasan-intelligence-consumer");
        pollingThread.setDaemon(true);
        pollingThread.start();
        log.info("IntelligenceEventConsumer started");
    }

    @Override
    public void stop() {
        running = false;
        if (pollingThread != null) {
            pollingThread.interrupt();
            pollingThread = null;
        }
        log.info("IntelligenceEventConsumer stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void setListener(Consumer<Map<String, Object>> listener) {
        this.listener = listener;
    }

    /**
     * Submit a trade event payload for AI analysis.
     * Thread-safe; returns false if the queue is full.
     */
    public boolean submit(Map<String, Object> eventPayload) {
        boolean accepted = eventQueue.offer(eventPayload);
        if (!accepted) {
            log.warn("IntelligenceEventConsumer: event queue full — event dropped for tradeId={}",
                    eventPayload.get("tradeId"));
        }
        return accepted;
    }

    private void pollLoop() {
        while (running) {
            try {
                Map<String, Object> event = eventQueue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (event != null && listener != null) {
                    log.debug("IntelligenceEventConsumer: dispatching event tradeId={}", event.get("tradeId"));
                    listener.accept(event);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("IntelligenceEventConsumer: error processing event: {}", e.getMessage(), e);
            }
        }
    }
}
