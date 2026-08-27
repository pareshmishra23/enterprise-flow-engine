Feature: EFE GraphQL API

  Scenario: Query a job
    Given a job has been registered
    When I query the job through GraphQL
    Then the GraphQL response should contain the jobId
    And the GraphQL response should contain the job status

  Scenario: Query tasks and results through GraphQL
    Given a job has been registered
    When I query the tasks and results through GraphQL
    Then the GraphQL response should contain the task and result fields
