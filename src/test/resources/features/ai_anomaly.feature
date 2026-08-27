@ai @anomaly-detection
Feature: EFE-003 — Anomaly Detection via Local Intelligence Provider

  The anomaly detection provider analyses trade events for statistical deviations
  using a deterministic local algorithm. It does not call any external service.

  Background:
    Given the EFE intelligence layer is configured with provider "local-anomaly-detector"

  Scenario: Normal trade — low anomaly score, decision is SAFE
    Given AI intelligence is enabled in configuration
    And a trade event with the following attributes:
      | tradeId          | T-010 |
      | quantity         | 1000  |
      | expectedQuantity | 1000  |
      | price            | 52.50 |
    When the anomaly detection provider analyses the event
    Then the intelligence result decision should be "SAFE"
    And the anomaly score should be below 0.80

  Scenario: Large quantity deviation — high anomaly score, decision is ANOMALOUS
    Given AI intelligence is enabled in configuration
    And a trade event with the following attributes:
      | tradeId          | T-011 |
      | quantity         | 5000  |
      | expectedQuantity | 100   |
      | price            | 52.50 |
    When the anomaly detection provider analyses the event
    Then the intelligence result decision should be "ANOMALOUS"
    And the anomaly score should be at or above 0.80
    And the reason codes should contain "QUANTITY_DEVIATION"

  Scenario: Zero price — flagged as anomaly
    Given AI intelligence is enabled in configuration
    And a trade event with price "0" and quantity "500"
    When the anomaly detection provider analyses the event
    Then the reason codes should contain "INVALID_PRICE"

  Scenario: AI disabled — anomaly detection returns SKIPPED without computation
    Given AI intelligence is disabled in configuration
    And a trade event with tradeId "T-012"
    When the anomaly detection provider analyses the event
    Then the intelligence result decision should be "SKIPPED"
