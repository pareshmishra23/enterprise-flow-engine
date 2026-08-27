@ai @llm-audit
Feature: EFE-003 — LLM Audit via Local Intelligence Provider

  AI audit is a pluggable EFE flow component that analyses trade events
  using a local LLM (Ollama). It is NEVER the financial authority.

  Background:
    Given the EFE intelligence layer is configured with provider "ollama-llm-audit"

  Scenario: Trade event passes LLM audit
    Given a trade event with the following attributes:
      | tradeId    | T-001        |
      | quantity   | 1000         |
      | price      | 52.50        |
      | currency   | USD          |
    When the LLM audit provider analyses the event
    Then the intelligence result decision should be "PASS" or "REVIEW" or "UNKNOWN"
    And the intelligence result should have a model metadata entry

  Scenario: AI is disabled — LLM audit returns SKIPPED without calling Ollama
    Given AI intelligence is disabled in configuration
    And a trade event with tradeId "T-002"
    When the LLM audit provider analyses the event
    Then the intelligence result decision should be "SKIPPED"
    And no call is made to Ollama

  Scenario: Ollama is unavailable — result has AI error code, flow continues
    Given AI intelligence is enabled in configuration
    And Ollama is not reachable at the configured endpoint
    And a trade event with tradeId "T-003"
    When the LLM audit provider analyses the event
    Then the intelligence result should contain error code "AI_MODEL_UNAVAILABLE" or "AI_PROVIDER_ERROR"
    And the intelligence result success flag should be false
    And the EFE flow should continue processing

  Scenario: Sensitive fields are masked before sending to LLM
    Given AI intelligence is enabled in configuration
    And a trade event containing sensitive field "accountId" with value "ACC-12345"
    When the data sanitizer processes the payload
    Then the field "accountId" should be masked as "***MASKED***"
    And the original payload should not be modified

  Scenario: LLM returns malformed JSON — result is AI_RESPONSE_PARSE_ERROR
    Given AI intelligence is enabled in configuration
    And the LLM returns a malformed non-JSON response "This trade looks fine to me!"
    When the response parser processes the LLM output
    Then the intelligence result should contain error code "AI_RESPONSE_PARSE_ERROR"
    And the flow should still complete without throwing an exception
