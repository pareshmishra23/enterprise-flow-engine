Feature: EFE JMX Management

  Scenario: Read EFE flow status through JMX
    Given EFE JMX management is enabled
    When I read the EFE module status through JMX
    Then the module status should be available

  Scenario: Read executor metrics through JMX
    Given the async demo flow is running
    When I query executor metrics through JMX
    Then the active worker count should be available
    And the completed task count should be available
