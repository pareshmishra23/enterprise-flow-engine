package com.efe.traderecon.intelligence.local;

import com.efe.traderecon.intelligence.spi.IntelligenceResult;
import com.efe.traderecon.intelligence.spi.IntelligenceType;
import com.efe.traderecon.intelligence.spi.ModelMetadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * EFE Structured Response Parser.
 *
 * Parses the raw LLM JSON output into a structured IntelligenceResult.
 * Malformed or unexpected responses become controlled AI_RESPONSE_PARSE_ERROR
 * results — never unhandled exceptions propagated through the EFE flow.
 */
@Component
public class StructuredResponseParser {

    private static final Logger log = LoggerFactory.getLogger(StructuredResponseParser.class);

    private final ObjectMapper objectMapper;

    public StructuredResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public IntelligenceResult parse(String rawResponse, IntelligenceType type, ModelMetadata metadata) {
        if (rawResponse == null || rawResponse.isBlank()) {
            log.warn("LLM returned empty response");
            IntelligenceResult err = IntelligenceResult.error(type, "AI_RESPONSE_PARSE_ERROR", metadata);
            err.setExplanation("LLM returned empty response");
            return err;
        }

        try {
            // Strip any accidental markdown fences the model may add
            String cleaned = rawResponse.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```[a-z]*", "").replace("```", "").trim();
            }

            // Find first JSON object in response
            int jsonStart = cleaned.indexOf('{');
            int jsonEnd = cleaned.lastIndexOf('}');
            if (jsonStart == -1 || jsonEnd == -1 || jsonEnd <= jsonStart) {
                log.warn("No JSON object found in LLM response: {}", rawResponse.substring(0, Math.min(200, rawResponse.length())));
                IntelligenceResult err = IntelligenceResult.error(type, "AI_RESPONSE_PARSE_ERROR", metadata);
                err.setExplanation("No JSON object found in model response");
                return err;
            }

            String jsonPart = cleaned.substring(jsonStart, jsonEnd + 1);
            JsonNode root = objectMapper.readTree(jsonPart);

            String decisionStr = root.path("decision").asText("UNKNOWN").toUpperCase();
            IntelligenceResult.Decision decision;
            try {
                decision = IntelligenceResult.Decision.valueOf(decisionStr);
            } catch (IllegalArgumentException e) {
                decision = IntelligenceResult.Decision.UNKNOWN;
            }

            double confidence = root.path("confidence").asDouble(0.5);
            String explanation = root.path("explanation").asText("");

            List<String> findings = new ArrayList<>();
            JsonNode findingsNode = root.path("findings");
            if (findingsNode.isArray()) {
                findingsNode.forEach(f -> findings.add(f.asText()));
            }

            IntelligenceResult result = IntelligenceResult.success(type, decision, confidence, metadata);
            result.setFindings(findings);
            result.setExplanation(explanation);
            return result;

        } catch (Exception e) {
            log.warn("Failed to parse LLM response: {}", e.getMessage());
            IntelligenceResult err = IntelligenceResult.error(type, "AI_RESPONSE_PARSE_ERROR", metadata);
            err.setExplanation("Parse error: " + e.getMessage());
            return err;
        }
    }
}
