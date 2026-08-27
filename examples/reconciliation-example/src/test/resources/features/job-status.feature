Feature: EFE Job Status

  Scenario: Retrieve an existing job
    Given a job has been registered
    When I request "/api/v1/jobs/{jobId}"
    Then the response status should be 200
    And the response should contain the jobId
    And the response should contain the jobType
    And the response should contain the job status

  Scenario: Retrieve a job that does not exist
    Given jobId "JOB-NOT-FOUND" does not exist
    When I request "/api/v1/jobs/JOB-NOT-FOUND"
    Then the response status should be 404
    And the response errorCode should be "EFE-JOB-404"
