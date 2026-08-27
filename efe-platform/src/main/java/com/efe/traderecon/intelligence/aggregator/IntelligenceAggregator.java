package com.efe.traderecon.intelligence.aggregator;

import com.efe.traderecon.intelligence.spi.IntelligenceResult;
import com.efe.traderecon.intelligence.spi.IntelligenceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * EFE Intelligence Aggregator.
 *
 * Combines multiple IntelligenceResult objects (LLM audit, anomaly, fraud)
 * into a single IntelligenceSummary with a recommended action.
 *
 * The aggregation is deterministic and auditable.
 * Rules: FAIL from any provider → HOLD/REJECT; REVIEW from any → MANUAL_REVIEW; else → PROCEED.
 */
@Component
public class IntelligenceAggregator {

    private static final Logger log = LoggerFactory.getLogger(IntelligenceAggregator.class);

    private static final double ANOMALY_HIGH_THRESHOLD = 0.80;
    private static final double FRAUD_HIGH_THRESHOLD = 0.75;

    public IntelligenceSummary aggregate(String tradeId, String correlationId,
                                         List<IntelligenceResult> results) {
        IntelligenceSummary summary = new IntelligenceSummary();
        summary.setTradeId(tradeId);
        summary.setCorrelationId(correlationId);

        List<String> allFindings = new ArrayList<>();
        boolean anyFail = false;
        boolean anyReview = false;
        boolean aiEnabled = false;

        for (IntelligenceResult result : results) {
            if (result == null) continue;

            // Collect findings
            if (result.getFindings() != null) {
                allFindings.addAll(result.getFindings());
            }
            if (result.getReasonCodes() != null) {
                allFindings.addAll(result.getReasonCodes());
            }

            // Track whether AI actually ran
            if (!IntelligenceResult.Decision.SKIPPED.equals(result.getDecision())) {
                aiEnabled = true;
            }

            // Determine contribution to final decision
            if (IntelligenceType.LLM_AUDIT.equals(result.getIntelligenceType())) {
                summary.setLlmAuditDecision(result.getDecision());
                if (IntelligenceResult.Decision.FAIL.equals(result.getDecision())) anyFail = true;
                if (IntelligenceResult.Decision.REVIEW.equals(result.getDecision())) anyReview = true;
            }

            if (IntelligenceType.ANOMALY_DETECTION.equals(result.getIntelligenceType())) {
                summary.setAnomalyScore(result.getScore());
                if (result.getScore() >= ANOMALY_HIGH_THRESHOLD) anyFail = true;
            }

            if (IntelligenceType.FRAUD_DETECTION.equals(result.getIntelligenceType())) {
                summary.setFraudRiskScore(result.getScore());
                if (result.getScore() >= FRAUD_HIGH_THRESHOLD) anyReview = true;
            }
        }

        // Aggregate to recommended action
        IntelligenceSummary.RecommendedAction action;
        String explanation;
        if (anyFail) {
            action = IntelligenceSummary.RecommendedAction.HOLD;
            explanation = "AI intelligence flagged critical issues — trade on hold for review";
        } else if (anyReview) {
            action = IntelligenceSummary.RecommendedAction.MANUAL_REVIEW;
            explanation = "AI intelligence recommends manual review before proceeding";
        } else {
            action = IntelligenceSummary.RecommendedAction.PROCEED;
            explanation = aiEnabled ? "AI intelligence cleared — proceed" : "AI disabled — proceed without AI validation";
        }

        summary.setRecommendedAction(action);
        summary.setExplanation(explanation);
        summary.setAllFindings(allFindings);
        summary.setAiEnabled(aiEnabled);

        log.info("IntelligenceAggregator: tradeId={}, action={}, findings={}", tradeId, action, allFindings.size());
        return summary;
    }
}
