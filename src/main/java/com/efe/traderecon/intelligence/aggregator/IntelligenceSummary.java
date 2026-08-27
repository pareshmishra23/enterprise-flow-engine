package com.efe.traderecon.intelligence.aggregator;

import com.efe.traderecon.intelligence.spi.IntelligenceResult;

import java.util.List;

/**
 * Aggregated intelligence summary for a single trade event.
 * Combines outputs from multiple IntelligenceProviders into a single decision envelope.
 *
 * IMPORTANT: This summary is decision-support only.
 * It must NEVER be used as the authoritative financial result.
 */
public class IntelligenceSummary {

    public enum RecommendedAction { PROCEED, MANUAL_REVIEW, HOLD, REJECT }

    private String tradeId;
    private String correlationId;

    // Individual provider decisions
    private IntelligenceResult.Decision llmAuditDecision;
    private double anomalyScore;
    private double fraudRiskScore;

    // Aggregated output
    private RecommendedAction recommendedAction;
    private String explanation;
    private List<String> allFindings;
    private boolean aiEnabled;

    public IntelligenceSummary() {}

    public String getTradeId() { return tradeId; }
    public void setTradeId(String tradeId) { this.tradeId = tradeId; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public IntelligenceResult.Decision getLlmAuditDecision() { return llmAuditDecision; }
    public void setLlmAuditDecision(IntelligenceResult.Decision llmAuditDecision) { this.llmAuditDecision = llmAuditDecision; }

    public double getAnomalyScore() { return anomalyScore; }
    public void setAnomalyScore(double anomalyScore) { this.anomalyScore = anomalyScore; }

    public double getFraudRiskScore() { return fraudRiskScore; }
    public void setFraudRiskScore(double fraudRiskScore) { this.fraudRiskScore = fraudRiskScore; }

    public RecommendedAction getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(RecommendedAction recommendedAction) { this.recommendedAction = recommendedAction; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public List<String> getAllFindings() { return allFindings; }
    public void setAllFindings(List<String> allFindings) { this.allFindings = allFindings; }

    public boolean isAiEnabled() { return aiEnabled; }
    public void setAiEnabled(boolean aiEnabled) { this.aiEnabled = aiEnabled; }
}
