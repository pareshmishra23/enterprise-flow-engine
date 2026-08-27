package com.efe.traderecon.intelligence.local;

import com.efe.traderecon.intelligence.sanitizer.AiDataSanitizer;
import com.efe.traderecon.intelligence.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

/**
 * EFE LLM Audit Provider.
 *
 * Uses Ollama with a configurable local LLM model to perform trade event auditing.
 * Returns structured IntelligenceResult — never raw text.
 *
 * IMPORTANT: This class is the only place in EFE that knows about Ollama.
 * Business processors see only IntelligenceProvider / IntelligenceResult.
 */
@Component
public class LlmAuditProvider implements IntelligenceProvider {

    private static final Logger log = LoggerFactory.getLogger(LlmAuditProvider.class);

    private final LocalIntelligenceProperties properties;
    private final OllamaClient ollamaClient;
    private final PromptBuilder promptBuilder;
    private final StructuredResponseParser responseParser;
    private final AiDataSanitizer dataSanitizer;

    public LlmAuditProvider(LocalIntelligenceProperties properties,
                             OllamaClient ollamaClient,
                             PromptBuilder promptBuilder,
                             StructuredResponseParser responseParser,
                             AiDataSanitizer dataSanitizer) {
        this.properties = properties;
        this.ollamaClient = ollamaClient;
        this.promptBuilder = promptBuilder;
        this.responseParser = responseParser;
        this.dataSanitizer = dataSanitizer;
    }

    @Override
    public boolean supports(IntelligenceType type) {
        return IntelligenceType.LLM_AUDIT.equals(type);
    }

    @Override
    public String getProviderName() {
        return "ollama-llm-audit";
    }

    @Override
    public IntelligenceResult analyze(IntelligenceRequest request) {
        if (!properties.isEnabled()) {
            log.info("AI intelligence is disabled — returning SKIPPED result");
            return IntelligenceResult.skipped(IntelligenceType.LLM_AUDIT);
        }

        String modelName = properties.getModels().getLlmAudit().getName();
        ModelMetadata metadata = new ModelMetadata(
                "ollama", modelName, "1.0", PromptBuilder.PROMPT_VERSION);

        log.info("LlmAuditProvider analyzing request [{}] with model [{}]",
                request.getRequestId(), modelName);

        try {
            // 1. Sanitize payload before sending to LLM
            var sanitizedPayload = dataSanitizer.sanitize(request.getPayload());

            // 2. Build versioned prompt
            String prompt = promptBuilder.buildAuditPrompt(sanitizedPayload);

            // 3. Call Ollama
            String rawResponse = ollamaClient.generate(modelName, prompt);

            // 4. Parse structured JSON from response
            return responseParser.parse(rawResponse, IntelligenceType.LLM_AUDIT, metadata);

        } catch (TimeoutException e) {
            log.warn("LLM audit timed out for request [{}]", request.getRequestId());
            return IntelligenceResult.error(IntelligenceType.LLM_AUDIT, "AI_TIMEOUT", metadata);

        } catch (Exception e) {
            String errorCode = isConnectionRefused(e) ? "AI_MODEL_UNAVAILABLE" : "AI_PROVIDER_ERROR";
            log.warn("LLM audit failed [{}]: {} — errorCode={}", request.getRequestId(), e.getMessage(), errorCode);
            IntelligenceResult err = IntelligenceResult.error(IntelligenceType.LLM_AUDIT, errorCode, metadata);
            err.setExplanation(e.getMessage());
            return err;
        }
    }

    private boolean isConnectionRefused(Exception e) {
        String msg = e.getMessage();
        return msg != null && (msg.contains("Connection refused") || msg.contains("connect") || msg.contains("ConnectException"));
    }
}
