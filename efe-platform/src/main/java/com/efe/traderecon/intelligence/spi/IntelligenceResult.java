package com.efe.traderecon.intelligence.spi;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * EFE Intelligence Result — structured output from any AI provider.
 *
 * AI failures are represented as error codes within this result,
 * not as thrown exceptions propagated through the Ikasan flow.
 *
 * IMPORTANT: This result must never silently overwrite authoritative
 * financial results. AI output is decision support only.
 */
public class IntelligenceResult {

    public enum Decision { PASS, REVIEW, FAIL, ANOMALOUS, SAFE, SKIPPED, UNKNOWN }

    // Core classification
    private IntelligenceType intelligenceType;
    private Decision decision;
    private double confidence;
    private double score;

    // Model provenance
    private ModelMetadata modelMetadata;

    // Analysis output
    private List<String> findings = new ArrayList<>();
    private List<String> reasonCodes = new ArrayList<>();
    private String explanation;

    // Error handling
    private String errorCode;   // AI_TIMEOUT, AI_MODEL_UNAVAILABLE, AI_RESPONSE_PARSE_ERROR, etc.
    private boolean success;

    // Timing
    private long processingTimeMs;
    private Instant timestamp;

    // Correlation
    private String requestId;
    private String correlationId;

    public IntelligenceResult() {
        this.timestamp = Instant.now();
        this.success = true;
    }

    /** Factory: successful result */
    public static IntelligenceResult success(IntelligenceType type, Decision decision,
                                             double confidence, ModelMetadata metadata) {
        IntelligenceResult r = new IntelligenceResult();
        r.intelligenceType = type;
        r.decision = decision;
        r.confidence = confidence;
        r.modelMetadata = metadata;
        r.success = true;
        return r;
    }

    /** Factory: error result — AI failure is observable but non-crashing */
    public static IntelligenceResult error(IntelligenceType type, String errorCode, ModelMetadata metadata) {
        IntelligenceResult r = new IntelligenceResult();
        r.intelligenceType = type;
        r.decision = Decision.UNKNOWN;
        r.errorCode = errorCode;
        r.success = false;
        r.modelMetadata = metadata;
        return r;
    }

    /** Factory: skipped when AI is disabled */
    public static IntelligenceResult skipped(IntelligenceType type) {
        IntelligenceResult r = new IntelligenceResult();
        r.intelligenceType = type;
        r.decision = Decision.SKIPPED;
        r.success = true;
        r.explanation = "AI intelligence is disabled";
        r.modelMetadata = new ModelMetadata("no-op", "none", "none", "none");
        return r;
    }

    // Getters and setters
    public IntelligenceType getIntelligenceType() { return intelligenceType; }
    public void setIntelligenceType(IntelligenceType intelligenceType) { this.intelligenceType = intelligenceType; }

    public Decision getDecision() { return decision; }
    public void setDecision(Decision decision) { this.decision = decision; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public ModelMetadata getModelMetadata() { return modelMetadata; }
    public void setModelMetadata(ModelMetadata modelMetadata) { this.modelMetadata = modelMetadata; }

    public List<String> getFindings() { return findings; }
    public void setFindings(List<String> findings) { this.findings = findings != null ? findings : new ArrayList<>(); }

    public List<String> getReasonCodes() { return reasonCodes; }
    public void setReasonCodes(List<String> reasonCodes) { this.reasonCodes = reasonCodes != null ? reasonCodes : new ArrayList<>(); }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    @Override
    public String toString() {
        return "IntelligenceResult{type=" + intelligenceType + ", decision=" + decision +
                ", confidence=" + confidence + ", errorCode='" + errorCode + "', success=" + success + "}";
    }
}
