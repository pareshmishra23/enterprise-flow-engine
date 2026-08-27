package com.efe.traderecon.messaging.rabbitmq;

import com.efe.traderecon.messaging.spi.MessagingConsumer;
import com.efe.traderecon.messaging.spi.MessagingMessage;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Future RabbitMQ Consumer Adapter Boundary.
 * Intended target: IKASAN-005
 *
 * Architecture:
 * Queue -> AMQP BasicConsumer -> RabbitMqConsumerAdapter -> MessagingConsumer
 */
public class RabbitMqConsumerAdapter<T> implements MessagingConsumer<T> {

    @Override
    public Optional<MessagingMessage<T>> poll(String destination, long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException("RabbitMQ transport is scheduled for IKASAN-005. Use 'inmemory' provider for IKASAN-001.");
    }

    @Override
    public void subscribe(String destination, Consumer<MessagingMessage<T>> handler) {
        throw new UnsupportedOperationException("RabbitMQ transport is scheduled for IKASAN-005. Use 'inmemory' provider for IKASAN-001.");
    }

    @Override
    public void unsubscribe(String destination) {
    }

    @Override
    public String getProviderName() {
        return "rabbitmq";
    }
}
