package com.efe.traderecon.messaging.inmemory;

import com.efe.traderecon.messaging.spi.MessagingConsumer;
import com.efe.traderecon.messaging.spi.MessagingProducer;
import com.efe.traderecon.messaging.spi.MessagingProvider;
import org.springframework.stereotype.Component;

@Component
public class InMemoryMessagingProvider implements MessagingProvider {

    private final InMemoryQueue queue;

    public InMemoryMessagingProvider(InMemoryQueue queue) {
        this.queue = queue;
    }

    @Override
    public String getName() {
        return "inmemory";
    }

    @Override
    public <T> MessagingProducer<T> createProducer() {
        return new InMemoryMessagingProducer<>(queue);
    }

    @Override
    public <T> MessagingConsumer<T> createConsumer() {
        return new InMemoryMessagingConsumer<>(queue);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
