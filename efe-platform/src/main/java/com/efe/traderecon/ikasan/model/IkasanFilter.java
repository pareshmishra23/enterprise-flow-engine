package com.efe.traderecon.ikasan.model;

/**
 * Ikasan Filter Component Contract.
 * Evaluates whether an event should continue downstream.
 *
 * @param <E> Event type
 */
@FunctionalInterface
public interface IkasanFilter<E> {
    boolean accept(E event);

    default String getName() {
        return getClass().getSimpleName();
    }
}
