package com.efe.traderecon.messaging.rabbitmq;

import com.efe.traderecon.messaging.spi.MessagingConsumer;
import com.efe.traderecon.messaging.spi.MessagingProducer;
import com.efe.traderecon.messaging.spi.MessagingProvider;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqMessagingProvider implements MessagingProvider {

    @Override
    public String getName() {
        return "rabbitmq";
    }

    @Override
    public <T> MessagingProducer<T> createProducer() {
        return new RabbitMqProducerAdapter<>();
    }

    @Override
    public <T> MessagingConsumer<T> createConsumer() {
        return new RabbitMqConsumerAdapter<>();
    }

    @Override
    public boolean isAvailable() {
        return false; // Future target IKASAN-005
    }
}
