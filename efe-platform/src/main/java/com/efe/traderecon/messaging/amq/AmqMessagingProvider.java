package com.efe.traderecon.messaging.amq;

import com.efe.traderecon.messaging.spi.MessagingConsumer;
import com.efe.traderecon.messaging.spi.MessagingProducer;
import com.efe.traderecon.messaging.spi.MessagingProvider;
import org.springframework.stereotype.Component;

@Component
public class AmqMessagingProvider implements MessagingProvider {

    @Override
    public String getName() {
        return "amq";
    }

    @Override
    public <T> MessagingProducer<T> createProducer() {
        return new JmsProducerAdapter<>();
    }

    @Override
    public <T> MessagingConsumer<T> createConsumer() {
        return new JmsConsumerAdapter<>();
    }

    @Override
    public boolean isAvailable() {
        return false; // Future target IKASAN-006
    }
}
