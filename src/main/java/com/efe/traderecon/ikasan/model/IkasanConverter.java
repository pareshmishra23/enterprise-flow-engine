package com.efe.traderecon.ikasan.model;

/**
 * Ikasan Converter Interface.
 * Converts one object type to another (e.g. JSON to DTO).
 */
public interface IkasanConverter<SOURCE, TARGET> {
    String getName();
    TARGET convert(SOURCE source);
}
