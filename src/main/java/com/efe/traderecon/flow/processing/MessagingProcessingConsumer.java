package com.efe.traderecon.flow.processing;

import com.efe.traderecon.domain.Task;
import com.efe.traderecon.ikasan.model.IkasanConsumer;
import com.efe.traderecon.messaging.spi.MessagingConsumer;
import com.efe.traderecon.messaging.spi.MessagingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class MessagingProcessingConsumer implements IkasanConsumer<MessagingMessage<Task>> {
    private static final Logger log = LoggerFactory.getLogger(MessagingProcessingConsumer.class);
    public static final String DESTINATION = "trade.recon.tasks";

    private final MessagingConsumer<Task> messagingConsumer;
    private volatile boolean running = false;
    private Consumer<MessagingMessage<Task>> listener;

    public MessagingProcessingConsumer(MessagingConsumer<Task> messagingConsumer) {
        this.messagingConsumer = messagingConsumer;
    }

    @Override
    public String getName() {
        return "messaging-processing-consumer";
    }

    @Override
    public synchronized void start() {
        if (running) return;
        this.running = true;
        log.info("MessagingProcessingConsumer subscribing to destination [{}]", DESTINATION);
        messagingConsumer.subscribe(DESTINATION, msg -> {
            if (running && listener != null) {
                listener.accept(msg);
            }
        });
        log.info("MessagingProcessingConsumer started on destination [{}]", DESTINATION);
    }

    @Override
    public synchronized void stop() {
        if (!running) return;
        this.running = false;
        messagingConsumer.unsubscribe(DESTINATION);
        log.info("MessagingProcessingConsumer stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void setListener(Consumer<MessagingMessage<Task>> listener) {
        this.listener = listener;
    }
}
