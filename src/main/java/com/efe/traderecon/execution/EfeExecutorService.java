package com.efe.traderecon.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * EFE Bounded Executor Facility.
 * Provides a bounded worker thread pool with configurable concurrency, queue capacity,
 * rejection policy, and operational metrics exposed via JMX and logs.
 */
@Component
public class EfeExecutorService {

    private static final Logger log = LoggerFactory.getLogger(EfeExecutorService.class);

    private final ThreadPoolExecutor executor;
    private final AtomicLong rejectedTaskCount = new AtomicLong(0);

    public EfeExecutorService(EfeExecutionProperties properties) {
        int coreSize = properties.getWorkers().getCoreSize();
        int maxSize = properties.getWorkers().getMaxSize();
        int queueCapacity = properties.getWorkers().getQueueCapacity();

        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(queueCapacity);
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicLong counter = new AtomicLong(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "efe-worker-" + counter.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        };

        RejectedExecutionHandler rejectionHandler = (r, exec) -> {
            rejectedTaskCount.incrementAndGet();
            log.warn("EFE Executor task rejected! Queue full (capacity={})", queueCapacity);
            throw new RejectedExecutionException("EFE worker queue saturated");
        };

        this.executor = new ThreadPoolExecutor(
                coreSize,
                maxSize,
                60L,
                TimeUnit.SECONDS,
                queue,
                threadFactory,
                rejectionHandler
        );
        log.info("EfeExecutorService initialized with coreSize={}, maxSize={}, queueCapacity={}",
                coreSize, maxSize, queueCapacity);
    }

    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    public void execute(Runnable task) {
        executor.execute(task);
    }

    public int getActiveThreads() { return executor.getActiveCount(); }
    public int getPoolSize() { return executor.getPoolSize(); }
    public int getCorePoolSize() { return executor.getCorePoolSize(); }
    public int getMaximumPoolSize() { return executor.getMaximumPoolSize(); }
    public int getQueueSize() { return executor.getQueue().size(); }
    public long getCompletedTasks() { return executor.getCompletedTaskCount(); }
    public long getTaskCount() { return executor.getTaskCount(); }
    public long getRejectedTasks() { return rejectedTaskCount.get(); }

    public void shutdown() {
        executor.shutdown();
    }
}
