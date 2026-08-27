package com.efe.traderecon.flow.dispatch;

import com.efe.traderecon.ikasan.model.IkasanConsumer;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.function.Consumer;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Quartz-backed scheduled consumer (Ikasan semantics) for the
 * reconciliation-dispatch flow. Replaces a raw ScheduledExecutorService with a
 * first-class Quartz Scheduler/Trigger/Job so the trigger cadence is durable,
 * observable via Quartz SPI, and aligned with EFE-010 ("Quartz-backed scheduler").
 */
@Component
public class ScheduledTaskConsumer implements IkasanConsumer<ScheduledTriggerEvent> {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskConsumer.class);

    public static final String JOB_KEY = "reconciliation-dispatch-job";
    public static final String CALLBACK_KEY = "listener";

    private final boolean enabled;
    private final long intervalMs;
    private volatile boolean running = false;
    private Scheduler scheduler;
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

        try {
            this.scheduler = new StdSchedulerFactory().getScheduler();

            JobDataMap jobData = new JobDataMap();
            jobData.put(CALLBACK_KEY, (Consumer<ScheduledTriggerEvent>) event -> fireTrigger());

            JobDetail job = newJob(TriggerJob.class)
                    .withIdentity(JOB_KEY, "ikasan-schedulers")
                    .usingJobData(jobData)
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity(JOB_KEY + "-trigger", "ikasan-schedulers")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInMilliseconds(intervalMs)
                            .repeatForever())
                    .build();

            scheduler.scheduleJob(job, trigger);
            scheduler.start();

            this.running = true;
            log.info("ScheduledTaskConsumer started with Quartz interval {}ms", intervalMs);
        } catch (SchedulerException e) {
            log.error("Failed to start Quartz scheduler for ScheduledTaskConsumer: {}", e.getMessage(), e);
        }
    }

    @Override
    public synchronized void stop() {
        if (!running) return;
        this.running = false;
        if (scheduler != null) {
            try {
                scheduler.shutdown(false);
            } catch (SchedulerException e) {
                log.warn("Error shutting down Quartz scheduler: {}", e.getMessage());
            }
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
                log.debug("ScheduledTaskConsumer firing Quartz trigger at {}", Instant.now());
                listener.accept(new ScheduledTriggerEvent(JOB_KEY, Instant.now()));
            }
        } catch (Exception e) {
            log.error("Error in ScheduledTaskConsumer trigger execution: {}", e.getMessage(), e);
        }
    }

    /**
     * Quartz {@link Job} that forwards the scheduled event to the consumer's
     * listener. The listener is carried in the {@link JobDataMap} so this class
     * stays stateless and Quartz-managed.
     */
    @SuppressWarnings("unchecked")
    public static class TriggerJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            Object callback = context.getMergedJobDataMap().get(CALLBACK_KEY);
            if (callback instanceof Consumer) {
                ((Consumer<ScheduledTriggerEvent>) callback).accept(
                        new ScheduledTriggerEvent(JOB_KEY, Instant.now()));
            }
        }
    }
}
