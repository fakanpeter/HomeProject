package hu.backend.bdd;

import hu.backend.dto.TaskAuditLogResponse;
import hu.backend.dto.TaskResponse;

import java.util.ArrayList;
import java.util.List;

public class BddWorld {

    public Long taskId;
    public TaskResponse taskResponse;
    public List<TaskResponse> taskResponses;
    public List<TaskAuditLogResponse> auditLogs;
    public Exception exception;

    public void reset() {
        taskId = null;
        taskResponse = null;
        taskResponses = new ArrayList<>();
        auditLogs = new ArrayList<>();
        exception = null;
    }
}