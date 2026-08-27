Feature: EFE Ikasan Routing

  Scenario: Route an event to the matching destination
    Given the foundation routing flow is running
    When I submit an event with route "A"
    Then the event should reach producer "A"
    And the event should not reach producer "B"

  Scenario: Route another event to the second destination
    Given the foundation routing flow is running
    When I submit an event with route "B"
    Then the event should reach producer "B"
    And the event should not reach producer "A"
