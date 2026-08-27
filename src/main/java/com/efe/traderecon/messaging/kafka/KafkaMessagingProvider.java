package com.efe.traderecon.messaging.kafka;

import com.efe.traderecon.messaging.spi.MessagingConsumer;
import com.efe.traderecon.messaging.spi.MessagingProducer;
import com.efe.traderecon.messaging.spi.MessagingProvider;
import org.springframework.stereotype.Component;

@Component
public class KafkaMessagingProvider implements MessagingProvider {

    @Override
    public String getName() {
        return "kafka";
    }

    @Override
    public <T> MessagingProducer<T> createProducer() {
        return new KafkaProducerAdapter<>();
    }

    @Override
    public <T> MessagingConsumer<T> createConsumer() {
        return new KafkaConsumerAdapter<>();
    }

    @Override
    public boolean isAvailable() {
        return false; // Future target IKASAN-004
    }
}
