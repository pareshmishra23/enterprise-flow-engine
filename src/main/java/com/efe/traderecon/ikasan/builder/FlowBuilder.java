package com.efe.traderecon.ikasan.builder;

import com.efe.traderecon.ikasan.model.*;

import java.util.ArrayList;
import java.util.List;

public class FlowBuilder {
    private final String name;
    private final String moduleName;
    private IkasanConsumer<?> consumer;
    private final List<FlowElement> elements = new ArrayList<>();
    private IkasanProducer<?> producer;

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

    public FlowBuilder broker(String name, IkasanBroker<?, ?> broker) {
        this.elements.add(new FlowElement(name, ComponentType.BROKER, broker));
        return this;
    }

    public FlowBuilder splitter(String name, IkasanSplitter<?, ?> splitter) {
        this.elements.add(new FlowElement(name, ComponentType.SPLITTER, splitter));
        return this;
    }

    public FlowBuilder producer(String name, IkasanProducer<?> producer) {
        this.producer = producer;
        return this;
    }

    public IkasanFlow build() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Flow name must not be empty");
        }
        if (consumer == null) {
            throw new IllegalStateException("Flow [" + name + "] must have a single Consumer configured");
        }
        return new IkasanFlow(name, moduleName, consumer, elements, producer);
    }
}
