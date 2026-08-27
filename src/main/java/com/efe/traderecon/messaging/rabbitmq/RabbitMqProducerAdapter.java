package com.efe.traderecon.messaging.rabbitmq;

import com.efe.traderecon.messaging.spi.MessagingMessage;
import com.efe.traderecon.messaging.spi.MessagingProducer;

/**
 * Future RabbitMQ Producer Adapter Boundary.
 * Intended target: IKASAN-005
 *
 * Architecture:
 * MessagingProducer -> RabbitMqProducerAdapter -> Exchange / AMQP Channel -> Queue
 */
public class RabbitMqProducerAdapter<T> implements MessagingProducer<T> {

    @Override
    public void send(String destination, MessagingMessage<T> message) {
        throw new UnsupportedOperationException("RabbitMQ transport is scheduled for IKASAN-005. Use 'inmemory' provider for IKASAN-001.");
    }

    @Override
    public String getProviderName() {
        return "rabbitmq";
    }
}
