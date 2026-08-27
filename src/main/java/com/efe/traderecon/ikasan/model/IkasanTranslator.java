package com.efe.traderecon.ikasan.model;

/**
 * Ikasan Translator Interface.
 * Modifies, validates, or enriches the existing event without changing its type.
 */
public interface IkasanTranslator<EVENT> {
    String getName();
    EVENT translate(EVENT event);
}
