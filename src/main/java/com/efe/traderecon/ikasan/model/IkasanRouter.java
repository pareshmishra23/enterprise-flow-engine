package com.efe.traderecon.ikasan.model;

/**
 * Ikasan Router Component Contract.
 * Evaluates an event and returns the target route identifier.
 *
 * @param <E> Event type being evaluated for routing
 */
@FunctionalInterface
public interface IkasanRouter<E> {
    String route(E event);

    default String getName() {
        return getClass().getSimpleName();
    }
}
