package com.efe.traderecon.intelligence.spi;

/**
 * EFE Intelligence Type enumeration.
 * Defines the categories of AI/ML analysis available through the Intelligence SPI.
 * Business flows depend only on this enum, never on provider implementations.
 */
public enum IntelligenceType {
    LLM_AUDIT,
    ANOMALY_DETECTION,
    FRAUD_DETECTION,
    CLASSIFICATION,
    ENRICHMENT,
    EXPLANATION
}
