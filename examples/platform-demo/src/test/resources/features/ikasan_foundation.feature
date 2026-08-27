Feature: EFE Real Ikasan Foundation

  Scenario: Process an event through a real Ikasan flow
    Given the EFE Ikasan module is running
    And the foundation flow is running
    When I submit a test event
    Then the event should enter the Ikasan flow
    And the converter should process the event
    And the processor should process the event
    And the producer should receive the result
    And the event should complete successfully
