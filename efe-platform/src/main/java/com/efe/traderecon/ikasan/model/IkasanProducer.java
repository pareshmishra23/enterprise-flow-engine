package com.efe.traderecon.ikasan.model;

/**
 * Ikasan Producer Interface.
 * Terminal component / outbound endpoint of an Ikasan Flow.
 */
public interface IkasanProducer<EVENT> {
    String getName();
    void produce(EVENT event);
}
