Feature: EFE Database Component

  Scenario: Persist and retrieve a job
    Given the H2 database is available
    When I submit a test job
    Then the job should be stored
    And the job should be retrievable
