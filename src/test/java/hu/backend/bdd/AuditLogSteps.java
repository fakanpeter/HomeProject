package hu.backend.bdd;

import hu.backend.model.TaskStatus;
import hu.backend.repository.TaskAuditLogRepository;
import io.cucumber.java.en.Then;

import static org.junit.jupiter.api.Assertions.*;

public class AuditLogSteps {

    private final TaskAuditLogRepository taskAuditLogRepository =
            BddApplicationContext.getBean(TaskAuditLogRepository.class);

    private final BddWorld world;

    public AuditLogSteps(BddWorld world) {
        this.world = world;
    }

    @Then("an audit log should exist from {string} to {string}")
    public void anAuditLogShouldExistFromTo(String oldStatus, String newStatus) {
        var logs = taskAuditLogRepository.findByTaskId(world.taskId);

        boolean exists = logs.stream().anyMatch(log ->
                log.getOldStatus() == TaskStatus.valueOf(oldStatus)
                        && log.getNewStatus() == TaskStatus.valueOf(newStatus)
        );

        assertTrue(exists);
    }

    @Then("an audit log should not exist from {string} to {string}")
    public void anAuditLogShouldNotExistFromTo(String oldStatus, String newStatus) {
        var logs = taskAuditLogRepository.findByTaskId(world.taskId);

        boolean exists = logs.stream().anyMatch(log ->
                log.getOldStatus() == TaskStatus.valueOf(oldStatus)
                        && log.getNewStatus() == TaskStatus.valueOf(newStatus)
        );

        assertFalse(exists);
    }

    @Then("the task audit log should contain {int} entries")
    public void theTaskAuditLogShouldContainEntries(int expectedCount) {
        var logs = taskAuditLogRepository.findByTaskId(world.taskId);
        assertEquals(expectedCount, logs.size());
    }

    @Then("the task audit log should contain {int} entry")
    public void theTaskAuditLogShouldContainEntry(int expectedCount) {
        var logs = taskAuditLogRepository.findByTaskId(world.taskId);

        assertEquals(expectedCount, logs.size());
    }
}