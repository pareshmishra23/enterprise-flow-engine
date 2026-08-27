package com.efe.traderecon.messaging.inmemory;

import com.efe.traderecon.messaging.spi.MessagingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Component
public class InMemoryQueue {
    private static final Logger log = LoggerFactory.getLogger(InMemoryQueue.class);

    private final int defaultCapacity;
    private final ConcurrentHashMap<String, BlockingQueue<MessagingMessage<?>>> queues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Consumer<MessagingMessage<?>>>> subscribers = new ConcurrentHashMap<>();
    private final ExecutorService dispatchExecutor = Executors.newCachedThreadPool();

    public InMemoryQueue() {
        this(1000);
    }

    public InMemoryQueue(int defaultCapacity) {
        this.defaultCapacity = defaultCapacity > 0 ? defaultCapacity : 1000;
    }

    private BlockingQueue<MessagingMessage<?>> getOrCreateQueue(String destination) {
        return queues.computeIfAbsent(destination, d -> new LinkedBlockingQueue<>(defaultCapacity));
    }

    public <T> boolean offer(String destination, MessagingMessage<T> message) {
        BlockingQueue<MessagingMessage<?>> queue = getOrCreateQueue(destination);
        boolean offered = queue.offer(message);
        if (offered) {
            log.debug("Enqueued message [{}] on destination [{}] (queue size: {})",
                    message.getMessageId(), destination, queue.size());
            notifySubscribers(destination, message);
        } else {
            log.warn("Failed to enqueue message [{}] on destination [{}] — capacity reached ({})",
                    message.getMessageId(), destination, defaultCapacity);
        }
        return offered;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<MessagingMessage<T>> poll(String destination, long timeout, TimeUnit unit) {
        BlockingQueue<MessagingMessage<?>> queue = getOrCreateQueue(destination);
        try {
            MessagingMessage<?> item = queue.poll(timeout, unit);
            if (item != null) {
                return Optional.of((MessagingMessage<T>) item);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private <T> void notifySubscribers(String destination, MessagingMessage<T> message) {
        CopyOnWriteArrayList<Consumer<MessagingMessage<?>>> list = subscribers.get(destination);
        if (list != null && !list.isEmpty()) {
            for (Consumer<MessagingMessage<?>> consumer : list) {
                dispatchExecutor.submit(() -> {
                    try {
                        consumer.accept(message);
                    } catch (Exception e) {
                        log.error("Subscriber notification failed on destination [{}] for message [{}]",
                                destination, message.getMessageId(), e);
                    }
                });
            }
        }
    }

    public void subscribe(String destination, Consumer<MessagingMessage<?>> consumer) {
        subscribers.computeIfAbsent(destination, d -> new CopyOnWriteArrayList<>()).add(consumer);
        log.info("Registered subscriber on in-memory destination [{}]", destination);
    }

    public void unsubscribe(String destination) {
        subscribers.remove(destination);
        log.info("Unregistered subscribers on in-memory destination [{}]", destination);
    }

    public int getQueueSize(String destination) {
        BlockingQueue<MessagingMessage<?>> queue = queues.get(destination);
        return queue != null ? queue.size() : 0;
    }

    public void clear(String destination) {
        BlockingQueue<MessagingMessage<?>> queue = queues.get(destination);
        if (queue != null) {
            queue.clear();
        }
    }

    public int getTotalQueueSize() {
        return queues.values().stream().mapToInt(BlockingQueue::size).sum();
    }

    public int getDefaultCapacity() {
        return defaultCapacity;
    }

    public void clearAll() {
        queues.values().forEach(BlockingQueue::clear);
    }
}
