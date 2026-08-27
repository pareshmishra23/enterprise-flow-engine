package com.efe.traderecon.ikasan.model;

/**
 * Ikasan Broker Interface.
 * Executes request/response interactions or side-effects (e.g. database persistence, external service call).
 */
public interface IkasanBroker<SOURCE, TARGET> {
    String getName();
    TARGET invoke(SOURCE source);
}
