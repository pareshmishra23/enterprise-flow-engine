Feature: EFE Async Execution

  Scenario: Process events using the executor
    Given the async demo flow is running
    And 20 test events are available
    When the scheduled flow executes
    Then all 20 events should eventually be processed
    And the executor should report completed tasks
    And no event should be lost
