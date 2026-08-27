package com.efe.traderecon.intelligence.local;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * EFE Prompt Builder.
 *
 * Responsible for constructing versioned AI prompts from EFE event data.
 * Prompts must NEVER be embedded in REST controllers, Ikasan configuration,
 * or business processors.
 *
 * Prompt templates are versionable — the version is stored in ModelMetadata.
 */
@Component
public class PromptBuilder {

    public static final String PROMPT_VERSION = "v1";

    /**
     * Build the LLM audit prompt for a trade event.
     * The model is instructed to return structured JSON only.
     */
    public String buildAuditPrompt(Map<String, Object> sanitizedPayload) {
        return """
                You are an enterprise trade audit AI assistant.
                Analyze the following trade event and return ONLY a JSON object with no additional text.
                
                Trade event:
                %s
                
                Return ONLY this JSON structure (no explanation, no markdown):
                {
                  "decision": "PASS",
                  "confidence": 0.9,
                  "findings": [],
                  "explanation": "brief explanation"
                }
                
                Where decision is one of: PASS, REVIEW, FAIL
                And confidence is a number between 0.0 and 1.0.
                """.formatted(sanitizedPayload.toString());
    }
}
