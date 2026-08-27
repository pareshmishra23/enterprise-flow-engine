package com.efe.traderecon.ikasan.model;

import java.util.List;

/**
 * Ikasan Splitter Interface.
 * Splits a composite payload into individual elements.
 */
public interface IkasanSplitter<SOURCE, ITEM> {
    String getName();
    List<ITEM> split(SOURCE source);
}
