package com.efe.traderecon.messaging.inmemory;

import com.efe.traderecon.messaging.spi.MessagingMessage;
import com.efe.traderecon.messaging.spi.MessagingProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryMessagingProducer<T> implements MessagingProducer<T> {
    private static final Logger log = LoggerFactory.getLogger(InMemoryMessagingProducer.class);

    private final InMemoryQueue queue;

    public InMemoryMessagingProducer(InMemoryQueue queue) {
        this.queue = queue;
    }

    @Override
    public void send(String destination, MessagingMessage<T> message) {
        if (message == null) {
            throw new IllegalArgumentException("Cannot send null message");
        }
        boolean success = queue.offer(destination, message);
        if (!success) {
            throw new IllegalStateException("Failed to deliver message to destination [" + destination + "] - queue full");
        }
        log.debug("InMemoryProducer sent message [{}] to destination [{}]", message.getMessageId(), destination);
    }

    @Override
    public String getProviderName() {
        return "inmemory";
    }
}
