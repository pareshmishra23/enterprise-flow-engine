@ai @fraud-detection
Feature: EFE-003 — Fraud Detection via Local Intelligence Provider

  Fraud detection uses deterministic rule-based scoring locally.
  It raises a REVIEW flag — never a REJECT without human confirmation.
  AI audit must NEVER become the financial authority.

  Background:
    Given the EFE intelligence layer is configured with provider "local-fraud-detector"

  Scenario: Normal trade — low fraud risk, decision is SAFE
    Given AI intelligence is enabled in configuration
    And a trade event with the following attributes:
      | tradeId             | T-020         |
      | notionalValue       | 50000         |
      | highRiskCounterparty| false         |
      | tradeDirection      | BUY           |
    When the fraud detection provider analyses the event
    Then the intelligence result decision should be "SAFE"
    And the fraud risk score should be below 0.75

  Scenario: High notional value — elevated fraud risk score
    Given AI intelligence is enabled in configuration
    And a trade event with notionalValue "15000000" and normal counterparty
    When the fraud detection provider analyses the event
    Then the findings should contain "HIGH_NOTIONAL_VALUE"

  Scenario: High-risk counterparty — REVIEW decision
    Given AI intelligence is enabled in configuration
    And a trade event with the following attributes:
      | tradeId             | T-022        |
      | notionalValue       | 100000       |
      | highRiskCounterparty| true         |
      | tradeDirection      | SELL         |
    When the fraud detection provider analyses the event
    Then the intelligence result decision should be "REVIEW"
    And the findings should contain "HIGH_RISK_COUNTERPARTY"

  Scenario: Trade reversal with high notional — multiple fraud signals
    Given AI intelligence is enabled in configuration
    And a trade event with the following attributes:
      | tradeId             | T-023        |
      | notionalValue       | 12000000     |
      | highRiskCounterparty| true         |
      | tradeDirection      | REVERSAL     |
    When the fraud detection provider analyses the event
    Then the intelligence result decision should be "REVIEW"
    And the findings should contain "HIGH_RISK_COUNTERPARTY"
    And the findings should contain "UNUSUAL_TRADE_DIRECTION_REVERSAL"

  Scenario: AI disabled — fraud detection returns SKIPPED
    Given AI intelligence is disabled in configuration
    And a trade event with tradeId "T-024"
    When the fraud detection provider analyses the event
    Then the intelligence result decision should be "SKIPPED"
