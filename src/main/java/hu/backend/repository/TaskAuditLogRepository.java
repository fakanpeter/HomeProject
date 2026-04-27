package hu.backend.repository;

import hu.backend.model.TaskAuditLog;
import hu.backend.model.TaskStatus;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Singleton
@AllArgsConstructor
public class TaskAuditLogRepository {
    private final DataSource dataSource;

    public void save(TaskAuditLog auditLog) {
        String sql = """
                INSERT INTO task_audit_logs (task_id, old_status, new_status, changed_at)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, auditLog.getTaskId());
            statement.setString(2, auditLog.getOldStatus().name());
            statement.setString(3, auditLog.getNewStatus().name());
            statement.setTimestamp(4, Timestamp.valueOf(auditLog.getChangedAt()));
            statement.executeUpdate();

        } catch (SQLException e) {
            log.error("Database error while saving audit log.", e);
        }
    }

    public List<TaskAuditLog> findByTaskId(Long taskId) {
        String sql = """
                SELECT id, task_id, old_status, new_status, changed_at
                FROM task_audit_logs
                WHERE task_id = ?
                ORDER BY changed_at
                """;

        List<TaskAuditLog> logs = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, taskId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(mapLog(resultSet));
                }
            }
            return logs;

        } catch (SQLException e) {
            throw new RuntimeException("Database error while finding audit logs.", e);
        }
    }

    private TaskAuditLog mapLog(ResultSet resultSet) throws SQLException {
        String oldStatus = resultSet.getString("old_status");

        return TaskAuditLog.builder()
                .id(resultSet.getLong("id"))
                .taskId(resultSet.getLong("task_id"))
                .oldStatus(oldStatus == null ? null : TaskStatus.valueOf(oldStatus))
                .newStatus(TaskStatus.valueOf(resultSet.getString("new_status")))
                .changedAt(resultSet.getTimestamp("changed_at").toLocalDateTime())
                .build();
    }
}