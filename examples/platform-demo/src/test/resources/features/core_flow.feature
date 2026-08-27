Feature: EFE Core Flow Demonstrator

  Background:
    Given the EFE core flow is running
    And the match and break output stores are empty

  Scenario: Route a matching event
    Given I submit an event with expected quantity 100 and actual quantity 100
    When the event is processed by the EFE core flow
    Then the event should be classified as "MATCH"
    And the event should be received by the "EFE-MATCH-OUT" producer
    And the event should not be received by the "EFE-BREAK-OUT" producer

  Scenario: Route a breaking event
    Given I submit an event with expected quantity 100 and actual quantity 80
    When the event is processed by the EFE core flow
    Then the event should be classified as "BREAK"
    And the event should be received by the "EFE-BREAK-OUT" producer
    And the event should not be received by the "EFE-MATCH-OUT" producer

  Scenario: Reject an invalid event
    Given I submit an event without an eventId
    When the event is processed by the EFE core flow
    Then the event should fail validation
    And no match or break output should be produced
