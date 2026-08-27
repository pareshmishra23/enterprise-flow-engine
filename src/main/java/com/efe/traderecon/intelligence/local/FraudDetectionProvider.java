package com.efe.traderecon.intelligence.local;

import com.efe.traderecon.intelligence.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * EFE Fraud Detection Provider.
 *
 * Rule-based local fraud detection for trade events.
 * Detects known patterns: velocity, threshold breaches, suspicious counterparties.
 *
 * NOTE: This is a PoC implementation. A production system would use a trained
 * ML model exposed through a separate inference service implementing IntelligenceProvider.
 */
@Component
public class FraudDetectionProvider implements IntelligenceProvider {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionProvider.class);

    private static final String MODEL_NAME = "local-fraud-rules-v1";
    private static final double HIGH_VALUE_THRESHOLD = 10_000_000.0;
    private static final double HIGH_RISK_SCORE = 0.75;

    private final LocalIntelligenceProperties properties;

    public FraudDetectionProvider(LocalIntelligenceProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean supports(IntelligenceType type) {
        return IntelligenceType.FRAUD_DETECTION.equals(type);
    }

    @Override
    public String getProviderName() {
        return "local-fraud-detector";
    }

    @Override
    public IntelligenceResult analyze(IntelligenceRequest request) {
        if (!properties.isEnabled()) {
            return IntelligenceResult.skipped(IntelligenceType.FRAUD_DETECTION);
        }

        ModelMetadata metadata = new ModelMetadata("local", MODEL_NAME, "1.0", "n/a");
        log.info("FraudDetectionProvider analyzing request [{}]", request.getRequestId());

        try {
            Map<String, Object> payload = request.getPayload();
            List<String> findings = new ArrayList<>();
            double riskScore = calculateFraudRisk(payload, findings);

            IntelligenceResult.Decision decision = riskScore >= HIGH_RISK_SCORE
                    ? IntelligenceResult.Decision.REVIEW
                    : IntelligenceResult.Decision.SAFE;

            IntelligenceResult result = IntelligenceResult.success(
                    IntelligenceType.FRAUD_DETECTION, decision, 1.0 - riskScore, metadata);
            result.setScore(riskScore);
            result.setFindings(findings);
            result.setExplanation("Fraud risk score: " + String.format("%.2f", riskScore)
                    + " (threshold: " + HIGH_RISK_SCORE + ")");
            return result;

        } catch (Exception e) {
            log.error("Fraud detection failed for request [{}]: {}", request.getRequestId(), e.getMessage());
            return IntelligenceResult.error(IntelligenceType.FRAUD_DETECTION, "AI_PROVIDER_ERROR", metadata);
        }
    }

    private double calculateFraudRisk(Map<String, Object> payload, List<String> findings) {
        if (payload == null) return 0.0;
        double risk = 0.0;

        // Rule 1: High-value transaction
        Object notional = payload.get("notionalValue");
        if (notional != null) {
            try {
                double val = Double.parseDouble(notional.toString());
                if (val > HIGH_VALUE_THRESHOLD) {
                    risk += 0.4;
                    findings.add("HIGH_NOTIONAL_VALUE: " + val);
                }
            } catch (NumberFormatException ignored) {}
        }

        // Rule 2: Known high-risk counterparty flag
        Object counterpartyFlag = payload.get("highRiskCounterparty");
        if (Boolean.TRUE.equals(counterpartyFlag)) {
            risk += 0.8;
            findings.add("HIGH_RISK_COUNTERPARTY");
        }

        // Rule 3: Unusual trade direction
        Object direction = payload.get("tradeDirection");
        if ("REVERSAL".equalsIgnoreCase(direction != null ? direction.toString() : "")) {
            risk += 0.3;
            findings.add("UNUSUAL_TRADE_DIRECTION_REVERSAL");
        }

        return Math.min(risk, 1.0);
    }
}
