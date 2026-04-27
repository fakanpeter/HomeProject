package hu.backend.observer;

import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Singleton
public class TaskStatusSubject {
    private final List<TaskObserver> observers = new ArrayList<>();
    private final Object lock = new Object();

    public void addObserver(TaskObserver observer) {
        synchronized (lock) {
            observers.add(observer);
        }
    }

    public void removeObserver(TaskObserver observer) {
        synchronized(lock) {
            observers.remove(observer);
        }
    }

    public void notifyObservers(Task task, TaskStatus oldStatus, TaskStatus newStatus) {
        List<TaskObserver> snapshot;

        synchronized (lock) {
            snapshot = new ArrayList<>(observers);
        }

        for (TaskObserver observer : snapshot) {
            try {
                observer.onTaskStatusChanged(task, oldStatus, newStatus);
            } catch (Exception e) {
                log.error("Observer failed: {}", observer.getClass().getSimpleName());
            }
        }
    }
}