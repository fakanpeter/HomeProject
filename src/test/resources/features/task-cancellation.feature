Feature: Task cancellation

  As a user
  I want to cancel a task
  So that unnecessary work is not completed

  Scenario: Cancel a created task
    Given a task exists with status "CREATED"
    When I cancel the task
    Then the task status should be "CANCELLED"
    And an audit log should exist from "CREATED" to "CANCELLED"

  Scenario: Cancel a running task
    Given a task exists with status "RUNNING"
    When I cancel the task
    Then the task status should be "CANCELLED"
    And an audit log should exist from "RUNNING" to "CANCELLED"

  Scenario: Completed task cannot be cancelled
    Given a task exists with status "COMPLETED"
    When I cancel the task
    Then the cancellation should fail
    And the task status should remain "COMPLETED"

  Scenario: Cancelled task must not be completed later
    Given a task exists with status "RUNNING"
    When I cancel the task
    And the background processor tries to complete the task
    Then the task status should remain "CANCELLED"
    And an audit log should not exist from "RUNNING" to "COMPLETED"