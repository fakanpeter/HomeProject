Feature: Task audit log

  As a user
  I want to see task status changes
  So that I can understand what happened to a task

  Scenario: Get audit logs for a processed task
    Given a task exists with status "CREATED"
    When the task changes status from "CREATED" to "RUNNING"
    And the task changes status from "RUNNING" to "COMPLETED"
    Then the task audit log should contain 2 entries
    And an audit log should exist from "CREATED" to "RUNNING"
    And an audit log should exist from "RUNNING" to "COMPLETED"

  Scenario: Get audit logs for a cancelled task
    Given a task exists with status "RUNNING"
    When I cancel the task
    Then the task audit log should contain 1 entry
    And an audit log should exist from "RUNNING" to "CANCELLED"