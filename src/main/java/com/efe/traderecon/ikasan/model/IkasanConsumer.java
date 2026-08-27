package com.efe.traderecon.ikasan.model;

import java.util.function.Consumer;

/**
 * Ikasan Consumer Interface.
 * Entry point for an Ikasan Flow. A Flow has exactly one Consumer.
 */
public interface IkasanConsumer<EVENT> {
    String getName();
    void start();
    void stop();
    boolean isRunning();
    void setListener(java.util.function.Consumer<EVENT> listener);
}
