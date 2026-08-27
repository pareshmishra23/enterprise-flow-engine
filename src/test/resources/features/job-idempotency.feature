Feature: EFE Job Idempotency

  Scenario: Duplicate submission with the same idempotency key
    Given I have a valid job request
    And the idempotency key is "IDEMP-001"
    When I submit the request to "/api/v1/jobs"
    Then the response status should be 201

    When I submit the same request again with idempotency key "IDEMP-001"
    Then the response should refer to the same jobId
    And only one job should exist for idempotency key "IDEMP-001"
