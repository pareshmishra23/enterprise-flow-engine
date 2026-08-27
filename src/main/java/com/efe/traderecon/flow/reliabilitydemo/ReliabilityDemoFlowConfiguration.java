package com.efe.traderecon.flow.reliabilitydemo;

import com.efe.traderecon.ikasan.builder.FlowBuilder;
import com.efe.traderecon.ikasan.model.*;
import com.efe.traderecon.ikasan.ui.FlowWiretapStore;
import com.efe.traderecon.reliability.ReliabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Reliability demo flow. Demonstrates EFE-010 flow-level reliability handling:
 * a processor that fails transiently so the flow engine's reliability path
 * retries with backoff before succeeding (or DLQing on permanent/exhausted
 * failures). The business processor stays infrastructure-agnostic.
 */
@Configuration
public class ReliabilityDemoFlowConfiguration {

    public static final String FLOW_NAME = "reliability-demo-flow";

    @Bean("reliabilityDemoFlow")
    public IkasanFlow reliabilityDemoFlow(
            @Value("${esb.module-name:enterprise-flow-engine}") String moduleName,
            ReliabilityDemoConsumer consumer,
            ReliabilityDemoProcessor processor,
            ReliabilityDemoProducer producer,
            ReliabilityService reliabilityService,
            FlowWiretapStore wiretapStore) {

        IkasanFlow flow = new FlowBuilder(FLOW_NAME, moduleName)
                .consumer("RELIABILITY-IN", consumer)
                .processor("RELIABILITY-DEALER", processor)
                .producer("RELIABILITY-OUT", producer)
                .reliable(reliabilityService, event -> {
                    if (event instanceof ReliabilityMessage m) return m.getMessageId();
                    return String.valueOf(event);
                })
                .build();

        flow.addWiretap(wiretapStore.listener());
        return flow;
    }

    // =============================================================
    // 1. Consumer: RELIABILITY-IN
    // =============================================================
    @Component
    public static class ReliabilityDemoConsumer implements IkasanConsumer<ReliabilityMessage> {
        private static final Logger log = LoggerFactory.getLogger(ReliabilityDemoConsumer.class);
        private volatile boolean running = false;
        private Consumer<ReliabilityMessage> listener;

        @Override public String getName() { return "RELIABILITY-IN"; }
        @Override public void start() { running = true; log.info("RELIABILITY-IN consumer started"); }
        @Override public void stop() { running = false; log.info("RELIABILITY-IN consumer stopped"); }
        @Override public boolean isRunning() { return running; }
        @Override public void setListener(Consumer<ReliabilityMessage> listener) { this.listener = listener; }

        public void publish(ReliabilityMessage message) {
            if (listener != null && running) {
                listener.accept(message);
            } else {
                throw new IllegalStateException("RELIABILITY-IN consumer is not running or has no listener");
            }
        }
    }

    // =============================================================
    // 2. Processor: RELIABILITY-DEALER (fails transiently)
    // =============================================================
    @Component
    public static class ReliabilityDemoProcessor implements IkasanProcessor<ReliabilityMessage, ReliabilityMessage> {
        private static final Logger log = LoggerFactory.getLogger(ReliabilityDemoProcessor.class);
        public static final int REQUIRED_ATTEMPTS = 2;
        private final AtomicInteger invocationCount = new AtomicInteger();

        @Override public String getName() { return "RELIABILITY-DEALER"; }

        @Override
        public ReliabilityMessage process(ReliabilityMessage message) {
            int attempt = invocationCount.incrementAndGet();
            if (message.isFailingPermanent()) {
                throw new IllegalArgumentException("Permanent business failure: invalid payload");
            }
            if (attempt < REQUIRED_ATTEMPTS) {
                log.info("RELIABILITY-DEALER transient failure on attempt {} for message [{}]", attempt, message.getMessageId());
                throw new RuntimeException("Transient downstream unavailability");
            }
            log.info("RELIABILITY-DEALER processed message [{}] on attempt {}", message.getMessageId(), attempt);
            message.markProcessed(attempt);
            return message;
        }

        public void reset() { invocationCount.set(0); }
    }

    // =============================================================
    // 3. Producer: RELIABILITY-OUT
    // =============================================================
    @Component
    public static class ReliabilityDemoProducer implements IkasanProducer<ReliabilityMessage> {
        private static final Logger log = LoggerFactory.getLogger(ReliabilityDemoProducer.class);
        private final List<ReliabilityMessage> store = new CopyOnWriteArrayList<>();

        @Override public String getName() { return "RELIABILITY-OUT"; }

        @Override
        public void produce(ReliabilityMessage message) {
            log.info("RELIABILITY-OUT emitted message [{}]", message.getMessageId());
            store.add(message);
        }

        public List<ReliabilityMessage> getStore() { return store; }
        public void clear() { store.clear(); }
    }
}
