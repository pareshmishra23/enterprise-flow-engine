package com.efe.traderecon.messaging.inmemory;

import com.efe.traderecon.messaging.spi.MessagingConsumer;
import com.efe.traderecon.messaging.spi.MessagingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class InMemoryMessagingConsumer<T> implements MessagingConsumer<T> {
    private static final Logger log = LoggerFactory.getLogger(InMemoryMessagingConsumer.class);

    private final InMemoryQueue queue;

    public InMemoryMessagingConsumer(InMemoryQueue queue) {
        this.queue = queue;
    }

    @Override
    public Optional<MessagingMessage<T>> poll(String destination, long timeout, TimeUnit unit) {
        return queue.poll(destination, timeout, unit);
    }

    @Override
    public void subscribe(String destination, Consumer<MessagingMessage<T>> handler) {
        log.info("InMemoryConsumer subscribing to [{}]", destination);
        queue.subscribe(destination, (Consumer) handler);
    }

    @Override
    public void unsubscribe(String destination) {
        log.info("InMemoryConsumer unsubscribing from [{}]", destination);
        queue.unsubscribe(destination);
    }

    @Override
    public String getProviderName() {
        return "inmemory";
    }
}
