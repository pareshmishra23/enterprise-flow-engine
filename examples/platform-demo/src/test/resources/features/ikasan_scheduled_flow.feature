Feature: EFE Ikasan Scheduled Flow

  Scenario: Execute a scheduled flow
    Given the scheduled foundation flow is running
    When the scheduled trigger occurs
    Then the scheduled consumer should initiate processing
    And the processor should execute
    And the producer should receive the result
