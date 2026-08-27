package com.efe.traderecon.intelligence.local;

import com.efe.traderecon.intelligence.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * EFE Anomaly Detection Provider.
 *
 * Deterministic local anomaly detection algorithm for trade events.
 * Detects quantity deviations, price anomalies, and missing fields.
 *
 * NOTE: This is a local PoC implementation — not a production ML model.
 * It is designed to be replaced by a real ML inference service in EFE-011+.
 * The interface is production-grade; the algorithm is demonstrative.
 */
@Component
public class AnomalyDetectionProvider implements IntelligenceProvider {

    private static final Logger log = LoggerFactory.getLogger(AnomalyDetectionProvider.class);

    private static final String MODEL_NAME = "local-anomaly-v1";
    private static final double ANOMALY_THRESHOLD = 0.80;

    private final LocalIntelligenceProperties properties;

    public AnomalyDetectionProvider(LocalIntelligenceProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean supports(IntelligenceType type) {
        return IntelligenceType.ANOMALY_DETECTION.equals(type);
    }

    @Override
    public String getProviderName() {
        return "local-anomaly-detector";
    }

    @Override
    public IntelligenceResult analyze(IntelligenceRequest request) {
        if (!properties.isEnabled()) {
            return IntelligenceResult.skipped(IntelligenceType.ANOMALY_DETECTION);
        }

        ModelMetadata metadata = new ModelMetadata("local", MODEL_NAME, "1.0", "n/a");
        log.info("AnomalyDetectionProvider analyzing request [{}]", request.getRequestId());

        try {
            Map<String, Object> payload = request.getPayload();
            double anomalyScore = calculateAnomalyScore(payload);
            List<String> reasonCodes = buildReasonCodes(payload, anomalyScore);

            IntelligenceResult.Decision decision = anomalyScore >= ANOMALY_THRESHOLD
                    ? IntelligenceResult.Decision.ANOMALOUS
                    : IntelligenceResult.Decision.SAFE;

            IntelligenceResult result = IntelligenceResult.success(
                    IntelligenceType.ANOMALY_DETECTION, decision, anomalyScore, metadata);
            result.setScore(anomalyScore);
            result.setReasonCodes(reasonCodes);
            result.setExplanation("Anomaly score: " + String.format("%.2f", anomalyScore)
                    + " (threshold: " + ANOMALY_THRESHOLD + ")");
            return result;

        } catch (Exception e) {
            log.error("Anomaly detection failed for request [{}]: {}", request.getRequestId(), e.getMessage());
            return IntelligenceResult.error(IntelligenceType.ANOMALY_DETECTION, "AI_PROVIDER_ERROR", metadata);
        }
    }

    /**
     * Deterministic anomaly scoring based on quantity/price deviation heuristics.
     */
    private double calculateAnomalyScore(Map<String, Object> payload) {
        if (payload == null) return 0.5;

        double score = 0.0;
        int signals = 0;

        // Signal 1: quantity vs expectedQuantity deviation
        Object qty = payload.get("quantity");
        Object expectedQty = payload.get("expectedQuantity");
        if (qty != null && expectedQty != null) {
            try {
                double q = Double.parseDouble(qty.toString());
                double eq = Double.parseDouble(expectedQty.toString());
                if (eq > 0) {
                    double deviation = Math.abs(q - eq) / eq;
                    score += Math.min(deviation, 1.0);
                    signals++;
                }
            } catch (NumberFormatException ignored) {}
        }

        // Signal 2: price zero or negative
        Object price = payload.get("price");
        if (price != null) {
            try {
                double p = Double.parseDouble(price.toString());
                if (p <= 0) { score += 1.0; signals++; }
            } catch (NumberFormatException ignored) {}
        }

        // Signal 3: missing required fields
        long missingCount = List.of("tradeId", "quantity", "price").stream()
                .filter(f -> payload.get(f) == null).count();
        if (missingCount > 0) {
            score += 0.5 * missingCount;
            signals++;
        }

        return signals > 0 ? Math.min(score / signals, 1.0) : 0.1;
    }

    private List<String> buildReasonCodes(Map<String, Object> payload, double score) {
        List<String> codes = new ArrayList<>();
        if (payload == null) return codes;

        Object qty = payload.get("quantity");
        Object expectedQty = payload.get("expectedQuantity");
        if (qty != null && expectedQty != null) {
            try {
                double q = Double.parseDouble(qty.toString());
                double eq = Double.parseDouble(expectedQty.toString());
                if (eq > 0 && Math.abs(q - eq) / eq > 0.1) {
                    codes.add("QUANTITY_DEVIATION");
                }
            } catch (NumberFormatException ignored) {}
        }

        Object price = payload.get("price");
        if (price != null) {
            try {
                if (Double.parseDouble(price.toString()) <= 0) codes.add("INVALID_PRICE");
            } catch (NumberFormatException ignored) {}
        }

        if (score >= ANOMALY_THRESHOLD) codes.add("HIGH_ANOMALY_SCORE");
        return codes;
    }
}
