package com.efe.traderecon.intelligence;

import com.efe.traderecon.intelligence.local.AnomalyDetectionProvider;
import com.efe.traderecon.intelligence.local.LocalIntelligenceProperties;
import com.efe.traderecon.intelligence.spi.IntelligenceRequest;
import com.efe.traderecon.intelligence.spi.IntelligenceResult;
import com.efe.traderecon.intelligence.spi.IntelligenceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AnomalyDetectionProvider — Unit Tests")
class AnomalyDetectionProviderTest {

    private AnomalyDetectionProvider provider;
    private LocalIntelligenceProperties properties;

    @BeforeEach
    void setUp() {
        properties = new LocalIntelligenceProperties();
        properties.setEnabled(true);
        provider = new AnomalyDetectionProvider(properties);
    }

    @Test
    @DisplayName("Supports ANOMALY_DETECTION type only")
    void supportsAnomalyDetectionType() {
        assertThat(provider.supports(IntelligenceType.ANOMALY_DETECTION)).isTrue();
        assertThat(provider.supports(IntelligenceType.LLM_AUDIT)).isFalse();
        assertThat(provider.supports(IntelligenceType.FRAUD_DETECTION)).isFalse();
    }

    @Test
    @DisplayName("Normal trade — SAFE decision, low anomaly score")
    void normalTradeLowAnomalyScore() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tradeId", "T-001");
        payload.put("quantity", "1000");
        payload.put("expectedQuantity", "1000");
        payload.put("price", "52.50");

        IntelligenceRequest request = new IntelligenceRequest(
                UUID.randomUUID().toString(), IntelligenceType.ANOMALY_DETECTION, "TRADE", "COR-1", payload);
        IntelligenceResult result = provider.analyze(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDecision()).isEqualTo(IntelligenceResult.Decision.SAFE);
        assertThat(result.getScore()).isLessThan(0.80);
    }

    @Test
    @DisplayName("Large quantity deviation — ANOMALOUS decision, high score")
    void largeQuantityDeviationAnomalous() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tradeId", "T-002");
        payload.put("quantity", "5000");
        payload.put("expectedQuantity", "100");
        payload.put("price", "52.50");

        IntelligenceRequest request = new IntelligenceRequest(
                UUID.randomUUID().toString(), IntelligenceType.ANOMALY_DETECTION, "TRADE", "COR-2", payload);
        IntelligenceResult result = provider.analyze(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDecision()).isEqualTo(IntelligenceResult.Decision.ANOMALOUS);
        assertThat(result.getScore()).isGreaterThanOrEqualTo(0.80);
        assertThat(result.getReasonCodes()).contains("QUANTITY_DEVIATION");
        assertThat(result.getReasonCodes()).contains("HIGH_ANOMALY_SCORE");
    }

    @Test
    @DisplayName("Zero price — INVALID_PRICE reason code emitted")
    void zeroPriceFlagged() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tradeId", "T-003");
        payload.put("quantity", "500");
        payload.put("expectedQuantity", "500");
        payload.put("price", "0");

        IntelligenceRequest request = new IntelligenceRequest(
                UUID.randomUUID().toString(), IntelligenceType.ANOMALY_DETECTION, "TRADE", "COR-3", payload);
        IntelligenceResult result = provider.analyze(request);

        assertThat(result.getReasonCodes()).contains("INVALID_PRICE");
    }

    @Test
    @DisplayName("AI disabled — returns SKIPPED without computation")
    void aiDisabledReturnsSkipped() {
        properties.setEnabled(false);
        Map<String, Object> payload = new HashMap<>();
        payload.put("tradeId", "T-004");

        IntelligenceRequest request = new IntelligenceRequest(
                UUID.randomUUID().toString(), IntelligenceType.ANOMALY_DETECTION, "TRADE", "COR-4", payload);
        IntelligenceResult result = provider.analyze(request);

        assertThat(result.getDecision()).isEqualTo(IntelligenceResult.Decision.SKIPPED);
    }

    @Test
    @DisplayName("Null payload — returns valid result without exception")
    void nullPayloadHandledGracefully() {
        IntelligenceRequest request = new IntelligenceRequest(
                UUID.randomUUID().toString(), IntelligenceType.ANOMALY_DETECTION, "TRADE", "COR-5", null);
        IntelligenceResult result = provider.analyze(request);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }
}
