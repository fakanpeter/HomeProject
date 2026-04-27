package hu.backend.controller;

import hu.backend.dto.TaskRequest;
import hu.backend.dto.TaskAuditLogResponse;
import hu.backend.dto.TaskResponse;
import hu.backend.service.TaskService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import lombok.AllArgsConstructor;

import java.util.List;

@Controller("/tasks")
@AllArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @Post
    public HttpResponse<TaskResponse> create(@Body TaskRequest request) {
        TaskResponse response = taskService.create(request);
        return HttpResponse.created(response);
    }

    @Get("/{id}")
    public TaskResponse findById(Long id) {
        return taskService.findById(id);
    }

    @Get
    public List<TaskResponse> findAll() {
        return taskService.findAll();
    }

    @Put("/{id}")
    public TaskResponse update(Long id, @Body TaskRequest request) {
        return taskService.update(id, request);
    }

    @Delete("/{id}")
    public HttpResponse<Void> delete(Long id) {
        taskService.delete(id);
        return HttpResponse.ok();
    }

    @Delete
    public HttpResponse<Void> deleteAll() {
        taskService.deleteAll();
        return HttpResponse.ok();
    }

    @Get("/{id}/audit-logs")
    public List<TaskAuditLogResponse> findAuditLogs(Long id) {
        return taskService.findAuditLogsByTaskId(id);
    }
}
