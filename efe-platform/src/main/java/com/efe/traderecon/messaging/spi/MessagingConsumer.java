package com.efe.traderecon.messaging.spi;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface MessagingConsumer<T> {
    Optional<MessagingMessage<T>> poll(String destination, long timeout, TimeUnit unit);
    void subscribe(String destination, Consumer<MessagingMessage<T>> handler);
    void unsubscribe(String destination);
    String getProviderName();
}
