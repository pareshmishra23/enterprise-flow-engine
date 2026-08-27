Feature: EFE AI Component

  Scenario: Process an event through local AI
    Given AI is enabled
    And the local AI provider is available
    When an event is submitted to the AI flow
    Then an intelligence result should be produced
    And the result should contain a model name
    And the event should continue to the next flow component

  Scenario: Process the same flow with AI disabled
    Given AI is disabled
    When an event is submitted to the AI flow
    Then the flow should complete without an AI invocation
