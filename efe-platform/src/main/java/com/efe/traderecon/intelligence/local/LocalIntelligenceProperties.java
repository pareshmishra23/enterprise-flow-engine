package com.efe.traderecon.intelligence.local;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * EFE Intelligence configuration properties.
 * Binds all efe.intelligence.* properties from application.yml.
 */
@Component
@ConfigurationProperties(prefix = "efe.intelligence")
public class LocalIntelligenceProperties {

    private boolean enabled = false;
    private String provider = "local";
    private long timeoutMs = 5000;

    private Local local = new Local();
    private Models models = new Models();

    public static class Local {
        private String runtime = "ollama";
        private String endpoint = "http://localhost:11434";

        public String getRuntime() { return runtime; }
        public void setRuntime(String runtime) { this.runtime = runtime; }

        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    }

    public static class ModelConfig {
        private String name = "qwen2.5:3b";
        private boolean enabled = true;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class Models {
        private ModelConfig llmAudit = new ModelConfig();
        private ModelConfig anomaly = new ModelConfig();
        private ModelConfig fraud = new ModelConfig();

        public ModelConfig getLlmAudit() { return llmAudit; }
        public void setLlmAudit(ModelConfig llmAudit) { this.llmAudit = llmAudit; }

        public ModelConfig getAnomaly() { return anomaly; }
        public void setAnomaly(ModelConfig anomaly) { this.anomaly = anomaly; }

        public ModelConfig getFraud() { return fraud; }
        public void setFraud(ModelConfig fraud) { this.fraud = fraud; }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public long getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }

    public Local getLocal() { return local; }
    public void setLocal(Local local) { this.local = local; }

    public Models getModels() { return models; }
    public void setModels(Models models) { this.models = models; }
}
