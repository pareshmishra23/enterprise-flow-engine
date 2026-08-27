package com.efe.traderecon.flow.core;

import com.efe.traderecon.ikasan.builder.FlowBuilder;
import com.efe.traderecon.ikasan.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Configuration
public class EfeCoreFlowConfiguration {

    public static final String CORE_FLOW_NAME = "efe-core-flow";

    @Bean("efeCoreFlow")
    public IkasanFlow efeCoreFlow(
            @Value("${esb.module-name:trade-recon-esb}") String moduleName,
            EfeCoreEventConsumer consumer,
            EfeCoreConverter converter,
            EfeCoreValidator validator,
            EfeCoreProcessor processor,
            EfeCoreRouter router,
            EfeMatchProducer matchProducer,
            EfeBreakProducer breakProducer) {

        return new FlowBuilder(CORE_FLOW_NAME, moduleName)
                .consumer("EFE-CORE-IN", consumer)
                .converter("EFE-CORE-CONVERTER", converter)
                .translator("EFE-CORE-VALIDATOR", validator)
                .processor("EFE-CORE-PROCESSOR", processor)
                .router("EFE-CORE-ROUTER", router, routes -> routes
                        .when("MATCH", matchProducer)
                        .when("BREAK", breakProducer)
                )
                .build();
    }

    // =============================================================
    // 1. Consumer: EFE-CORE-IN
    // =============================================================
    @Component
    public static class EfeCoreEventConsumer implements IkasanConsumer<String> {
        private static final Logger log = LoggerFactory.getLogger(EfeCoreEventConsumer.class);
        private volatile boolean running = false;
        private Consumer<String> listener;

        @Override public String getName() { return "EFE-CORE-IN"; }
        @Override public void start() { running = true; log.info("EFE-CORE-IN Consumer started"); }
        @Override public void stop() { running = false; log.info("EFE-CORE-IN Consumer stopped"); }
        @Override public boolean isRunning() { return running; }
        @Override public void setListener(Consumer<String> listener) { this.listener = listener; }

        public void publish(String eventJson) {
            if (listener != null && running) {
                listener.accept(eventJson);
            } else {
                throw new IllegalStateException("EFE-CORE-IN consumer is not running or has no listener");
            }
        }
    }

    // =============================================================
    // 2. Converter: EFE-CORE-CONVERTER
    // =============================================================
    @Component
    public static class EfeCoreConverter implements IkasanConverter<String, EfeCoreEvent> {
        private static final Logger log = LoggerFactory.getLogger(EfeCoreConverter.class);
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override public String getName() { return "EFE-CORE-CONVERTER"; }

        @Override
        public EfeCoreEvent convert(String source) {
            log.debug("EFE-CORE-CONVERTER converting raw payload: {}", source);
            try {
                if (source == null || source.isBlank()) {
                    throw new IllegalArgumentException("Payload is empty or null");
                }
                return objectMapper.readValue(source, EfeCoreEvent.class);
            } catch (Exception e) {
                log.warn("EFE-CORE-CONVERTER failed to parse JSON: {}", e.getMessage());
                EfeCoreEvent errorEvent = new EfeCoreEvent();
                errorEvent.setErrorMessage("Malformed JSON: " + e.getMessage());
                return errorEvent;
            }
        }
    }

    // =============================================================
    // 3. Translator / Validator: EFE-CORE-VALIDATOR
    // =============================================================
    @Component
    public static class EfeCoreValidator implements IkasanTranslator<EfeCoreEvent> {
        private static final Logger log = LoggerFactory.getLogger(EfeCoreValidator.class);

        @Override public String getName() { return "EFE-CORE-VALIDATOR"; }

