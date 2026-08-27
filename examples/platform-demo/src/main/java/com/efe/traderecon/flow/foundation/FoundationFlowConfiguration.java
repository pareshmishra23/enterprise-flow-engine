package com.efe.traderecon.flow.foundation;

import com.efe.traderecon.ikasan.builder.FlowBuilder;
import com.efe.traderecon.ikasan.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class FoundationFlowConfiguration {

    public static final String FOUNDATION_FLOW_NAME = "efe-foundation-flow";
    public static final String SCHEDULED_FLOW_NAME = "efe-scheduled-foundation-flow";
    public static final String ROUTER_FLOW_NAME = "efe-router-foundation-flow";

    // -------------------------------------------------------------
    // 1. Foundation Flow (Consumer -> Converter -> Processor -> Producer)
    // -------------------------------------------------------------
    @Bean("efeFoundationFlow")
    public IkasanFlow efeFoundationFlow(
            @Value("${esb.module-name:trade-recon-esb}") String moduleName,
            FoundationEventConsumer consumer,
            FoundationJsonConverter converter,
            FoundationBusinessProcessor processor,
            FoundationResultProducer producer) {

        return new FlowBuilder(FOUNDATION_FLOW_NAME, moduleName)
                .consumer("EFE-FOUNDATION-IN", consumer)
                .converter("EFE-JSON-CONVERTER", converter)
                .processor("EFE-FOUNDATION-PROCESSOR", processor)
                .producer("EFE-FOUNDATION-OUT", producer)
                .build();
    }

    // -------------------------------------------------------------
    // 2. Scheduled Foundation Flow (Scheduled Consumer -> Broker -> Processor -> Producer)
    // -------------------------------------------------------------
    @Bean("efeScheduledFoundationFlow")
    public IkasanFlow efeScheduledFoundationFlow(
            @Value("${esb.module-name:trade-recon-esb}") String moduleName,
            FoundationScheduledConsumer consumer,
            FoundationTaskBroker broker,
            FoundationScheduledProcessor processor,
            FoundationScheduledProducer producer) {

        return new FlowBuilder(SCHEDULED_FLOW_NAME, moduleName)
                .consumer("EFE-SCHEDULED-IN", consumer)
                .broker("EFE-TASK-BROKER", broker)
                .processor("EFE-SCHEDULED-PROCESSOR", processor)
                .producer("EFE-SCHEDULED-OUT", producer)
                .build();
    }

    // -------------------------------------------------------------
    // 3. Router Foundation Flow (Consumer -> Processor -> Router -> Producer A / Producer B)
    // -------------------------------------------------------------
    @Bean("efeRouterFoundationFlow")
    public IkasanFlow efeRouterFoundationFlow(
            @Value("${esb.module-name:trade-recon-esb}") String moduleName,
            RouterEventConsumer consumer,
            RouterBusinessProcessor processor,
            FoundationRouter router,
            FoundationRouteAProducer producerA,
            FoundationRouteBProducer producerB) {

        return new FlowBuilder(ROUTER_FLOW_NAME, moduleName)
                .consumer("EFE-ROUTER-IN", consumer)
                .processor("EFE-ROUTER-PROCESSOR", processor)
                .router("EFE-ROUTER", router, routes -> routes
                        .when("A", producerA)
                        .when("B", producerB)
                )
                .build();
    }

    // =============================================================
    // Foundation Flow Components
    // =============================================================

    @Component
    public static class FoundationEventConsumer implements IkasanConsumer<String> {
        private volatile boolean running = false;
        private Consumer<String> listener;

        @Override public String getName() { return "EFE-FOUNDATION-IN"; }
        @Override public void start() { running = true; }
        @Override public void stop() { running = false; }
        @Override public boolean isRunning() { return running; }
        @Override public void setListener(Consumer<String> listener) { this.listener = listener; }

        public void publish(String eventJson) {
            if (listener != null && running) {
                listener.accept(eventJson);
            }
        }
    }

    @Component
    public static class FoundationJsonConverter implements IkasanConverter<String, Map<String, Object>> {
        private static final Logger log = LoggerFactory.getLogger(FoundationJsonConverter.class);
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override public String getName() { return "EFE-JSON-CONVERTER"; }

        @Override
        @SuppressWarnings("unchecked")
        public Map<String, Object> convert(String source) {
            log.debug("FoundationJsonConverter converting source: {}", source);
            try {
                if (source.startsWith("{")) {
                    return objectMapper.readValue(source, Map.class);
                }
                return Map.of("raw", source, "timestamp", System.currentTimeMillis());
            } catch (Exception e) {
                return Map.of("raw", source, "error", e.getMessage());
            }
        }
    }

    @Component
    public static class FoundationBusinessProcessor implements IkasanProcessor<Map<String, Object>, Map<String, Object>> {
        private static final Logger log = LoggerFactory.getLogger(FoundationBusinessProcessor.class);

        @Override public String getName() { return "EFE-FOUNDATION-PROCESSOR"; }

        @Override
        public Map<String, Object> process(Map<String, Object> payload) {
            log.info("FoundationBusinessProcessor processing payload: {}", payload);
            Map<String, Object> result = new HashMap<>(payload);
            result.put("status", "PROCESSED");
            result.put("processedAt", System.currentTimeMillis());
            return result;
        }
    }

    @Component
    public static class FoundationResultProducer implements IkasanProducer<Map<String, Object>> {
        private static final Logger log = LoggerFactory.getLogger(FoundationResultProducer.class);
        private final List<Map<String, Object>> producedResults = new CopyOnWriteArrayList<>();

        @Override public String getName() { return "EFE-FOUNDATION-OUT"; }

        @Override
        public void produce(Map<String, Object> event) {
            log.info("FoundationResultProducer received event: {}", event);
            producedResults.add(event);
        }

        public List<Map<String, Object>> getProducedResults() { return producedResults; }
        public void clear() { producedResults.clear(); }
    }

    // =============================================================
    // Scheduled Flow Components
    // =============================================================

    @Component
    public static class FoundationScheduledConsumer implements IkasanConsumer<String> {
        private volatile boolean running = false;
        private Consumer<String> listener;

        @Override public String getName() { return "EFE-SCHEDULED-IN"; }
        @Override public void start() { running = true; }
        @Override public void stop() { running = false; }
        @Override public boolean isRunning() { return running; }
        @Override public void setListener(Consumer<String> listener) { this.listener = listener; }

        public void trigger(String scheduleContext) {
            if (listener != null && running) {
                listener.accept(scheduleContext);
            }
        }
    }

    @Component
    public static class FoundationTaskBroker implements IkasanBroker<String, Map<String, Object>> {
        @Override public String getName() { return "EFE-TASK-BROKER"; }
        @Override
        public Map<String, Object> invoke(String context) {
            return Map.of("taskId", "TSK-SCHED-01", "context", context, "fetchedAt", System.currentTimeMillis());
        }
    }

    @Component
    public static class FoundationScheduledProcessor implements IkasanProcessor<Map<String, Object>, Map<String, Object>> {
        @Override public String getName() { return "EFE-SCHEDULED-PROCESSOR"; }
        @Override
        public Map<String, Object> process(Map<String, Object> payload) {
            Map<String, Object> res = new HashMap<>(payload);
            res.put("status", "EXECUTED");
            return res;
        }
    }

    @Component
    public static class FoundationScheduledProducer implements IkasanProducer<Map<String, Object>> {
        private final List<Map<String, Object>> producedEvents = new CopyOnWriteArrayList<>();

        @Override public String getName() { return "EFE-SCHEDULED-OUT"; }
        @Override
        public void produce(Map<String, Object> event) {
            producedEvents.add(event);
        }
        public List<Map<String, Object>> getProducedEvents() { return producedEvents; }
        public void clear() { producedEvents.clear(); }
    }

    // =============================================================
    // Router Flow Components
    // =============================================================

    public static class RoutedEvent {
        private final String route;
        private final String payload;

        public RoutedEvent(String route, String payload) {
            this.route = route;
            this.payload = payload;
        }

        public String getRoute() { return route; }
        public String getPayload() { return payload; }
    }

    @Component
    public static class RouterEventConsumer implements IkasanConsumer<RoutedEvent> {
        private volatile boolean running = false;
        private Consumer<RoutedEvent> listener;

        @Override public String getName() { return "EFE-ROUTER-IN"; }
        @Override public void start() { running = true; }
        @Override public void stop() { running = false; }
        @Override public boolean isRunning() { return running; }
        @Override public void setListener(Consumer<RoutedEvent> listener) { this.listener = listener; }

        public void publish(RoutedEvent event) {
            if (listener != null && running) {
                listener.accept(event);
            }
        }
    }

    @Component
    public static class RouterBusinessProcessor implements IkasanProcessor<RoutedEvent, RoutedEvent> {
        @Override public String getName() { return "EFE-ROUTER-PROCESSOR"; }
        @Override
        public RoutedEvent process(RoutedEvent event) {
            return event;
        }
    }

    @Component
    public static class FoundationRouter implements IkasanRouter<RoutedEvent> {
        @Override public String getName() { return "EFE-ROUTER"; }
        @Override
        public String route(RoutedEvent event) {
            return event != null ? event.getRoute() : "UNKNOWN";
        }
    }

    @Component
    public static class FoundationRouteAProducer implements IkasanProducer<RoutedEvent> {
        private final List<RoutedEvent> receivedEvents = new CopyOnWriteArrayList<>();
        @Override public String getName() { return "EFE-PRODUCER-A"; }
        @Override public void produce(RoutedEvent event) { receivedEvents.add(event); }
        public List<RoutedEvent> getReceivedEvents() { return receivedEvents; }
        public void clear() { receivedEvents.clear(); }
    }

    @Component
    public static class FoundationRouteBProducer implements IkasanProducer<RoutedEvent> {
        private final List<RoutedEvent> receivedEvents = new CopyOnWriteArrayList<>();
        @Override public String getName() { return "EFE-PRODUCER-B"; }
        @Override public void produce(RoutedEvent event) { receivedEvents.add(event); }
        public List<RoutedEvent> getReceivedEvents() { return receivedEvents; }
        public void clear() { receivedEvents.clear(); }
    }
}
