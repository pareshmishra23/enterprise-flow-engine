package com.efe.traderecon.ikasan.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Ikasan Flow Execution Model.
 * Represents a real Ikasan directed execution graph:
 * - Exactly one ingress Consumer
 * - Sequence of intermediate components (Converter, Translator, Processor, Broker, Splitter, Filter, Router)
 * - Terminal Producer(s) / Route-specific Producers
 */
public class IkasanFlow {
    private static final Logger log = LoggerFactory.getLogger(IkasanFlow.class);

    private final String name;
    private final String moduleName;
    private final IkasanConsumer<?> consumer;
    private final List<FlowElement> elements;
    private final IkasanProducer<?> defaultProducer;
    private final List<Consumer<Object>> wiretaps = new CopyOnWriteArrayList<>();

    private volatile FlowState state = FlowState.STOPPED;
    private long totalEventsProcessed = 0;
    private long totalEventsFailed = 0;

    public IkasanFlow(String name, String moduleName, IkasanConsumer<?> consumer, List<FlowElement> elements, IkasanProducer<?> defaultProducer) {
        this.name = name;
        this.moduleName = moduleName;
        this.consumer = consumer;
        this.elements = elements != null ? new ArrayList<>(elements) : new ArrayList<>();
        this.defaultProducer = defaultProducer;
        wirePipeline();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void wirePipeline() {
        if (consumer != null) {
            ((IkasanConsumer) consumer).setListener(this::onConsumerEvent);
        }
    }

    public synchronized void start() {
        if (state == FlowState.RUNNING || state == FlowState.STARTING) {
            return;
        }
        state = FlowState.STARTING;
        log.info("Starting Ikasan Flow [{}::{}]", moduleName, name);
        try {
            if (consumer != null) {
                consumer.start();
            }
            state = FlowState.RUNNING;
            log.info("Ikasan Flow [{}::{}] is now RUNNING", moduleName, name);
        } catch (Exception e) {
            state = FlowState.ERROR;
            log.error("Failed to start Ikasan Flow [{}::{}]", moduleName, name, e);
            throw new RuntimeException("Failed to start flow " + name, e);
        }
    }

    public synchronized void stop() {
        if (state == FlowState.STOPPED || state == FlowState.STOPPING) {
            return;
        }
        state = FlowState.STOPPING;
        log.info("Stopping Ikasan Flow [{}::{}]", moduleName, name);
        try {
            if (consumer != null) {
                consumer.stop();
            }
            state = FlowState.STOPPED;
            log.info("Ikasan Flow [{}::{}] is now STOPPED", moduleName, name);
        } catch (Exception e) {
            state = FlowState.ERROR;
            log.error("Failed to stop Ikasan Flow [{}::{}]", moduleName, name, e);
            throw new RuntimeException("Failed to stop flow " + name, e);
        }
    }

    public synchronized void pause() {
        if (state == FlowState.RUNNING) {
            state = FlowState.PAUSED;
            log.info("Ikasan Flow [{}::{}] is now PAUSED", moduleName, name);
        }
    }

    public synchronized void resume() {
        if (state == FlowState.PAUSED) {
            state = FlowState.RUNNING;
            log.info("Ikasan Flow [{}::{}] is now RESUMED", moduleName, name);
        }
    }

    public void addWiretap(Consumer<Object> wiretap) {
        if (wiretap != null) {
            this.wiretaps.add(wiretap);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object onConsumerEvent(Object initialEvent) {
        if (state != FlowState.RUNNING) {
            log.warn("Ikasan Flow [{}::{}] received event while in state [{}], ignoring or rejecting", moduleName, name, state);
            return null;
        }

        long start = System.currentTimeMillis();
        Object currentPayload = initialEvent;

        // Wiretap initial intake
        notifyWiretaps(currentPayload);

        try {
            boolean routeTerminated = false;

            for (FlowElement element : elements) {
                if (routeTerminated || currentPayload == null) {
                    break;
                }

                long elStart = System.currentTimeMillis();
                try {
                    switch (element.getType()) {
                        case CONVERTER -> {
                            IkasanConverter converter = (IkasanConverter) element.getComponent();
                            currentPayload = converter.convert(currentPayload);
                        }
                        case TRANSLATOR -> {
                            IkasanTranslator translator = (IkasanTranslator) element.getComponent();
                            currentPayload = translator.translate(currentPayload);
                        }
                        case PROCESSOR -> {
                            IkasanProcessor processor = (IkasanProcessor) element.getComponent();
                            currentPayload = processor.process(currentPayload);
                        }
                        case BROKER -> {
                            IkasanBroker broker = (IkasanBroker) element.getComponent();
                            currentPayload = broker.invoke(currentPayload);
                        }
                        case SPLITTER -> {
                            IkasanSplitter splitter = (IkasanSplitter) element.getComponent();
                            currentPayload = splitter.split(currentPayload);
                        }
                        case FILTER -> {
                            IkasanFilter filter = (IkasanFilter) element.getComponent();
                            boolean accepted = filter.accept(currentPayload);
                            if (!accepted) {
                                log.debug("Event filtered out by filter component [{}]", element.getName());
                                currentPayload = null;
                                routeTerminated = true;
                            }
                        }
                        case ROUTER -> {
                            IkasanRouter router = (IkasanRouter) element.getComponent();
                            String targetRoute = router.route(currentPayload);
                            log.debug("Router [{}] selected target route: [{}]", element.getName(), targetRoute);

                            IkasanProducer targetProducer = element.getRoute(targetRoute);
                            if (targetProducer != null) {
                                targetProducer.produce(currentPayload);
                                routeTerminated = true; // Router handled terminal dispatch
                            } else {
                                log.warn("No producer registered for route [{}] on router [{}]", targetRoute, element.getName());
                            }
                        }
                        default -> {
                        }
                    }
                    element.recordExecution(System.currentTimeMillis() - elStart, true);
                    notifyWiretaps(currentPayload);
                } catch (Exception ex) {
                    element.recordExecution(System.currentTimeMillis() - elStart, false);
                    throw ex;
                }
            }

            // If not routed by a router and default producer exists, produce to default producer
            if (!routeTerminated && defaultProducer != null && currentPayload != null) {
                if (currentPayload instanceof List<?> list && !(defaultProducer instanceof BatchListProducer)) {
                    for (Object item : list) {
                        ((IkasanProducer) defaultProducer).produce(item);
                    }
                } else {
                    ((IkasanProducer) defaultProducer).produce(currentPayload);
                }
            }

            synchronized (this) {
                totalEventsProcessed++;
            }
            long duration = System.currentTimeMillis() - start;
            log.debug("Ikasan Flow [{}::{}] completed event execution in {}ms", moduleName, name, duration);
            return currentPayload;
        } catch (Exception ex) {
            synchronized (this) {
                totalEventsFailed++;
            }
            log.error("Ikasan Flow [{}::{}] failed processing event: {}", moduleName, name, ex.getMessage(), ex);
            throw new RuntimeException("Flow execution failed: " + ex.getMessage(), ex);
        }
    }

    private void notifyWiretaps(Object event) {
        if (event != null && !wiretaps.isEmpty()) {
            for (Consumer<Object> wiretap : wiretaps) {
                try {
                    wiretap.accept(event);
                } catch (Exception e) {
                    log.warn("Wiretap listener error in flow [{}]: {}", name, e.getMessage());
                }
            }
        }
    }

    public String getName() { return name; }
    public String getModuleName() { return moduleName; }
    public IkasanConsumer<?> getConsumer() { return consumer; }
    public List<FlowElement> getElements() { return Collections.unmodifiableList(elements); }
    public IkasanProducer<?> getProducer() { return defaultProducer; }
    public FlowState getState() { return state; }
    public synchronized long getTotalEventsProcessed() { return totalEventsProcessed; }
    public synchronized long getTotalEventsFailed() { return totalEventsFailed; }

    public interface BatchListProducer {}
}
