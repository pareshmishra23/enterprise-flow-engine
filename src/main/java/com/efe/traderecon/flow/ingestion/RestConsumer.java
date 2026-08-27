package com.efe.traderecon.flow.ingestion;

import com.efe.traderecon.ikasan.model.IkasanConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class RestConsumer implements IkasanConsumer<Object> {
    private static final Logger log = LoggerFactory.getLogger(RestConsumer.class);

    private volatile boolean running = false;
    private java.util.function.Consumer<Object> listener;

    @Override
    public String getName() {
        return "rest-ingestion-consumer";
    }

    @Override
    public void start() {
        this.running = true;
        log.info("RestConsumer [{}] started and ready to receive HTTP payloads", getName());
    }

    @Override
    public void stop() {
        this.running = false;
        log.info("RestConsumer [{}] stopped", getName());
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void setListener(Consumer<Object> listener) {
        this.listener = listener;
    }

    public Object onEvent(Object rawPayload) {
        if (!running) {
            throw new IllegalStateException("RestConsumer is not running");
        }
        if (listener != null) {
            listener.accept(rawPayload);
            return rawPayload;
        }
        throw new IllegalStateException("No flow listener registered on RestConsumer");
    }
}
