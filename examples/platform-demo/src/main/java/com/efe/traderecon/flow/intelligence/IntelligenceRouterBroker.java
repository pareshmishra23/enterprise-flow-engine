package com.efe.traderecon.flow.intelligence;

import com.efe.traderecon.intelligence.aggregator.IntelligenceAggregator;
import com.efe.traderecon.intelligence.aggregator.IntelligenceSummary;
import com.efe.traderecon.intelligence.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * EFE Intelligence Router Broker — Ikasan flow broker component.
 *
 * Routes an IntelligenceRequest through the IntelligenceRouter
 * (LLM Audit → Anomaly → Fraud) and aggregates results.
 *
 * This is an Ikasan-side adapter. It depends on the SPI only,
 * never on Ollama or any vendor-specific class.
 */
@Component
public class IntelligenceRouterBroker {

    private static final Logger log = LoggerFactory.getLogger(IntelligenceRouterBroker.class);

    private final IntelligenceRouter router;
    private final IntelligenceAggregator aggregator;

    public IntelligenceRouterBroker(IntelligenceRouter router, IntelligenceAggregator aggregator) {
        this.router = router;
        this.aggregator = aggregator;
    }

    /**
     * Perform full AI analysis on the event payload.
     * Returns an IntelligenceSummary (aggregated from all provider results).
     */
    public IntelligenceSummary analyze(String tradeId, String correlationId, Map<String, Object> payload) {
        log.info("IntelligenceRouterBroker: starting AI analysis for tradeId={}", tradeId);

        String requestId = UUID.randomUUID().toString();

        // Run LLM Audit
        IntelligenceRequest llmRequest = new IntelligenceRequest(
                requestId + "-llm", IntelligenceType.LLM_AUDIT, "TRADE_EVENT", correlationId, payload);
        IntelligenceResult llmResult = router.route(llmRequest);

        // Run Anomaly Detection
        IntelligenceRequest anomalyRequest = new IntelligenceRequest(
                requestId + "-anomaly", IntelligenceType.ANOMALY_DETECTION, "TRADE_EVENT", correlationId, payload);
        IntelligenceResult anomalyResult = router.route(anomalyRequest);

        // Run Fraud Detection
        IntelligenceRequest fraudRequest = new IntelligenceRequest(
                requestId + "-fraud", IntelligenceType.FRAUD_DETECTION, "TRADE_EVENT", correlationId, payload);
        IntelligenceResult fraudResult = router.route(fraudRequest);

        // Aggregate
        IntelligenceSummary summary = aggregator.aggregate(tradeId, correlationId,
                List.of(llmResult, anomalyResult, fraudResult));

        log.info("IntelligenceRouterBroker: analysis complete for tradeId={} → action={}",
                tradeId, summary.getRecommendedAction());
        return summary;
    }
}
