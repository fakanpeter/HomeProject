CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    started_at TIMESTAMP,
    finished_at TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS task_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_task_audit_logs_task
    FOREIGN KEY (task_id)
    REFERENCES tasks (id)
    ON DELETE CASCADE
    );