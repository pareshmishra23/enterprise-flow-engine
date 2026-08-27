Feature: EFE Task Query

  Scenario: Retrieve tasks belonging to a job
    Given a registered job with tasks
    When I request the tasks for that job
    Then the response status should be 200
    And the response should contain a task collection
    And every returned task should belong to the requested job
