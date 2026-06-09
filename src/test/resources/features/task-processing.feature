Feature: Task processing

  As a system
  I want to process created tasks asynchronously
  So that tasks move through their lifecycle automatically

  Scenario: Process a created task successfully
    Given a task exists with status "CREATED"
    When the task processing starts
    Then the task status should become "RUNNING"
    When the task processing completes
    Then the task status should become "COMPLETED"

  Scenario: Processing a task creates audit logs
    Given a task exists with status "CREATED"
    When the task is processed successfully
    Then an audit log should exist from "CREATED" to "RUNNING"
    And an audit log should exist from "RUNNING" to "COMPLETED"