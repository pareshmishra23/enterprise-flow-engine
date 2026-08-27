@ai @configuration
Feature: EFE-003 — AI Intelligence Configuration

  EFE must start and operate normally with AI disabled.
  When AI is enabled, all providers must register with the Intelligence Registry.
  Provider routing must be deterministic and correct.

  Scenario: EFE starts with AI disabled — zero impact on existing flows
    Given AI intelligence is disabled in configuration
    When the EFE application context starts
    Then the intelligence registry should contain the no-op provider
    And the three existing Ikasan flows should be running
    And the intelligence-audit-flow should be registered in the module

  Scenario: Intelligence router selects LLM Audit provider for LLM_AUDIT type
    Given AI intelligence is enabled in configuration
    When the intelligence router receives a request of type "LLM_AUDIT"
    Then the selected provider name should be "ollama-llm-audit"

  Scenario: Intelligence router selects anomaly provider for ANOMALY_DETECTION type
    Given AI intelligence is enabled in configuration
    When the intelligence router receives a request of type "ANOMALY_DETECTION"
    Then the selected provider name should be "local-anomaly-detector"

  Scenario: Intelligence router selects fraud provider for FRAUD_DETECTION type
    Given AI intelligence is enabled in configuration
    When the intelligence router receives a request of type "FRAUD_DETECTION"
    Then the selected provider name should be "local-fraud-detector"

  Scenario: All three AI providers run in sequence — results are aggregated
    Given AI intelligence is enabled in configuration
    And a trade event with tradeId "T-030" with normal attributes
    When the intelligence router broker analyses the event
    Then the intelligence summary should have a recommended action
    And the intelligence summary should contain an explanation
