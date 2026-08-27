package com.efe.traderecon.messaging.redis;

import com.efe.traderecon.messaging.spi.MessagingConsumer;
import com.efe.traderecon.messaging.spi.MessagingProducer;
import com.efe.traderecon.messaging.spi.MessagingProvider;
import org.springframework.stereotype.Component;

@Component
public class RedisMessagingProvider implements MessagingProvider {

    @Override
    public String getName() {
        return "redis";
    }

    @Override
    public <T> MessagingProducer<T> createProducer() {
        return new RedisStreamsProducerAdapter<>();
    }

    @Override
    public <T> MessagingConsumer<T> createConsumer() {
        return new RedisStreamsConsumerAdapter<>();
    }

    @Override
    public boolean isAvailable() {
        return false; // Future target IKASAN-007
    }
}
