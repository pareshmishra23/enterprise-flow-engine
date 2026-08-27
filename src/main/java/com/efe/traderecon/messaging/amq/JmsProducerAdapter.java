package com.efe.traderecon.messaging.amq;

import com.efe.traderecon.messaging.spi.MessagingMessage;
import com.efe.traderecon.messaging.spi.MessagingProducer;

/**
 * Future AMQ / JMS Producer Adapter Boundary.
 * Intended target: IKASAN-006
 *
 * Architecture:
 * MessagingProducer -> JmsProducerAdapter -> JMS Connection/Session (javax.jms / jakarta.jms) -> ActiveMQ / Artemis Broker
 */
public class JmsProducerAdapter<T> implements MessagingProducer<T> {

    @Override
    public void send(String destination, MessagingMessage<T> message) {
        throw new UnsupportedOperationException("AMQ/JMS transport is scheduled for IKASAN-006. Use 'inmemory' provider for IKASAN-001.");
    }

    @Override
    public String getProviderName() {
        return "amq";
    }
}
