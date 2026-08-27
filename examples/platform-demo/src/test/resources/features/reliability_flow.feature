Feature: EFE Reliability Flow (EFE-010)

  As the flow engine, I wrap event pipelines in reliability handling so
  transient failures are retried with backoff and permanent/exhausted
  failures are routed to the dead letter queue, all while capturing
  audit and wiretap observability.

  Background:
    Given the reliability demo flow is running
    And the reliability demo producer store is cleared

  Scenario: A transient processor failure is retried and then succeeds
    Given a reliability message "MSG-TRANSIENT-01"
    When the reliability demo flow processes the message
    Then the message should be marked processed
    And the message should be attempted 2 times
    And the dead letter queue should be empty
    And the reliability audit trail should record a successful outcome

  Scenario: A permanent business failure is routed straight to the DLQ
    Given a permanently failing reliability message "MSG-PERMANENT-01"
    When the reliability demo flow processes the message
    Then the message should not be processed
    And the dead letter queue should contain exactly 1 record
    And the reliability audit trail should record a DLQ outcome

  Scenario: Wiretap observability captures events passing through the flow
    Given a reliability message "MSG-WIRETAP-01"
    When the reliability demo flow processes the message
    Then the wiretap store should have recorded the message
