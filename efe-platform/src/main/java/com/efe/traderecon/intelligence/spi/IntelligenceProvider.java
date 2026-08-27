package com.efe.traderecon.intelligence.spi;

/**
 * EFE Intelligence Provider SPI.
 *
 * All AI implementations (Ollama, OpenAI, Azure OpenAI, Hugging Face,
 * local ONNX, custom REST model) must implement this interface.
 *
 * Business processors and Ikasan flow components depend ONLY on this interface.
 * No vendor-specific classes may appear outside the provider implementation package.
 */
public interface IntelligenceProvider {

    /**
     * Returns true if this provider can handle the given IntelligenceType.
     */
    boolean supports(IntelligenceType type);

    /**
     * Execute the intelligence analysis and return a structured result.
     * Implementations must NOT throw exceptions for AI failures —
     * return an error-coded IntelligenceResult instead.
     */
    IntelligenceResult analyze(IntelligenceRequest request);

    /**
     * Returns the provider identifier used for routing and logging.
     */
    String getProviderName();

    /**
     * Returns true if this provider is currently available and configured.
     */
    default boolean isAvailable() {
        return true;
    }
}
