Feature: EFE Bounded Async Execution Flow

  Background:
    Given the EFE async execution flow is running
    And the async result store is empty

  Scenario: Execute scheduled async batch processing
    Given a scheduled trigger occurs with batch ID "B-2026-001"
    When the async flow processes the batch of 10 partitioned tasks
    Then all 10 tasks should complete asynchronously
    And the executor worker pool should report completed tasks
    And each completed task should contain a valid worker outcome

  Scenario: Verify bounded worker concurrency metrics
    Given the async executor worker pool is initialized
    When 10 tasks are submitted in parallel
    Then the active threads should not exceed the maximum pool size of 10
    And the task queue depth should remain within capacity
