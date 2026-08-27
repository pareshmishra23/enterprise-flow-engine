package com.efe.traderecon.flow.asyncdemo;

import com.efe.traderecon.execution.EfeExecutorService;
import com.efe.traderecon.ikasan.builder.FlowBuilder;
import com.efe.traderecon.ikasan.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Configuration
public class AsyncDemoFlowConfiguration {

    public static final String FLOW_NAME = "async-demo-flow";

    @Bean("asyncDemoFlow")
    public IkasanFlow asyncDemoFlow(
            @Value("${esb.module-name:trade-recon-esb}") String moduleName,
            AsyncScheduledConsumer consumer,
            AsyncTaskRetrievalBroker broker,
            AsyncTaskSplitter splitter,
            AsyncWorkerProcessor processor,
            AsyncResultProducer producer) {

        return new FlowBuilder(FLOW_NAME, moduleName)
                .consumer("Async Scheduled Consumer", consumer)
                .broker("Async Task Retrieval Broker", broker)
                .splitter("Async Task Splitter", splitter)
                .producer("Async Result Producer", producer)
                .build();
    }

    @Component
    public static class AsyncScheduledConsumer implements IkasanConsumer<String> {
        private volatile boolean running = false;
        private Consumer<String> listener;

        @Override public String getName() { return "async-scheduled-consumer"; }
        @Override public void start() { running = true; }
        @Override public void stop() { running = false; }
        @Override public boolean isRunning() { return running; }
        @Override public void setListener(Consumer<String> listener) { this.listener = listener; }

        public void trigger(String batchId) {
            if (listener != null) listener.accept(batchId);
        }
    }

    @Component
    public static class AsyncTaskRetrievalBroker implements IkasanBroker<String, List<String>> {
        @Override public String getName() { return "async-task-retrieval-broker"; }
        @Override public List<String> invoke(String batchId) {
            List<String> items = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                items.add("EVENT-" + batchId + "-" + i);
            }
            return items;
        }
    }

    @Component
    public static class AsyncTaskSplitter implements IkasanSplitter<List<String>, String> {
        @Override public String getName() { return "async-task-splitter"; }
        @Override public List<String> split(List<String> events) {
            return events != null ? events : List.of();
        }
    }

    @Component
    public static class AsyncWorkerProcessor {
        private static final Logger log = LoggerFactory.getLogger(AsyncWorkerProcessor.class);
        private final EfeExecutorService executorService;
        private final List<String> processedEvents = new CopyOnWriteArrayList<>();

        public AsyncWorkerProcessor(EfeExecutorService executorService) {
            this.executorService = executorService;
        }

        public void processAsync(List<String> events, CountDownLatch latch) {
            for (String event : events) {
                executorService.execute(() -> {
                    try {
                        // simulate bounded async work
                        Thread.sleep(10);
                        processedEvents.add(event);
                        log.debug("Async worker processed event: {}", event);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        if (latch != null) latch.countDown();
                    }
                });
            }
        }

        public List<String> getProcessedEvents() { return processedEvents; }
        public void clear() { processedEvents.clear(); }
    }

    @Component
    public static class AsyncResultProducer implements IkasanProducer<String> {
        private static final Logger log = LoggerFactory.getLogger(AsyncResultProducer.class);
        private final List<String> emittedResults = new CopyOnWriteArrayList<>();

        @Override public String getName() { return "async-result-producer"; }
        @Override public void produce(String event) {
            emittedResults.add(event);
            log.info("Emitted async event: {}", event);
        }

        public List<String> getEmittedResults() { return emittedResults; }
        public void clear() { emittedResults.clear(); }
    }
}
