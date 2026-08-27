package com.efe.traderecon.messaging.spi;

public interface MessagingProducer<T> {
    void send(String destination, MessagingMessage<T> message);
    String getProviderName();
}
