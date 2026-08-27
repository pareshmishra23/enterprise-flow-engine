package com.efe.traderecon.messaging.amq;

import com.efe.traderecon.messaging.spi.MessagingConsumer;
import com.efe.traderecon.messaging.spi.MessagingMessage;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Future AMQ / JMS Consumer Adapter Boundary.
 * Intended target: IKASAN-006
 *
 * Architecture:
 * ActiveMQ / Artemis Broker -> JMS MessageConsumer -> JmsConsumerAdapter -> MessagingConsumer
 */
public class JmsConsumerAdapter<T> implements MessagingConsumer<T> {

    @Override
    public Optional<MessagingMessage<T>> poll(String destination, long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException("AMQ/JMS transport is scheduled for IKASAN-006. Use 'inmemory' provider for IKASAN-001.");
    }

    @Override
    public void subscribe(String destination, Consumer<MessagingMessage<T>> handler) {
        throw new UnsupportedOperationException("AMQ/JMS transport is scheduled for IKASAN-006. Use 'inmemory' provider for IKASAN-001.");
    }

    @Override
    public void unsubscribe(String destination) {
    }

    @Override
    public String getProviderName() {
        return "amq";
    }
}
