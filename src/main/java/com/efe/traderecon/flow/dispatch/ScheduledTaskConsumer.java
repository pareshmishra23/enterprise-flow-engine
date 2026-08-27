package com.efe.traderecon.flow.dispatch;

import com.efe.traderecon.ikasan.model.IkasanConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class ScheduledTaskConsumer implements IkasanConsumer<ScheduledTriggerEvent> {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskConsumer.class);

    private final boolean enabled;
    private final long intervalMs;
    private volatile boolean running = false;
    private ScheduledExecutorService scheduler;
    private Consumer<ScheduledTriggerEvent> listener;

    public ScheduledTaskConsumer(
            @Value("${scheduler.reconciliation-dispatch.enabled:true}") boolean enabled,
            @Value("${scheduler.reconciliation-dispatch.interval-ms:5000}") long intervalMs) {
        this.enabled = enabled;
        this.intervalMs = intervalMs;
    }

    @Override
    public String getName() {
        return "scheduled-task-consumer";
    }

    @Override
    public synchronized void start() {
        if (!enabled) {
            log.info("ScheduledTaskConsumer is disabled by configuration");
            return;
        }
        if (running) return;

        this.running = true;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ikasan-dispatch-scheduler");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleWithFixedDelay(this::fireTrigger, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
        log.info("ScheduledTaskConsumer started with interval {}ms", intervalMs);
    }

    @Override
    public synchronized void stop() {
        if (!running) return;
        this.running = false;
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        log.info("ScheduledTaskConsumer stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void setListener(Consumer<ScheduledTriggerEvent> listener) {
        this.listener = listener;
    }

    public void triggerManually() {
        fireTrigger();
    }

    private void fireTrigger() {
        if (!running) return;
        try {
            if (listener != null) {
                log.debug("ScheduledTaskConsumer firing trigger at {}", Instant.now());
                listener.accept(new ScheduledTriggerEvent("reconciliation-dispatch-job", Instant.now()));
            }
        } catch (Exception e) {
            log.error("Error in ScheduledTaskConsumer trigger execution: {}", e.getMessage(), e);
        }
    }
}
