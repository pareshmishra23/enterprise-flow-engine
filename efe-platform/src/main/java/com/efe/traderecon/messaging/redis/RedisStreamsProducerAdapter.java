package com.efe.traderecon.messaging.redis;

import com.efe.traderecon.messaging.spi.MessagingMessage;
import com.efe.traderecon.messaging.spi.MessagingProducer;

/**
 * Future Redis Streams Producer Adapter Boundary.
 * Intended target: IKASAN-007
 *
 * Architecture:
 * MessagingProducer -> RedisStreamsProducerAdapter -> Redis XADD -> Redis Stream
 */
public class RedisStreamsProducerAdapter<T> implements MessagingProducer<T> {

    @Override
    public void send(String destination, MessagingMessage<T> message) {
        throw new UnsupportedOperationException("Redis Streams transport is scheduled for IKASAN-007. Use 'inmemory' provider for IKASAN-001.");
    }

    @Override
    public String getProviderName() {
        return "redis";
    }
}
