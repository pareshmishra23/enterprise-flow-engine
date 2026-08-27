package com.efe.traderecon.intelligence;

import com.efe.traderecon.intelligence.local.FraudDetectionProvider;
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

@DisplayName("FraudDetectionProvider — Unit Tests")
class FraudDetectionProviderTest {

    private FraudDetectionProvider provider;
    private LocalIntelligenceProperties properties;

    @BeforeEach
    void setUp() {
        properties = new LocalIntelligenceProperties();
        properties.setEnabled(true);
        provider = new FraudDetectionProvider(properties);
    }

    @Test
    @DisplayName("Supports FRAUD_DETECTION type only")
    void supportsFraudDetectionType() {
        assertThat(provider.supports(IntelligenceType.FRAUD_DETECTION)).isTrue();
        assertThat(provider.supports(IntelligenceType.LLM_AUDIT)).isFalse();
        assertThat(provider.supports(IntelligenceType.ANOMALY_DETECTION)).isFalse();
    }

    @Test
    @DisplayName("Normal trade — SAFE decision, low fraud risk")
    void normalTradeLowFraudRisk() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tradeId", "T-020");
        payload.put("notionalValue", "50000");
        payload.put("highRiskCounterparty", false);
        payload.put("tradeDirection", "BUY");

        IntelligenceRequest request = new IntelligenceRequest(
                UUID.randomUUID().toString(), IntelligenceType.FRAUD_DETECTION, "TRADE", "COR-20", payload);
        IntelligenceResult result = provider.analyze(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getDecision()).isEqualTo(IntelligenceResult.Decision.SAFE);
        assertThat(result.getScore()).isLessThan(0.75);
    }

    @Test
    @DisplayName("High notional value — elevated risk, HIGH_NOTIONAL_VALUE finding")
    void highNotionalValueElevatesRisk() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tradeId", "T-021");
        payload.put("notionalValue", "15000000");
        payload.put("highRiskCounterparty", false);
        payload.put("tradeDirection", "BUY");

        IntelligenceRequest request = new IntelligenceRequest(
                UUID.randomUUID().toString(), IntelligenceType.FRAUD_DETECTION, "TRADE", "COR-21", payload);
        IntelligenceResult result = provider.analyze(request);

        assertThat(result.getFindings()).anyMatch(f -> f.startsWith("HIGH_NOTIONAL_VALUE"));
    }

    @Test
    @DisplayName("High-risk counterparty — REVIEW decision")
    void highRiskCounterpartyTriggersReview() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tradeId", "T-022");
        payload.put("notionalValue", "100000");
        payload.put("highRiskCounterparty", true);
        payload.put("tradeDirection", "SELL");

        IntelligenceRequest request = new IntelligenceRequest(
                UUID.randomUUID().toString(), IntelligenceType.FRAUD_DETECTION, "TRADE", "COR-22", payload);
        IntelligenceResult result = provider.analyze(request);

        assertThat(result.getDecision()).isEqualTo(IntelligenceResult.Decision.REVIEW);
        assertThat(result.getFindings()).contains("HIGH_RISK_COUNTERPARTY");
    }

    @Test
    @DisplayName("Trade reversal with high notional and high-risk counterparty — multiple findings")
    void tradeReversalMultipleSignals() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tradeId", "T-023");
        payload.put("notionalValue", "12000000");
        payload.put("highRiskCounterparty", true);
        payload.put("tradeDirection", "REVERSAL");

        IntelligenceRequest request = new IntelligenceRequest(
                UUID.randomUUID().toString(), IntelligenceType.FRAUD_DETECTION, "TRADE", "COR-23", payload);
        IntelligenceResult result = provider.analyze(request);

        assertThat(result.getDecision()).isEqualTo(IntelligenceResult.Decision.REVIEW);
        assertThat(result.getFindings()).contains("HIGH_RISK_COUNTERPARTY");
        assertThat(result.getFindings()).contains("UNUSUAL_TRADE_DIRECTION_REVERSAL");
    }

    @Test
    @DisplayName("AI disabled — returns SKIPPED")
    void aiDisabledReturnsSkipped() {
        properties.setEnabled(false);
        Map<String, Object> payload = new HashMap<>();
        payload.put("tradeId", "T-024");

        IntelligenceRequest request = new IntelligenceRequest(
                UUID.randomUUID().toString(), IntelligenceType.FRAUD_DETECTION, "TRADE", "COR-24", payload);
        IntelligenceResult result = provider.analyze(request);

        assertThat(result.getDecision()).isEqualTo(IntelligenceResult.Decision.SKIPPED);
    }
}
