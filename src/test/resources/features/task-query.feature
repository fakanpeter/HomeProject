Feature: Task query

  As a user
  I want to query tasks
  So that I can inspect their state

  Scenario: Find task by id
    Given a task exists with status "CREATED"
    When I request the task by id
    Then the task should be returned
    And the task status should be "CREATED"

  Scenario: Find all tasks
    Given a task exists with title "First task" and status "CREATED"
    And a task exists with title "Second task" and status "COMPLETED"
    When I request all tasks
    Then the response should contain 2 tasks