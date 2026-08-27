Feature: EFE REST Job API

  Scenario: Submit a job through REST
    Given EFE is running
    And the REST interface is available
    When I submit a valid job to "/api/v1/jobs"
    Then the HTTP status should be 201
    And the response should contain a jobId
    And the job should be registered

  Scenario: Retrieve the job
    Given a job has been registered
    When I request the job using its jobId
    Then the HTTP status should be 200
    And the response should contain the same jobId
