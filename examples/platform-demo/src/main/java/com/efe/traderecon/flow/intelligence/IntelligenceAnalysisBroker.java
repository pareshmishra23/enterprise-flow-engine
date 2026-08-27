package com.efe.traderecon.flow.intelligence;

import com.efe.traderecon.intelligence.aggregator.IntelligenceSummary;
import com.efe.traderecon.ikasan.model.IkasanBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * EFE Intelligence Analysis Broker — Ikasan Broker for the intelligence-audit-flow.
 *
 * Receives a trade event payload from the consumer, runs all three AI providers
 * (LLM audit, anomaly detection, fraud detection) via IntelligenceRouterBroker,
 * and produces an aggregated IntelligenceSummary.
 */
@Component
public class IntelligenceAnalysisBroker implements IkasanBroker<Map<String, Object>, IntelligenceSummary> {

    private static final Logger log = LoggerFactory.getLogger(IntelligenceAnalysisBroker.class);

    private final IntelligenceRouterBroker routerBroker;

    public IntelligenceAnalysisBroker(IntelligenceRouterBroker routerBroker) {
        this.routerBroker = routerBroker;
    }

    @Override
    public String getName() {
        return "intelligence-analysis-broker";
    }

    @Override
    public IntelligenceSummary invoke(Map<String, Object> payload) {
        if (payload == null) {
            log.warn("IntelligenceAnalysisBroker received null payload");
            return null;
        }

        String tradeId = (String) payload.getOrDefault("tradeId", "UNKNOWN");
        String correlationId = (String) payload.getOrDefault("correlationId", "UNKNOWN");

        log.info("IntelligenceAnalysisBroker: analysing tradeId={}", tradeId);
        return routerBroker.analyze(tradeId, correlationId, payload);
    }
}
