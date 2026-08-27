package com.efe.traderecon.ikasan.builder;

import com.efe.traderecon.ikasan.model.*;
import com.efe.traderecon.reliability.ReliabilityService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Ikasan Flow Builder DSL.
 * Standard builder for constructing native Ikasan Flow component chains.
 */
public class FlowBuilder {
    private final String name;
    private final String moduleName;
    private IkasanConsumer<?> consumer;
    private final List<FlowElement> elements = new ArrayList<>();
    private IkasanProducer<?> defaultProducer;
    private ReliabilityService reliability;
    private Function<Object, String> eventIdExtractor;

    public FlowBuilder(String name, String moduleName) {
        this.name = name;
        this.moduleName = moduleName;
    }

    public FlowBuilder consumer(String name, IkasanConsumer<?> consumer) {
        this.consumer = consumer;
        return this;
    }

    public FlowBuilder converter(String name, IkasanConverter<?, ?> converter) {
        this.elements.add(new FlowElement(name, ComponentType.CONVERTER, converter));
        return this;
    }

    public FlowBuilder translator(String name, IkasanTranslator<?> translator) {
        this.elements.add(new FlowElement(name, ComponentType.TRANSLATOR, translator));
        return this;
    }

    public FlowBuilder processor(String name, IkasanProcessor<?, ?> processor) {
        this.elements.add(new FlowElement(name, ComponentType.PROCESSOR, processor));
        return this;
    }

    public FlowBuilder broker(String name, IkasanBroker<?, ?> broker) {
        this.elements.add(new FlowElement(name, ComponentType.BROKER, broker));
        return this;
    }

    public FlowBuilder splitter(String name, IkasanSplitter<?, ?> splitter) {
        this.elements.add(new FlowElement(name, ComponentType.SPLITTER, splitter));
        return this;
    }

    public FlowBuilder filter(String name, IkasanFilter<?> filter) {
        this.elements.add(new FlowElement(name, ComponentType.FILTER, filter));
        return this;
    }

    public FlowBuilder router(String name, IkasanRouter<?> router, Consumer<RouteConfiguration> routeConfig) {
        FlowElement routerElement = new FlowElement(name, ComponentType.ROUTER, router);
        RouteConfiguration config = new RouteConfiguration(routerElement);
        if (routeConfig != null) {
            routeConfig.accept(config);
        }
        this.elements.add(routerElement);
        return this;
    }

    public FlowBuilder producer(String name, IkasanProducer<?> producer) {
        this.defaultProducer = producer;
        return this;
    }

    /**
     * Enable flow-level reliability handling (retry with backoff, then DLQ) for
     * this flow. When enabled, the whole event pipeline is executed through the
     * provided {@link ReliabilityService}; on failure it retries per configuration
     * and, once exhausted or for permanently classified failures, routes the event
     * to the dead letter queue.
     *
     * @param service            the reliability service to use
     * @param eventIdExtractor   extracts a stable event id from an inbound event for
     *                           audit/DLQ correlation; may be null to use toString()
     */
    public FlowBuilder reliable(ReliabilityService service, Function<Object, String> eventIdExtractor) {
        this.reliability = service;
        this.eventIdExtractor = eventIdExtractor;
        return this;
    }

    public static class RouteConfiguration {
        private final FlowElement routerElement;

        public RouteConfiguration(FlowElement routerElement) {
            this.routerElement = routerElement;
        }

        public RouteConfiguration when(String routeName, IkasanProducer<?> targetProducer) {
            this.routerElement.addRoute(routeName, targetProducer);
            return this;
        }
    }

    public IkasanFlow build() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Flow name must not be empty");
        }
        if (consumer == null) {
            throw new IllegalStateException("Flow [" + name + "] must have exactly one Consumer configured");
        }
        return new IkasanFlow(name, moduleName, consumer, elements, defaultProducer, reliability, eventIdExtractor);
    }
}
