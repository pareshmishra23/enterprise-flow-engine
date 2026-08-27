package com.efe.traderecon.intelligence;

import com.efe.traderecon.intelligence.aggregator.IntelligenceAggregator;
import com.efe.traderecon.intelligence.aggregator.IntelligenceSummary;
import com.efe.traderecon.intelligence.spi.IntelligenceResult;
import com.efe.traderecon.intelligence.spi.IntelligenceType;
import com.efe.traderecon.intelligence.spi.ModelMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IntelligenceAggregator — Aggregation logic unit tests")
class IntelligenceAggregatorTest {

    private IntelligenceAggregator aggregator;
    private static final ModelMetadata METADATA = new ModelMetadata("local", "test", "1.0", "v1");

    @BeforeEach
    void setUp() {
        aggregator = new IntelligenceAggregator();
    }

    @Test
    @DisplayName("All clear — recommended action is PROCEED")
    void allClearResultsInProceed() {
        IntelligenceResult llm = IntelligenceResult.success(IntelligenceType.LLM_AUDIT, IntelligenceResult.Decision.PASS, 0.9, METADATA);
        IntelligenceResult anomaly = IntelligenceResult.success(IntelligenceType.ANOMALY_DETECTION, IntelligenceResult.Decision.SAFE, 0.1, METADATA);
        anomaly.setScore(0.1);
        IntelligenceResult fraud = IntelligenceResult.success(IntelligenceType.FRAUD_DETECTION, IntelligenceResult.Decision.SAFE, 0.9, METADATA);
        fraud.setScore(0.2);

        IntelligenceSummary summary = aggregator.aggregate("T-001", "COR-001", List.of(llm, anomaly, fraud));

        assertThat(summary.getRecommendedAction()).isEqualTo(IntelligenceSummary.RecommendedAction.PROCEED);
        assertThat(summary.getTradeId()).isEqualTo("T-001");
    }

    @Test
    @DisplayName("LLM FAIL — recommended action is HOLD")
    void llmFailResultsInHold() {
        IntelligenceResult llm = IntelligenceResult.success(IntelligenceType.LLM_AUDIT, IntelligenceResult.Decision.FAIL, 0.95, METADATA);
        IntelligenceResult anomaly = IntelligenceResult.success(IntelligenceType.ANOMALY_DETECTION, IntelligenceResult.Decision.SAFE, 0.5, METADATA);
        anomaly.setScore(0.2);
        IntelligenceResult fraud = IntelligenceResult.success(IntelligenceType.FRAUD_DETECTION, IntelligenceResult.Decision.SAFE, 0.9, METADATA);
        fraud.setScore(0.1);

        IntelligenceSummary summary = aggregator.aggregate("T-002", "COR-002", List.of(llm, anomaly, fraud));

        assertThat(summary.getRecommendedAction()).isEqualTo(IntelligenceSummary.RecommendedAction.HOLD);
    }

    @Test
    @DisplayName("High fraud risk — recommended action is MANUAL_REVIEW")
    void highFraudRiskResultsInManualReview() {
        IntelligenceResult llm = IntelligenceResult.success(IntelligenceType.LLM_AUDIT, IntelligenceResult.Decision.PASS, 0.9, METADATA);
        IntelligenceResult anomaly = IntelligenceResult.success(IntelligenceType.ANOMALY_DETECTION, IntelligenceResult.Decision.SAFE, 0.5, METADATA);
        anomaly.setScore(0.2);
        IntelligenceResult fraud = IntelligenceResult.success(IntelligenceType.FRAUD_DETECTION, IntelligenceResult.Decision.REVIEW, 0.2, METADATA);
        fraud.setScore(0.9); // above 0.75 threshold

        IntelligenceSummary summary = aggregator.aggregate("T-003", "COR-003", List.of(llm, anomaly, fraud));

        assertThat(summary.getRecommendedAction()).isEqualTo(IntelligenceSummary.RecommendedAction.MANUAL_REVIEW);
    }

    @Test
    @DisplayName("All SKIPPED (AI disabled) — recommended action is PROCEED with AI disabled flag")
    void allSkippedResultsInProceedWithDisabledFlag() {
        IntelligenceResult llm = IntelligenceResult.skipped(IntelligenceType.LLM_AUDIT);
        IntelligenceResult anomaly = IntelligenceResult.skipped(IntelligenceType.ANOMALY_DETECTION);
        IntelligenceResult fraud = IntelligenceResult.skipped(IntelligenceType.FRAUD_DETECTION);

        IntelligenceSummary summary = aggregator.aggregate("T-004", "COR-004", List.of(llm, anomaly, fraud));

        assertThat(summary.getRecommendedAction()).isEqualTo(IntelligenceSummary.RecommendedAction.PROCEED);
        assertThat(summary.isAiEnabled()).isFalse();
    }

    @Test
    @DisplayName("High anomaly score — results in HOLD")
    void highAnomalyScoreResultsInHold() {
        IntelligenceResult llm = IntelligenceResult.success(IntelligenceType.LLM_AUDIT, IntelligenceResult.Decision.PASS, 0.9, METADATA);
        IntelligenceResult anomaly = IntelligenceResult.success(IntelligenceType.ANOMALY_DETECTION, IntelligenceResult.Decision.ANOMALOUS, 0.1, METADATA);
        anomaly.setScore(0.95); // above 0.80 threshold
        IntelligenceResult fraud = IntelligenceResult.success(IntelligenceType.FRAUD_DETECTION, IntelligenceResult.Decision.SAFE, 0.9, METADATA);
        fraud.setScore(0.1);

        IntelligenceSummary summary = aggregator.aggregate("T-005", "COR-005", List.of(llm, anomaly, fraud));

        assertThat(summary.getRecommendedAction()).isEqualTo(IntelligenceSummary.RecommendedAction.HOLD);
    }
}
