package com.efe.traderecon.ikasan.model;

/**
 * Ikasan Processor Component Contract.
 * Responsible for core domain computation or business transformation.
 *
 * @param <S> Source input payload type
 * @param <T> Target processed result type
 */
@FunctionalInterface
public interface IkasanProcessor<S, T> {
    T process(S payload);

    default String getName() {
        return getClass().getSimpleName();
    }
}
