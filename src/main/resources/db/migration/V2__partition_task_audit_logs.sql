ALTER TABLE task_audit_logs
    RENAME TO task_audit_logs_old;

ALTER INDEX task_audit_logs_pkey
    RENAME TO task_audit_logs_old_pkey;

CREATE TABLE task_audit_logs (
                                 id BIGINT NOT NULL,
                                 task_id BIGINT NOT NULL,
                                 old_status VARCHAR(50),
                                 new_status VARCHAR(50) NOT NULL,
                                 changed_at TIMESTAMP NOT NULL,
                                 CONSTRAINT pk_task_audit_logs PRIMARY KEY (id, changed_at),
                                 CONSTRAINT fk_task_audit_logs_task
                                     FOREIGN KEY (task_id)
                                         REFERENCES tasks (id)
                                         ON DELETE CASCADE
) PARTITION BY RANGE (changed_at);

CREATE TABLE task_audit_logs_2026_05
    PARTITION OF task_audit_logs
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');

CREATE TABLE task_audit_logs_2026_06
    PARTITION OF task_audit_logs
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');

CREATE TABLE task_audit_logs_2026_07
    PARTITION OF task_audit_logs
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');

CREATE TABLE task_audit_logs_default
    PARTITION OF task_audit_logs
    DEFAULT;

INSERT INTO task_audit_logs (
    id,
    task_id,
    old_status,
    new_status,
    changed_at
)
SELECT
    id,
    task_id,
    old_status,
    new_status,
    changed_at
FROM task_audit_logs_old;

CREATE SEQUENCE task_audit_logs_id_seq_new;

SELECT setval(
               'task_audit_logs_id_seq_new',
               COALESCE((SELECT MAX(id) FROM task_audit_logs), 0) + 1,
               false
       );

ALTER TABLE task_audit_logs
    ALTER COLUMN id SET DEFAULT nextval('task_audit_logs_id_seq_new');

CREATE INDEX idx_task_audit_logs_task_id
    ON task_audit_logs (task_id);

CREATE INDEX idx_task_audit_logs_changed_at
    ON task_audit_logs (changed_at);