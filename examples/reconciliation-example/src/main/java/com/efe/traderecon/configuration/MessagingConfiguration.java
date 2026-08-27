package com.efe.traderecon.configuration;

import com.efe.traderecon.domain.Task;
import com.efe.traderecon.messaging.inmemory.InMemoryQueue;
import com.efe.traderecon.messaging.spi.MessagingBrokerFactory;
import com.efe.traderecon.messaging.spi.MessagingConsumer;
import com.efe.traderecon.messaging.spi.MessagingProducer;
import com.efe.traderecon.messaging.spi.MessagingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfiguration {
    private static final Logger log = LoggerFactory.getLogger(MessagingConfiguration.class);

    @Bean
    public InMemoryQueue inMemoryQueue(@Value("${messaging.queue.capacity:1000}") int capacity) {
        log.info("Configured InMemoryQueue with capacity: {}", capacity);
        return new InMemoryQueue(capacity);
    }

    @Bean
    public MessagingProducer<Task> taskMessagingProducer(
            MessagingBrokerFactory factory,
            @Value("${messaging.provider:inmemory}") String providerName) {
        MessagingProvider provider = factory.getProvider(providerName);
        return provider.createProducer();
    }

    @Bean
    public MessagingConsumer<Task> taskMessagingConsumer(
            MessagingBrokerFactory factory,
            @Value("${messaging.provider:inmemory}") String providerName) {
        MessagingProvider provider = factory.getProvider(providerName);
        return provider.createConsumer();
    }
}
