package hu.backend.repository;

import hu.backend.model.Task;
import hu.backend.model.TaskStatus;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
@AllArgsConstructor
public class TaskRepository {
    private final DataSource dataSource;

    public Task save(Task task) {
        String sql = """
                INSERT INTO tasks (title, status, created_at)
                VALUES (?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, task.getTitle());
            statement.setString(2, task.getStatus().name());
            statement.setTimestamp(3, Timestamp.valueOf(task.getCreatedAt()));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    task.setId(resultSet.getLong("id"));
                    return task;
                }
            }
            throw new RuntimeException("Task save failed.");

        } catch (SQLException e) {
            throw new RuntimeException("Database error while saving task.", e);
        }
    }

    public Optional<Task> findById(Long id) {
        String sql = """
                SELECT
                    id,
                    title,
                    status,
                    created_at,
                    updated_at,
                    started_at,
                    finished_at
                FROM tasks
                WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapTask(resultSet));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Database error while finding task by id.", e);
        }
    }

    public List<Task> findAll() {
        String sql = """
                SELECT
                    id,
                    title,
                    status,
                    created_at,
                    updated_at,
                    started_at,
                    finished_at
                FROM tasks
                ORDER BY id
                """;

        List<Task> tasks = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                tasks.add(mapTask(resultSet));
            }
            return tasks;

        } catch (SQLException e) {
            throw new RuntimeException("Database error while finding all tasks.", e);
        }
    }

    public Task update(Task task) {
        String sql = """
                UPDATE tasks
                SET title = ?,
                    status = ?,
                    updated_at = ?,
                    started_at = ?,
                    finished_at = ?
                WHERE id = ?
                """;

        task.setUpdatedAt(LocalDateTime.now());

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, task.getTitle());
            statement.setString(2, task.getStatus().name());
            statement.setTimestamp(3, Timestamp.valueOf(task.getUpdatedAt()));
            setNullableTimestamp(statement, 4, task.getStartedAt());
            setNullableTimestamp(statement, 5, task.getFinishedAt());
            statement.setLong(6, task.getId());

            int updatedRows = statement.executeUpdate();

            if (updatedRows == 0) {
                throw new RuntimeException("Task not found with id: " + task.getId());
            }
            return task;

        } catch (SQLException e) {
            throw new RuntimeException("Database error while updating task.", e);
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            int deletedRows = statement.executeUpdate();

            if (deletedRows == 0) {
                throw new RuntimeException("Task not found with id: " + id);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database error while deleting task.", e);
        }
    }

    public void deleteAll() {
        String sql = "DELETE FROM tasks";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error while deleting tasks.");
        }
    }

    private Task mapTask(ResultSet resultSet) throws SQLException {
        return Task.builder()
                .id(resultSet.getLong("id"))
                .title(resultSet.getString("title"))
                .status(TaskStatus.valueOf(resultSet.getString("status")))
                .createdAt(toLocalDateTime(resultSet.getTimestamp("created_at")))
                .updatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")))
                .startedAt(toLocalDateTime(resultSet.getTimestamp("started_at")))
                .finishedAt(toLocalDateTime(resultSet.getTimestamp("finished_at")))
                .build();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private void setNullableTimestamp(
            PreparedStatement statement,
            int index,
            LocalDateTime value
    ) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.TIMESTAMP);
        } else {
            statement.setTimestamp(index, Timestamp.valueOf(value));
        }
    }
}