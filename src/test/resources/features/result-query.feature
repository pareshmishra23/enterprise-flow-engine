Feature: EFE Result Query

  Scenario: Retrieve job results
    Given a completed job with results
    When I request the results for that job
    Then the response status should be 200
    And the response should contain a result collection
    And every returned result should belong to the requested job
