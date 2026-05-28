CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    public_id VARCHAR(64) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    role VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    avatar_file_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_public_id (public_id),
    UNIQUE KEY uk_users_email (email)
);

CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_ip VARCHAR(64) NULL,
    user_agent VARCHAR(512) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_hash (token_hash),
    KEY idx_refresh_tokens_user_id (user_id),
    KEY idx_refresh_tokens_expires_at (expires_at),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE password_reset_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    reset_token_hash VARCHAR(255) NULL,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP NULL,
    used_at TIMESTAMP NULL,
    created_ip VARCHAR(64) NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_password_reset_user_id (user_id),
    KEY idx_password_reset_email (email),
    KEY idx_password_reset_expires_at (expires_at),
    KEY idx_password_reset_token_hash (reset_token_hash),
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE workspaces (
    id BIGINT NOT NULL AUTO_INCREMENT,
    public_id VARCHAR(64) NOT NULL,
    name VARCHAR(160) NOT NULL,
    description VARCHAR(1000) NULL,
    visibility VARCHAR(32) NOT NULL DEFAULT 'PRIVATE',
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_workspaces_public_id (public_id),
    KEY idx_workspaces_owner_id (owner_id),
    CONSTRAINT fk_workspaces_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE TABLE workspace_members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    workspace_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    workspace_role VARCHAR(32) NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    invited_by BIGINT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_workspace_members_workspace_user (workspace_id, user_id),
    KEY idx_workspace_members_user_id (user_id),
    CONSTRAINT fk_workspace_members_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_workspace_members_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_workspace_members_invited_by FOREIGN KEY (invited_by) REFERENCES users (id)
);

CREATE TABLE tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    public_id VARCHAR(64) NOT NULL,
    workspace_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    assignee_id BIGINT NULL,
    created_by BIGINT NOT NULL,
    due_date DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tasks_public_id (public_id),
    KEY idx_tasks_workspace_id (workspace_id),
    KEY idx_tasks_assignee_id (assignee_id),
    KEY idx_tasks_created_by (created_by),
    CONSTRAINT fk_tasks_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_tasks_assignee FOREIGN KEY (assignee_id) REFERENCES users (id),
    CONSTRAINT fk_tasks_created_by FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    public_id VARCHAR(64) NOT NULL,
    task_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    body TEXT NOT NULL,
    body_sanitized TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_comments_public_id (public_id),
    KEY idx_comments_task_id (task_id),
    KEY idx_comments_author_id (author_id),
    CONSTRAINT fk_comments_task FOREIGN KEY (task_id) REFERENCES tasks (id),
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users (id)
);

CREATE TABLE files (
    id BIGINT NOT NULL AUTO_INCREMENT,
    public_id VARCHAR(64) NOT NULL,
    owner_id BIGINT NOT NULL,
    workspace_id BIGINT NULL,
    task_id BIGINT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(160) NOT NULL,
    size_bytes BIGINT NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    sha256 VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_files_public_id (public_id),
    UNIQUE KEY uk_files_stored_filename (stored_filename),
    KEY idx_files_owner_id (owner_id),
    KEY idx_files_workspace_id (workspace_id),
    KEY idx_files_task_id (task_id),
    CONSTRAINT fk_files_owner FOREIGN KEY (owner_id) REFERENCES users (id),
    CONSTRAINT fk_files_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_files_task FOREIGN KEY (task_id) REFERENCES tasks (id)
);

ALTER TABLE users
    ADD CONSTRAINT fk_users_avatar_file FOREIGN KEY (avatar_file_id) REFERENCES files (id);

CREATE TABLE user_activities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    activity_type VARCHAR(80) NOT NULL,
    resource_type VARCHAR(80) NULL,
    resource_id VARCHAR(80) NULL,
    summary VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_activities_user_id (user_id),
    CONSTRAINT fk_user_activities_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE challenges (
    id BIGINT NOT NULL AUTO_INCREMENT,
    challenge_key VARCHAR(120) NOT NULL,
    title VARCHAR(180) NOT NULL,
    category VARCHAR(120) NOT NULL,
    difficulty VARCHAR(40) NOT NULL,
    summary VARCHAR(1000) NOT NULL,
    affected_endpoint VARCHAR(255) NOT NULL,
    points INT NOT NULL DEFAULT 100,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_challenges_key (challenge_key)
);

CREATE TABLE flags (
    id BIGINT NOT NULL AUTO_INCREMENT,
    challenge_key VARCHAR(120) NOT NULL,
    flag_value VARCHAR(255) NOT NULL,
    storage_location VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_flags_challenge_active (challenge_key, active)
);

CREATE TABLE solved_challenges (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    challenge_id BIGINT NOT NULL,
    submitted_flag_hash VARCHAR(255) NOT NULL,
    solved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    attempt_count INT NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_solved_challenges_user_challenge (user_id, challenge_id),
    CONSTRAINT fk_solved_challenges_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_solved_challenges_challenge FOREIGN KEY (challenge_id) REFERENCES challenges (id)
);

CREATE TABLE xss_collected_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NULL,
    task_id BIGINT NULL,
    payload_preview VARCHAR(500) NULL,
    captured_value VARCHAR(1000) NULL,
    request_ip VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_xss_events_user_id (user_id),
    KEY idx_xss_events_task_id (task_id),
    CONSTRAINT fk_xss_events_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_xss_events_task FOREIGN KEY (task_id) REFERENCES tasks (id)
);

CREATE TABLE audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    actor_user_id BIGINT NULL,
    event_type VARCHAR(120) NOT NULL,
    resource_type VARCHAR(120) NULL,
    resource_id VARCHAR(120) NULL,
    result VARCHAR(40) NOT NULL,
    ip_address VARCHAR(64) NULL,
    user_agent VARCHAR(512) NULL,
    metadata_json JSON NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_audit_logs_actor_user_id (actor_user_id),
    KEY idx_audit_logs_event_type (event_type),
    KEY idx_audit_logs_created_at (created_at),
    CONSTRAINT fk_audit_logs_actor FOREIGN KEY (actor_user_id) REFERENCES users (id)
);

CREATE TABLE lab_settings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    setting_key VARCHAR(120) NOT NULL,
    setting_value VARCHAR(1000) NOT NULL,
    environment VARCHAR(40) NOT NULL,
    public_visible BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_lab_settings_key_environment (setting_key, environment)
);
