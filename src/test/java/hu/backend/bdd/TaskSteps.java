package hu.backend.bdd;

import hu.backend.dto.TaskRequest;
import hu.backend.dto.TaskResponse;
import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import hu.backend.repository.TaskRepository;
import hu.backend.service.TaskService;
import hu.backend.service.TaskStatusService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskSteps {

    private final TaskService taskService = BddApplicationContext.getBean(TaskService.class);
    private final TaskStatusService taskStatusService = BddApplicationContext.getBean(TaskStatusService.class);
    private final TaskRepository taskRepository = BddApplicationContext.getBean(TaskRepository.class);

    private final BddWorld world;

    public TaskSteps(BddWorld world) {
        this.world = world;
    }

    @Before
    public void beforeScenario() {
        taskRepository.deleteAll();
        world.reset();
    }

    @When("I create a task with title {string}")
    public void iCreateATaskWithTitle(String title) {
        try {
            world.taskResponse = taskService.create(new TaskRequest(title));
            world.taskId = world.taskResponse.id();
        } catch (Exception e) {
            world.exception = e;
        }
    }

    @Given("a task exists with status {string}")
    public void aTaskExistsWithStatus(String status) {
        Task task = Task.builder()
                .title("BDD task")
                .status(TaskStatus.valueOf(status))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Task savedTask = taskRepository.save(task);
        world.taskId = savedTask.getId();
    }

    @Given("a task exists with title {string} and status {string}")
    public void aTaskExistsWithTitleAndStatus(String title, String status) {
        Task task = Task.builder()
                .title(title)
                .status(TaskStatus.valueOf(status))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Task savedTask = taskRepository.save(task);
        world.taskId = savedTask.getId();
    }

    @When("I cancel the task")
    public void iCancelTheTask() {
        try {
            world.taskResponse = taskService.cancel(world.taskId);
        } catch (Exception e) {
            world.exception = e;
        }
    }

    @When("I request the task by id")
    public void iRequestTheTaskById() {
        try {
            world.taskResponse = taskService.findById(world.taskId);
        } catch (Exception e) {
            world.exception = e;
        }
    }

    @When("I request all tasks")
    public void iRequestAllTasks() {
        try {
            world.taskResponses = taskService.findAll();
        } catch (Exception e) {
            world.exception = e;
        }
    }

    @When("the task changes status from {string} to {string}")
    public void theTaskChangesStatusFromTo(String oldStatus, String newStatus) {
        Task task = taskRepository.findById(world.taskId).orElseThrow();

        assertEquals(TaskStatus.valueOf(oldStatus), task.getStatus());

        Task updatedTask = taskStatusService.changeStatus(task, TaskStatus.valueOf(newStatus));
        world.taskId = updatedTask.getId();
    }

    @When("the background processor tries to complete the task")
    public void theBackgroundProcessorTriesToCompleteTheTask() {
        try {
            Task completedOrSkipped = taskStatusService.completeIfRunning(world.taskId);
            world.taskResponse = TaskResponse.from(completedOrSkipped);
        } catch (Exception e) {
            world.exception = e;
        }
    }

    @When("the task processing starts")
    public void theTaskProcessingStarts() {
        Task task = taskRepository.findById(world.taskId).orElseThrow();

        Task updatedTask = taskStatusService.changeStatus(task, TaskStatus.RUNNING);

        world.taskId = updatedTask.getId();
        world.taskResponse = TaskResponse.from(updatedTask);
    }

    @When("the task processing completes")
    public void theTaskProcessingCompletes() {
        Task updatedTask = taskStatusService.completeIfRunning(world.taskId);

        world.taskId = updatedTask.getId();
        world.taskResponse = TaskResponse.from(updatedTask);
    }

    @When("the task is processed successfully")
    public void theTaskIsProcessedSuccessfully() {
        theTaskProcessingStarts();
        theTaskProcessingCompletes();
    }

    @Then("the task should exist")
    public void theTaskShouldExist() {
        assertNotNull(world.taskId);
        assertTrue(taskRepository.findById(world.taskId).isPresent());
    }

    @Then("the task should be returned")
    public void theTaskShouldBeReturned() {
        assertNull(world.exception);
        assertNotNull(world.taskResponse);
        assertEquals(world.taskId, world.taskResponse.id());
    }

    @Then("the task title should be {string}")
    public void theTaskTitleShouldBe(String expectedTitle) {
        assertNull(world.exception);
        assertNotNull(world.taskResponse);
        assertEquals(expectedTitle, world.taskResponse.title());
    }

    @Then("the task status should be {string}")
    public void theTaskStatusShouldBe(String expectedStatus) {
        assertNull(world.exception);

        Task task = taskRepository.findById(world.taskId).orElseThrow();
        assertEquals(TaskStatus.valueOf(expectedStatus), task.getStatus());
    }

    @Then("the task status should become {string}")
    public void theTaskStatusShouldBecome(String expectedStatus) {
        theTaskStatusShouldBe(expectedStatus);
    }

    @Then("the task status should remain {string}")
    public void theTaskStatusShouldRemain(String expectedStatus) {
        Task task = taskRepository.findById(world.taskId).orElseThrow();
        assertEquals(TaskStatus.valueOf(expectedStatus), task.getStatus());
    }

    @Then("the cancellation should fail")
    public void theCancellationShouldFail() {
        assertNotNull(world.exception);
    }

    @Then("the response should contain {int} tasks")
    public void theResponseShouldContainTasks(int expectedCount) {
        assertNull(world.exception);
        assertEquals(expectedCount, world.taskResponses.size());
    }
}