        @Override
        public EfeCoreEvent translate(EfeCoreEvent event) {
            if (event == null) {
                throw new IllegalArgumentException("Event is null");
            }
            if (event.getErrorMessage() != null) {
                throw new IllegalArgumentException(event.getErrorMessage());
            }
            if (event.getEventId() == null || event.getEventId().isBlank()) {
                throw new IllegalArgumentException("eventId is required and cannot be empty");
            }
            if (event.getType() == null || event.getType().isBlank()) {
                throw new IllegalArgumentException("type is required");
            }
            if (event.getExpectedQuantity() == null) {
                throw new IllegalArgumentException("expectedQuantity is required");
            }
            if (event.getActualQuantity() == null) {
                throw new IllegalArgumentException("actualQuantity is required");
            }

            log.debug("EFE-CORE-VALIDATOR validated event: [{}]", event.getEventId());
            return event;
        }
    }

    // =============================================================
    // 4. Core Processor: EFE-CORE-PROCESSOR
    // =============================================================
    @Component
    public static class EfeCoreProcessor implements IkasanProcessor<EfeCoreEvent, EfeCoreEvent> {
        private static final Logger log = LoggerFactory.getLogger(EfeCoreProcessor.class);

        @Override public String getName() { return "EFE-CORE-PROCESSOR"; }

        @Override
        public EfeCoreEvent process(EfeCoreEvent event) {
            boolean matched = Double.compare(event.getExpectedQuantity(), event.getActualQuantity()) == 0;
            String status = matched ? "MATCH" : "BREAK";
            event.setStatus(status);
            event.setProcessedAt(System.currentTimeMillis());
            log.info("EFE-CORE-PROCESSOR evaluated event [{}] -> status: [{}] (expected: {}, actual: {})",
                    event.getEventId(), status, event.getExpectedQuantity(), event.getActualQuantity());
            return event;
        }
    }

    // =============================================================
    // 5. Router: EFE-CORE-ROUTER
    // =============================================================
    @Component
    public static class EfeCoreRouter implements IkasanRouter<EfeCoreEvent> {
        private static final Logger log = LoggerFactory.getLogger(EfeCoreRouter.class);

        @Override public String getName() { return "EFE-CORE-ROUTER"; }

        @Override
        public String route(EfeCoreEvent event) {
            String route = (event != null && event.getStatus() != null) ? event.getStatus() : "UNKNOWN";
            log.debug("EFE-CORE-ROUTER routing event [{}] to destination: [{}]",
                    event != null ? event.getEventId() : "null", route);
            return route;
        }
    }

    // =============================================================
    // 6. Producer A: EFE-MATCH-OUT
    // =============================================================
    @Component
    public static class EfeMatchProducer implements IkasanProducer<EfeCoreEvent> {
        private static final Logger log = LoggerFactory.getLogger(EfeMatchProducer.class);
        private final List<EfeCoreEvent> matchStore = new CopyOnWriteArrayList<>();

        @Override public String getName() { return "EFE-MATCH-OUT"; }

        @Override
        public void produce(EfeCoreEvent event) {
            log.info("EFE-MATCH-OUT received matching event: [{}]", event.getEventId());
            matchStore.add(event);
        }

        public List<EfeCoreEvent> getMatchStore() { return matchStore; }
        public void clear() { matchStore.clear(); }
    }

    // =============================================================
    // 7. Producer B: EFE-BREAK-OUT
    // =============================================================
    @Component
    public static class EfeBreakProducer implements IkasanProducer<EfeCoreEvent> {
        private static final Logger log = LoggerFactory.getLogger(EfeBreakProducer.class);
        private final List<EfeCoreEvent> breakStore = new CopyOnWriteArrayList<>();

        @Override public String getName() { return "EFE-BREAK-OUT"; }

        @Override
        public void produce(EfeCoreEvent event) {
            log.info("EFE-BREAK-OUT received breaking event: [{}]", event.getEventId());
            breakStore.add(event);
        }

        public List<EfeCoreEvent> getBreakStore() { return breakStore; }
        public void clear() { breakStore.clear(); }
    }
}
