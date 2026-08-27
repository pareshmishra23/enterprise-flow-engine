Feature: EFE gRPC Job API

  Scenario: Submit a job through gRPC
    Given EFE gRPC is available
    When I submit a valid job using gRPC
    Then the gRPC response should contain a jobId
    And the job should be registered

  Scenario: Retrieve a job through gRPC
    Given a job has been registered
    When I request the job using gRPC
    Then the gRPC response should contain the expected jobId
