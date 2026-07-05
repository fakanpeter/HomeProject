package hu.backend.exception;

public class OptimisticLockingException extends RuntimeException {
    public OptimisticLockingException(Long taskId) {
        super("Task was modified concurrently: " + taskId);
    }
}