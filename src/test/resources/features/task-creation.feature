Feature: Task creation

  As a user
  I want to create tasks
  So that they can be processed by the system

  Scenario: Create a task
    When I create a task with title "Test task"
    Then the task should exist
    And the task title should be "Test task"
    And the task status should be "CREATED"