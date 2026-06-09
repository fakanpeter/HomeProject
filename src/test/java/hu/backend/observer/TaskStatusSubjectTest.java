package hu.backend.observer;

import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskStatusSubjectTest {

    @Mock
    TaskObserver firstObserver;

    @Mock
    TaskObserver secondObserver;

    @Test
    void shouldNotifyRegisteredObserver() {
        TaskStatusSubject taskStatusSubject = new TaskStatusSubject();
        Task task = taskWithStatus(TaskStatus.RUNNING);

        taskStatusSubject.addObserver(firstObserver);

        taskStatusSubject.notifyObservers(
                task,
                TaskStatus.CREATED,
                TaskStatus.RUNNING
        );

        verify(firstObserver).onTaskStatusChanged(
                task,
                TaskStatus.CREATED,
                TaskStatus.RUNNING
        );
    }

    @Test
    void shouldNotifyMultipleRegisteredObservers() {
        TaskStatusSubject taskStatusSubject = new TaskStatusSubject();
        Task task = taskWithStatus(TaskStatus.CANCELLED);

        taskStatusSubject.addObserver(firstObserver);
        taskStatusSubject.addObserver(secondObserver);

        taskStatusSubject.notifyObservers(
                task,
                TaskStatus.RUNNING,
                TaskStatus.CANCELLED
        );

        verify(firstObserver).onTaskStatusChanged(
                task,
                TaskStatus.RUNNING,
                TaskStatus.CANCELLED
        );

        verify(secondObserver).onTaskStatusChanged(
                task,
                TaskStatus.RUNNING,
                TaskStatus.CANCELLED
        );
    }

    private Task taskWithStatus(TaskStatus status) {
        return Task.builder()
                .id(1L)
                .title("Subject test task")
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}