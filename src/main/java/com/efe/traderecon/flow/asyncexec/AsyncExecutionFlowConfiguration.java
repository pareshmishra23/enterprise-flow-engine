package com.efe.traderecon.flow.asyncexec;

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
import java.util.concurrent.*;
import java.util.function.Consumer;

@Configuration
public class AsyncExecutionFlowConfiguration {

    public static final String FLOW_NAME = "efe-async-flow";

    @Bean("efeAsyncFlow")
    public IkasanFlow efeAsyncFlow(
            @Value("${esb.module-name:enterprise-flow-engine}") String moduleName,
            EfeAsyncScheduledConsumer consumer,
            EfeAsyncTaskBroker broker,
            EfeAsyncSplitter splitter,
            EfeAsyncWorkerProcessor processor,
            EfeAsyncResultProducer producer) {

        return new FlowBuilder(FLOW_NAME, moduleName)
                .consumer("EFE-ASYNC-SCHEDULED-IN", consumer)
                .broker("EFE-ASYNC-TASK-BROKER", broker)
                .splitter("EFE-ASYNC-SPLITTER", splitter)
                .processor("EFE-ASYNC-WORKER-PROCESSOR", processor)
                .producer("EFE-ASYNC-OUT", producer)
                .build();
    }

    // =============================================================
    // 1. Consumer: EFE-ASYNC-SCHEDULED-IN
    // =============================================================
    @Component
    public static class EfeAsyncScheduledConsumer implements IkasanConsumer<String> {
        private static final Logger log = LoggerFactory.getLogger(EfeAsyncScheduledConsumer.class);
        private volatile boolean running = false;
        private Consumer<String> listener;

        @Override public String getName() { return "EFE-ASYNC-SCHEDULED-IN"; }
        @Override public void start() { running = true; log.info("EFE-ASYNC-SCHEDULED-IN Consumer started"); }
        @Override public void stop() { running = false; log.info("EFE-ASYNC-SCHEDULED-IN Consumer stopped"); }
        @Override public boolean isRunning() { return running; }
        @Override public void setListener(Consumer<String> listener) { this.listener = listener; }

        public void trigger(String batchId) {
            if (listener != null && running) {
                listener.accept(batchId);
            }
        }
    }

    // =============================================================
    // 2. Broker: EFE-ASYNC-TASK-BROKER
    // =============================================================
    @Component
    public static class EfeAsyncTaskBroker implements IkasanBroker<String, List<Map<String, Object>>> {
        private static final Logger log = LoggerFactory.getLogger(EfeAsyncTaskBroker.class);

        @Override public String getName() { return "EFE-ASYNC-TASK-BROKER"; }

        @Override
        public List<Map<String, Object>> invoke(String batchId) {
            log.info("EFE-ASYNC-TASK-BROKER fetching batch [{}]", batchId);
            List<Map<String, Object>> tasks = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                Map<String, Object> task = new HashMap<>();
                task.put("taskId", "TSK-ASYNC-" + batchId + "-" + String.format("%02d", i));
                task.put("batchId", batchId);
                task.put("itemIndex", i);
                task.put("amount", 100.0 * i);
                task.put("createdAt", System.currentTimeMillis());
                tasks.add(task);
            }
            return tasks;
        }
    }

    // =============================================================
    // 3. Splitter: EFE-ASYNC-SPLITTER
    // =============================================================
    @Component
    public static class EfeAsyncSplitter implements IkasanSplitter<List<Map<String, Object>>, Map<String, Object>> {
        private static final Logger log = LoggerFactory.getLogger(EfeAsyncSplitter.class);

        @Override public String getName() { return "EFE-ASYNC-SPLITTER"; }

        @Override
        public List<Map<String, Object>> split(List<Map<String, Object>> batch) {
            log.debug("EFE-ASYNC-SPLITTER partitioning batch of {} items", batch != null ? batch.size() : 0);
            return batch != null ? batch : List.of();
        }
    }

    // =============================================================
    // 4. Processor: EFE-ASYNC-WORKER-PROCESSOR
    // =============================================================
    @Component
    public static class EfeAsyncWorkerProcessor implements IkasanProcessor<Map<String, Object>, Map<String, Object>> {
        private static final Logger log = LoggerFactory.getLogger(EfeAsyncWorkerProcessor.class);
        private final EfeExecutorService executorService;

        public EfeAsyncWorkerProcessor(EfeExecutorService executorService) {
            this.executorService = executorService;
        }

        @Override public String getName() { return "EFE-ASYNC-WORKER-PROCESSOR"; }

        @Override
        public Map<String, Object> process(Map<String, Object> task) {
            Map<String, Object> result = new HashMap<>(task);
            // Execute bounded asynchronous task computation
            Future<String> future = executorService.submit(() -> {
                String threadName = Thread.currentThread().getName();
                log.debug("Worker thread [{}] executing async task [{}]", threadName, task.get("taskId"));
                return "COMPLETED_ON_" + threadName;
            });

            try {
                String executionOutcome = future.get(3, TimeUnit.SECONDS);
                result.put("status", "COMPLETED");
                result.put("workerOutcome", executionOutcome);
                result.put("completedAt", System.currentTimeMillis());
            } catch (Exception e) {
                log.error("Async execution failed for task [{}]: {}", task.get("taskId"), e.getMessage());
                result.put("status", "FAILED");
                result.put("error", e.getMessage());
            }

            return result;
        }
    }

    // =============================================================
    // 5. Producer: EFE-ASYNC-OUT
    // =============================================================
    @Component
    public static class EfeAsyncResultProducer implements IkasanProducer<Map<String, Object>> {
        private static final Logger log = LoggerFactory.getLogger(EfeAsyncResultProducer.class);
        private final List<Map<String, Object>> completedResults = new CopyOnWriteArrayList<>();

        @Override public String getName() { return "EFE-ASYNC-OUT"; }

        @Override
        public void produce(Map<String, Object> event) {
            log.info("EFE-ASYNC-OUT received completed task: [{}] status: [{}]",
                    event.get("taskId"), event.get("status"));
            completedResults.add(event);
        }

        public List<Map<String, Object>> getCompletedResults() { return completedResults; }
        public void clear() { completedResults.clear(); }
    }
}
