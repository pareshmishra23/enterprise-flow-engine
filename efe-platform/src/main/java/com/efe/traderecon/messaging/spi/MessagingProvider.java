package com.efe.traderecon.messaging.spi;

public interface MessagingProvider {
    String getName();
    <T> MessagingProducer<T> createProducer();
    <T> MessagingConsumer<T> createConsumer();
    boolean isAvailable();
}
