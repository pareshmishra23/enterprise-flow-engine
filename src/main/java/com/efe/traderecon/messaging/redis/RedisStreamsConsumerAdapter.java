package com.efe.traderecon.messaging.redis;

import com.efe.traderecon.messaging.spi.MessagingConsumer;
import com.efe.traderecon.messaging.spi.MessagingMessage;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Future Redis Streams Consumer Adapter Boundary.
 * Intended target: IKASAN-007
 *
 * Architecture:
 * Redis Stream -> Redis XREADGROUP / XREAD -> RedisStreamsConsumerAdapter -> MessagingConsumer
 */
public class RedisStreamsConsumerAdapter<T> implements MessagingConsumer<T> {

    @Override
    public Optional<MessagingMessage<T>> poll(String destination, long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException("Redis Streams transport is scheduled for IKASAN-007. Use 'inmemory' provider for IKASAN-001.");
    }

    @Override
    public void subscribe(String destination, Consumer<MessagingMessage<T>> handler) {
        throw new UnsupportedOperationException("Redis Streams transport is scheduled for IKASAN-007. Use 'inmemory' provider for IKASAN-001.");
    }

    @Override
    public void unsubscribe(String destination) {
    }

    @Override
    public String getProviderName() {
        return "redis";
    }
}
