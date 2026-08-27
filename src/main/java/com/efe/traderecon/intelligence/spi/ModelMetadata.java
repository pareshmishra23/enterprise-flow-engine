package com.efe.traderecon.intelligence.spi;

/**
 * Metadata about the AI model used to produce an IntelligenceResult.
 * Retained for AI governance, auditability, and future explainability.
 */
public class ModelMetadata {

    private String providerName;
    private String modelName;
    private String modelVersion;
    private String promptVersion;

    public ModelMetadata() {}

    public ModelMetadata(String providerName, String modelName, String modelVersion, String promptVersion) {
        this.providerName = providerName;
        this.modelName = modelName;
        this.modelVersion = modelVersion;
        this.promptVersion = promptVersion;
    }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public String getPromptVersion() { return promptVersion; }
    public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }
}
