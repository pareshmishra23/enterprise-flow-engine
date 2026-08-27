Feature: EFE Health

  Scenario: EFE is alive
    When I request "/health"
    Then the response status should be 200

  Scenario: EFE is ready
    When I request "/ready"
    Then the response status should be 200
