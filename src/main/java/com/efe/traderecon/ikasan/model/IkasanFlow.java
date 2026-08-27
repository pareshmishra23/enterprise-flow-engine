package com.efe.traderecon.ikasan.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IkasanFlow {
    private static final Logger log = LoggerFactory.getLogger(IkasanFlow.class);

    private final String name;
    private final String moduleName;
    private final IkasanConsumer<?> consumer;
    private final List<FlowElement> elements;
    private final IkasanProducer<?> producer;
    private volatile FlowState state = FlowState.STOPPED;
    private long totalEventsProcessed = 0;
    private long totalEventsFailed = 0;

    public IkasanFlow(String name, String moduleName, IkasanConsumer<?> consumer, List<FlowElement> elements, IkasanProducer<?> producer) {
        this.name = name;
        this.moduleName = moduleName;
        this.consumer = consumer;
        this.elements = elements != null ? new ArrayList<>(elements) : new ArrayList<>();
        this.producer = producer;
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
        log.info("Starting Flow [{}::{}]", moduleName, name);
        try {
            if (consumer != null) {
                consumer.start();
            }
            state = FlowState.RUNNING;
            log.info("Flow [{}::{}] is now RUNNING", moduleName, name);
        } catch (Exception e) {
            state = FlowState.ERROR;
            log.error("Failed to start Flow [{}::{}]", moduleName, name, e);
            throw new RuntimeException("Failed to start flow " + name, e);
        }
    }

    public synchronized void stop() {
        if (state == FlowState.STOPPED || state == FlowState.STOPPING) {
            return;
        }
        state = FlowState.STOPPING;
        log.info("Stopping Flow [{}::{}]", moduleName, name);
        try {
            if (consumer != null) {
                consumer.stop();
            }
            state = FlowState.STOPPED;
            log.info("Flow [{}::{}] is now STOPPED", moduleName, name);
        } catch (Exception e) {
            state = FlowState.ERROR;
            log.error("Failed to stop Flow [{}::{}]", moduleName, name, e);
            throw new RuntimeException("Failed to stop flow " + name, e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object onConsumerEvent(Object initialEvent) {
        if (state != FlowState.RUNNING) {
            log.warn("Flow [{}::{}] received event while in state [{}], ignoring or failing", moduleName, name, state);
            return null;
        }

        long start = System.currentTimeMillis();
        Object currentPayload = initialEvent;

        try {
            for (FlowElement element : elements) {
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
                        case BROKER -> {
                            IkasanBroker broker = (IkasanBroker) element.getComponent();
                            currentPayload = broker.invoke(currentPayload);
                        }
                        case SPLITTER -> {
                            IkasanSplitter splitter = (IkasanSplitter) element.getComponent();
                            List items = splitter.split(currentPayload);
                            currentPayload = items;
                        }
                        default -> {
                        }
                    }
                    element.recordExecution(System.currentTimeMillis() - elStart, true);
                } catch (Exception ex) {
                    element.recordExecution(System.currentTimeMillis() - elStart, false);
                    throw ex;
                }
            }

            if (producer != null && currentPayload != null) {
                long pStart = System.currentTimeMillis();
                if (currentPayload instanceof List<?> list && !(producer instanceof BatchListProducer)) {
                    for (Object item : list) {
                        ((IkasanProducer) producer).produce(item);
                    }
                } else {
                    ((IkasanProducer) producer).produce(currentPayload);
                }
            }

            synchronized (this) {
                totalEventsProcessed++;
            }
            long duration = System.currentTimeMillis() - start;
            log.debug("Flow [{}::{}] executed event in {}ms", moduleName, name, duration);
            return currentPayload;
        } catch (Exception ex) {
            synchronized (this) {
                totalEventsFailed++;
            }
            log.error("Flow [{}::{}] failed processing event: {}", moduleName, name, ex.getMessage(), ex);
            throw new RuntimeException("Flow execution failed: " + ex.getMessage(), ex);
        }
    }

    public String getName() {
        return name;
    }

    public String getModuleName() {
        return moduleName;
    }

    public IkasanConsumer<?> getConsumer() {
        return consumer;
    }

    public List<FlowElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public IkasanProducer<?> getProducer() {
        return producer;
    }

    public FlowState getState() {
        return state;
    }

    public synchronized long getTotalEventsProcessed() {
        return totalEventsProcessed;
    }

    public synchronized long getTotalEventsFailed() {
        return totalEventsFailed;
    }

    public interface BatchListProducer {}
}
