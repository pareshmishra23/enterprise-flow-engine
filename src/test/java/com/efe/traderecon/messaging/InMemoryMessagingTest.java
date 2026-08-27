package com.efe.traderecon.messaging;

import com.efe.traderecon.messaging.inmemory.InMemoryMessagingConsumer;
import com.efe.traderecon.messaging.inmemory.InMemoryMessagingProducer;
import com.efe.traderecon.messaging.inmemory.InMemoryMessagingProvider;
import com.efe.traderecon.messaging.inmemory.InMemoryQueue;
import com.efe.traderecon.messaging.spi.MessagingMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryMessagingTest {

    private InMemoryQueue queue;
    private InMemoryMessagingProvider provider;

    @BeforeEach
    void setUp() {
        queue = new InMemoryQueue(50);
        provider = new InMemoryMessagingProvider(queue);
    }

    @Test
    @DisplayName("Should send and poll messages synchronously via In-Memory provider")
    void shouldSendAndPollMessages() {
        InMemoryMessagingProducer<String> producer = (InMemoryMessagingProducer<String>) provider.<String>createProducer();
        InMemoryMessagingConsumer<String> consumer = (InMemoryMessagingConsumer<String>) provider.<String>createConsumer();

        MessagingMessage<String> msg = new MessagingMessage<>("test-payload");
        producer.send("test.topic", msg);

        assertThat(queue.getQueueSize("test.topic")).isEqualTo(1);

        Optional<MessagingMessage<String>> polled = consumer.poll("test.topic", 100, TimeUnit.MILLISECONDS);
        assertThat(polled).isPresent();
        assertThat(polled.get().getPayload()).isEqualTo("test-payload");
        assertThat(polled.get().getMessageId()).isEqualTo(msg.getMessageId());
    }

    @Test
    @DisplayName("Should deliver messages to active asynchronous subscribers")
    void shouldDeliverToSubscribers() throws InterruptedException {
        InMemoryMessagingProducer<String> producer = (InMemoryMessagingProducer<String>) provider.<String>createProducer();
        InMemoryMessagingConsumer<String> consumer = (InMemoryMessagingConsumer<String>) provider.<String>createConsumer();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> received = new AtomicReference<>();

        consumer.subscribe("async.topic", message -> {
            received.set(message.getPayload());
            latch.countDown();
        });

        producer.send("async.topic", new MessagingMessage<>("async-data"));

        boolean delivered = latch.await(2, TimeUnit.SECONDS);
        assertThat(delivered).isTrue();
        assertThat(received.get()).isEqualTo("async-data");
    }
}
