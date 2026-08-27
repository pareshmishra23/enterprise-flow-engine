Feature: EFE Job Submission

  Background:
    Given EFE is running
    And the messaging provider is "inmemory"
    And the persistence provider is "inmemory"

  Scenario: Submit a valid job
    Given I have a valid job request with jobType "TRADE_RECONCILIATION"
    When I submit the request to "/api/v1/jobs"
    Then the response status should be 201
    And the response should contain a jobId
    And the response status should be "REGISTERED"
    And the response should contain a Location header
    And the job should exist in EFE

  Scenario: Submit a job with missing jobType
    Given I have a job request without a jobType
    When I submit the request to "/api/v1/jobs"
    Then the response status should be 400
    And the response errorCode should be "EFE-VAL-001"
    And no job should be created

  Scenario: Submit a job with missing businessDate
    Given I have a job request without a businessDate
    When I submit the request to "/api/v1/jobs"
    Then the response status should be 400
    And no job should be created
