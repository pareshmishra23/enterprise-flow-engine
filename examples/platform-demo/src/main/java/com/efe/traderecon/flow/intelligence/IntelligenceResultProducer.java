package com.efe.traderecon.flow.intelligence;

import com.efe.traderecon.intelligence.aggregator.IntelligenceSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * EFE Intelligence Result Producer — Ikasan flow producer.
 *
 * Emits the IntelligenceSummary to a downstream sink.
 * In EFE-003 this logs and stores in-memory; future beads (EFE-008, EFE-011)
 * will wire this to a real persistence or messaging producer.
 */
@Component
public class IntelligenceResultProducer {

    private static final Logger log = LoggerFactory.getLogger(IntelligenceResultProducer.class);

    /**
     * Persist or emit the intelligence summary downstream.
     * EFE-008 (Persistence SPI) will replace this with real storage.
     */
    public void produce(IntelligenceSummary summary) {
        log.info("IntelligenceResultProducer: tradeId={}, action={}, aiEnabled={}, findings={}",
                summary.getTradeId(),
                summary.getRecommendedAction(),
                summary.isAiEnabled(),
                summary.getAllFindings());

        // Structured audit log — always emitted regardless of AI decision
        log.info("AI_AUDIT_RECORD | tradeId={} | correlationId={} | llmDecision={} | anomalyScore={} " +
                        "| fraudRisk={} | action={} | explanation={}",
                summary.getTradeId(),
                summary.getCorrelationId(),
                summary.getLlmAuditDecision(),
                String.format("%.2f", summary.getAnomalyScore()),
                String.format("%.2f", summary.getFraudRiskScore()),
                summary.getRecommendedAction(),
                summary.getExplanation());
    }
}
