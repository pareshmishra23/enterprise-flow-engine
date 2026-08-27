package com.efe.traderecon.messaging.kafka;

import com.efe.traderecon.messaging.spi.MessagingConsumer;
import com.efe.traderecon.messaging.spi.MessagingMessage;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Future Kafka Consumer Adapter Boundary.
 * Intended target: IKASAN-004
 *
 * Architecture:
 * Kafka Topic -> KafkaConsumer (org.apache.kafka.clients.consumer.KafkaConsumer) -> KafkaConsumerAdapter -> MessagingConsumer
 */
public class KafkaConsumerAdapter<T> implements MessagingConsumer<T> {

    @Override
    public Optional<MessagingMessage<T>> poll(String destination, long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException("Kafka transport is scheduled for IKASAN-004. Use 'inmemory' provider for IKASAN-001.");
    }

    @Override
    public void subscribe(String destination, Consumer<MessagingMessage<T>> handler) {
        throw new UnsupportedOperationException("Kafka transport is scheduled for IKASAN-004. Use 'inmemory' provider for IKASAN-001.");
    }

    @Override
    public void unsubscribe(String destination) {
    }

    @Override
    public String getProviderName() {
        return "kafka";
    }
}
