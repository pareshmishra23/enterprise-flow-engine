@ai @failure @resilience
Feature: EFE-003 — AI Intelligence Failure Resilience

  EFE flows must continue operating when AI components fail.
  AI failures are structured, logged, and non-blocking.
  No AI failure may propagate as an unhandled exception through Ikasan.

  Scenario: LLM timeout or unavailability — result carries AI error code, flow continues
    Given AI intelligence is enabled in configuration
    And the LLM call will time out after the configured timeout
    When the LLM audit provider analyses a trade event
    Then the intelligence result should contain error code "AI_TIMEOUT" or "AI_MODEL_UNAVAILABLE" or "AI_PROVIDER_ERROR"
    And the intelligence result success flag should be false
    And the EFE flow should continue processing

  Scenario: Ollama returns HTTP 500 — result carries AI error code
    Given AI intelligence is enabled in configuration
    And the Ollama server returns HTTP 500
    When the LLM audit provider analyses a trade event
    Then the intelligence result should contain error code "AI_PROVIDER_ERROR" or "AI_MODEL_UNAVAILABLE"
    And the EFE flow should continue processing

  Scenario: Anomaly detection handles null payload gracefully — returns safe result, no crash
    Given AI intelligence is enabled in configuration
    When the anomaly detection provider analyses a trade event
    Then the intelligence result success flag should be false or the intelligence result decision should be "SAFE"
    And the EFE flow should continue processing

  Scenario: All three providers fail — aggregator still produces a summary
    Given AI intelligence is enabled in configuration
    And all three AI providers return error results
    When the intelligence aggregator processes the failed results
    Then the intelligence summary should have a recommended action of "PROCEED" or "MANUAL_REVIEW" or "HOLD"
    And the aggregation should not throw an exception

  Scenario: Intelligence router cannot find provider — structured error, no crash
    Given the intelligence registry has no provider for type "CLASSIFICATION"
    When the intelligence router routes a request of type "CLASSIFICATION"
    Then the intelligence result should contain error code "AI_PROVIDER_ERROR"
    And no exception propagates to the Ikasan flow
