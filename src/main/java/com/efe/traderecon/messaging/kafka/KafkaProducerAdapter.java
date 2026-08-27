package com.efe.traderecon.messaging.kafka;

import com.efe.traderecon.messaging.spi.MessagingMessage;
import com.efe.traderecon.messaging.spi.MessagingProducer;

/**
 * Future Kafka Producer Adapter Boundary.
 * Intended target: IKASAN-004
 *
 * Architecture:
 * MessagingProducer -> KafkaProducerAdapter -> KafkaProducer (org.apache.kafka.clients.producer.KafkaProducer) -> Kafka Topic
 */
public class KafkaProducerAdapter<T> implements MessagingProducer<T> {

    @Override
    public void send(String destination, MessagingMessage<T> message) {
        throw new UnsupportedOperationException("Kafka transport is scheduled for IKASAN-004. Use 'inmemory' provider for IKASAN-001.");
    }

    @Override
    public String getProviderName() {
        return "kafka";
    }
}
