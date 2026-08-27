package com.efe.traderecon.intelligence;

import com.efe.traderecon.intelligence.spi.IntelligenceResult;
import com.efe.traderecon.intelligence.spi.IntelligenceType;
import com.efe.traderecon.intelligence.spi.ModelMetadata;
import com.efe.traderecon.intelligence.local.StructuredResponseParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StructuredResponseParser — LLM response parsing unit tests")
class StructuredResponseParserTest {

    private StructuredResponseParser parser;
    private ModelMetadata testMetadata;

    @BeforeEach
    void setUp() {
        parser = new StructuredResponseParser(new ObjectMapper());
        testMetadata = new ModelMetadata("ollama", "qwen2.5:3b", "1.0", "v1");
    }

    @Test
    @DisplayName("Valid PASS JSON response — parsed correctly")
    void validPassJson() {
        String raw = """
                {"decision": "PASS", "confidence": 0.92, "findings": [], "explanation": "Trade is within normal parameters"}
                """;

        IntelligenceResult result = parser.parse(raw, IntelligenceType.LLM_AUDIT, testMetadata);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDecision()).isEqualTo(IntelligenceResult.Decision.PASS);
        assertThat(result.getConfidence()).isEqualTo(0.92);
        assertThat(result.getExplanation()).contains("normal");
    }

    @Test
    @DisplayName("Valid REVIEW JSON with findings — parsed correctly")
    void validReviewJsonWithFindings() {
        String raw = """
                {"decision": "REVIEW", "confidence": 0.65, "findings": ["QUANTITY_MISMATCH", "PRICE_DEVIATION"], "explanation": "Requires review"}
                """;

        IntelligenceResult result = parser.parse(raw, IntelligenceType.LLM_AUDIT, testMetadata);

        assertThat(result.getDecision()).isEqualTo(IntelligenceResult.Decision.REVIEW);
        assertThat(result.getFindings()).contains("QUANTITY_MISMATCH", "PRICE_DEVIATION");
    }

    @Test
    @DisplayName("Model wraps JSON in markdown — still parsed correctly")
    void markdownFenceStripped() {
        String raw = """
                ```json
                {"decision": "PASS", "confidence": 0.88, "findings": [], "explanation": "OK"}
                ```
                """;

        IntelligenceResult result = parser.parse(raw, IntelligenceType.LLM_AUDIT, testMetadata);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDecision()).isEqualTo(IntelligenceResult.Decision.PASS);
    }

    @Test
    @DisplayName("Malformed JSON — AI_RESPONSE_PARSE_ERROR, no exception")
    void malformedJsonReturnsParseError() {
        String raw = "This trade looks fine, proceed!";

        IntelligenceResult result = parser.parse(raw, IntelligenceType.LLM_AUDIT, testMetadata);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("AI_RESPONSE_PARSE_ERROR");
    }

    @Test
    @DisplayName("Empty response — AI_RESPONSE_PARSE_ERROR")
    void emptyResponseReturnsParseError() {
        IntelligenceResult result = parser.parse("", IntelligenceType.LLM_AUDIT, testMetadata);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("AI_RESPONSE_PARSE_ERROR");
    }

    @Test
    @DisplayName("Null response — AI_RESPONSE_PARSE_ERROR")
    void nullResponseHandledGracefully() {
        IntelligenceResult result = parser.parse(null, IntelligenceType.LLM_AUDIT, testMetadata);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("AI_RESPONSE_PARSE_ERROR");
    }

    @Test
    @DisplayName("Unknown decision value — UNKNOWN decision, no exception")
    void unknownDecisionValueFallsBack() {
        String raw = """
                {"decision": "MAYBE", "confidence": 0.5, "findings": [], "explanation": "Not sure"}
                """;

        IntelligenceResult result = parser.parse(raw, IntelligenceType.LLM_AUDIT, testMetadata);

        assertThat(result.getDecision()).isEqualTo(IntelligenceResult.Decision.UNKNOWN);
    }
}
