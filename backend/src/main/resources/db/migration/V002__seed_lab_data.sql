INSERT INTO users (id, public_id, email, password_hash, display_name, role, enabled) VALUES
(1, 'usr-admin-000000000000000000000000001', 'admin@test.com', '$2a$10$PYFdZygBOMvvEay7j4NZ8.5QAUfBSgUiU8AT1u94uTrCTIy1nQef.', 'Admin User', 'ADMIN', TRUE),
(2, 'usr-alice-000000000000000000000000001', 'alice@test.com', '$2a$10$mWSv/m4G/X5tmSqChL7lae8fh.kfrspSZ.kg2Df7eAlGVeP2tm1ka', 'Alice Nguyen', 'USER', TRUE),
(3, 'usr-bob-0000000000000000000000000001', 'bob@test.com', '$2a$10$mWSv/m4G/X5tmSqChL7lae8fh.kfrspSZ.kg2Df7eAlGVeP2tm1ka', 'Bob Tran', 'USER', TRUE),
(4, 'usr-charlie-00000000000000000000001', 'charlie@test.com', '$2a$10$mWSv/m4G/X5tmSqChL7lae8fh.kfrspSZ.kg2Df7eAlGVeP2tm1ka', 'Charlie Pham', 'USER', TRUE);

INSERT INTO workspaces (id, public_id, name, description, visibility, owner_id) VALUES
(1, 'wks-public-engineering-000000000001', 'Public Engineering', 'Shared engineering workspace for product delivery.', 'PUBLIC', 2),
(2, 'wks-mobile-team-000000000000000001', 'Mobile Team', 'Mobile app planning and release coordination.', 'PRIVATE', 3),
(3, 'wks-finance-internal-000000000001', 'Finance Internal', 'Sensitive payroll and finance operations.', 'PRIVATE', 2),
(4, 'wks-admin-operations-000000000001', 'Admin Operations', 'Administrative workspace for platform operators.', 'PRIVATE', 1),
(5, 'wks-deployment-lab-000000000001', 'Deployment Lab', 'Deployment notes and lab configuration exercises.', 'PRIVATE', 1);

INSERT INTO workspace_members (workspace_id, user_id, workspace_role, invited_by) VALUES
(1, 2, 'OWNER', NULL),
(1, 3, 'MEMBER', 2),
(1, 4, 'VIEWER', 2),
(2, 3, 'OWNER', NULL),
(2, 2, 'MEMBER', 3),
(3, 2, 'OWNER', NULL),
(3, 1, 'MEMBER', 2),
(4, 1, 'OWNER', NULL),
(5, 1, 'OWNER', NULL),
(5, 2, 'MEMBER', 1);

INSERT INTO tasks (id, public_id, workspace_id, title, description, status, priority, assignee_id, created_by, due_date) VALUES
(1, 'tsk-setup-ci-pipeline-000000000001', 1, 'Setup CI pipeline', 'Create backend and frontend checks for pull requests.', 'TODO', 'HIGH', 2, 2, DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY)),
(2, 'tsk-review-docker-config-000000001', 1, 'Review Docker config', 'Review Compose defaults and ensure internal services are not public.', 'IN_PROGRESS', 'HIGH', 3, 2, DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY)),
(3, 'tsk-fix-login-ui-000000000000001', 1, 'Fix login UI', 'Polish auth form validation and error messages.', 'REVIEW', 'MEDIUM', 4, 2, DATE_ADD(CURRENT_DATE, INTERVAL 10 DAY)),
(4, 'tsk-import-partner-tasks-00000001', 2, 'Import partner tasks', 'Import partner task feed from a URL for planning.', 'TODO', 'MEDIUM', 3, 3, DATE_ADD(CURRENT_DATE, INTERVAL 12 DAY)),
(5, 'tsk-private-payroll-review-00001', 3, 'Private payroll review', 'Review payroll export and restrict access to finance members only.', 'TODO', 'URGENT', 2, 2, DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY)),
(6, 'tsk-deployment-checklist-000001', 5, 'Deployment checklist', 'Verify HTTPS, reverse proxy rules, and no public internal services.', 'IN_PROGRESS', 'HIGH', 1, 1, DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY)),
(7, 'tsk-serialized-import-investigation', 5, 'Serialized import investigation', 'Review old serialized profile import feature before release.', 'TODO', 'MEDIUM', 1, 1, DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY));

INSERT INTO comments (public_id, task_id, author_id, body, body_sanitized) VALUES
('cmt-ci-0000000000000000000000000001', 1, 2, 'Please keep this pipeline simple until the API contract is stable.', 'Please keep this pipeline simple until the API contract is stable.'),
('cmt-docker-000000000000000000001', 2, 3, 'Redis and MailHog should stay local-only for now.', 'Redis and MailHog should stay local-only for now.'),
('cmt-payroll-00000000000000000001', 5, 2, 'Finance files must not appear in public workspace search results.', 'Finance files must not appear in public workspace search results.'),
('cmt-deploy-000000000000000000001', 6, 1, 'Double-check that backup files are not served from web root.', 'Double-check that backup files are not served from web root.');

INSERT INTO files (id, public_id, owner_id, workspace_id, task_id, original_filename, stored_filename, content_type, size_bytes, storage_path, sha256) VALUES
(1, 'fil-public-report-000000000000001', 2, 1, NULL, 'public-report.txt', 'seed-public-report.txt', 'text/plain', 128, './uploads/seed-public-report.txt', NULL),
(2, 'fil-private-finance-note-0000001', 2, 3, 5, 'private-finance-note.txt', 'seed-private-finance-note.txt', 'text/plain', 256, './uploads/private/seed-private-finance-note.txt', NULL),
(3, 'fil-deployment-backup-env-bak', 1, 5, 6, 'deployment-backup.env.bak', 'seed-deployment-backup.env.bak', 'text/plain', 256, './uploads/lab/seed-deployment-backup.env.bak', NULL),
(4, 'fil-serialized-sample-bin-00001', 1, 5, 7, 'serialized-sample.bin', 'seed-serialized-sample.bin', 'application/octet-stream', 512, './uploads/lab/seed-serialized-sample.bin', NULL);

INSERT INTO user_activities (user_id, activity_type, resource_type, resource_id, summary) VALUES
(1, 'ADMIN_LOGIN', 'USER', 'usr-admin-000000000000000000000000001', 'Admin seed login event for audit examples.'),
(2, 'TASK_CREATED', 'TASK', 'tsk-setup-ci-pipeline-000000000001', 'Alice created Setup CI pipeline.'),
(3, 'COMMENT_CREATED', 'TASK', 'tsk-review-docker-config-000000001', 'Bob commented on Docker configuration.'),
(2, 'FILE_UPLOADED', 'FILE', 'fil-public-report-000000000000001', 'Alice uploaded public-report.txt.');

INSERT INTO challenges (id, challenge_key, title, category, difficulty, summary, affected_endpoint, points, enabled, sort_order) VALUES
(1, 'broken-access-control-admin-door', 'Admin Door Without Guard', 'Broken Access Control', 'Medium', 'Admin API checks authentication but misses ADMIN role in vulnerable mode.', 'GET /api/admin/users', 100, TRUE, 1),
(2, 'sqli-search', 'Search Is Not Just Search', 'Injection', 'Medium', 'Task search uses unsafe SQL construction in vulnerable mode.', 'GET /api/tasks/search?keyword=', 120, TRUE, 2),
(3, 'idor-workspace', 'Private Workspace Leak', 'Broken Access Control', 'Medium', 'Workspace detail lookup misses membership verification in vulnerable mode.', 'GET /api/workspaces/{publicId}', 100, TRUE, 3),
(4, 'mass-assignment-profile', 'Profile Privilege Upgrade', 'Mass Assignment', 'Medium', 'Profile update maps arbitrary fields into the user entity in vulnerable mode.', 'PATCH /api/users/me', 120, TRUE, 4),
(5, 'stored-xss-comments', 'Dangerous Comments', 'Cross-Site Scripting', 'Medium', 'Task comments are stored and rendered unsafely in vulnerable mode.', 'POST /api/tasks/{taskId}/comments', 120, TRUE, 5),
(6, 'file-upload-bypass', 'Avatar Confusion', 'File Upload', 'Medium', 'Avatar upload trusts filename or client content type in vulnerable mode.', 'POST /api/users/me/avatar', 120, TRUE, 6),
(7, 'path-traversal-download', 'Lost In Path', 'Path Traversal', 'Medium', 'File download concatenates upload path with user input in vulnerable mode.', 'GET /api/files/download?name=', 120, TRUE, 7),
(8, 'ssrf-task-import', 'Import From Inside', 'SSRF', 'Medium-Hard', 'Task import fetches arbitrary URLs and can reach an internal service in vulnerable mode.', 'POST /api/tasks/import', 150, TRUE, 8),
(9, 'jwt-weak-secret', 'Secret In The Token', 'Authentication', 'Medium-Hard', 'JWT validation uses a weak secret and trusts token role claims in vulnerable mode.', 'GET /api/admin/jwt-flag', 150, TRUE, 9),
(10, 'reset-password-logic-flaw', 'Reset Confusion', 'Identification and Authentication Failures', 'Medium', 'Reset token is not bound to the requesting user in vulnerable mode.', 'POST /api/auth/reset-password', 120, TRUE, 10),
(11, 'java-deserialization', 'Serialized Trust', 'Insecure Deserialization', 'Medium-Hard', 'Lab endpoint deserializes untrusted Java objects in vulnerable mode.', 'POST /api/lab/deserialization/import-profile', 150, TRUE, 11),
(12, 'deployment-leftovers', 'Deployment Leftovers', 'Security Misconfiguration', 'Medium', 'Lab Nginx config exposes a fake backup file in vulnerable mode.', 'GET /backup/.env.bak', 100, TRUE, 12),
(13, 'security-logging-alerting', 'Silent Attack', 'Security Logging and Monitoring Failures', 'Medium', 'Important security events are missing from logs in vulnerable mode.', 'Security event coverage', 100, TRUE, 13);

INSERT INTO flags (challenge_key, flag_value, storage_location, active) VALUES
('broken-access-control-admin-door', 'FLAG{admin_api_missing_role_check}', 'admin_api', TRUE),
('sqli-search', 'FLAG{sqli_union_search_task}', 'database', TRUE),
('idor-workspace', 'FLAG{idor_workspace_access}', 'workspace', TRUE),
('mass-assignment-profile', 'FLAG{mass_assignment_to_admin}', 'admin_api', TRUE),
('stored-xss-comments', 'FLAG{stored_xss_comment}', 'xss_collector', TRUE),
('file-upload-bypass', 'FLAG{file_upload_bypass}', 'upload_lab', TRUE),
('path-traversal-download', 'FLAG{path_traversal_download}', 'file', TRUE),
('ssrf-task-import', 'FLAG{ssrf_internal_service}', 'internal_api', TRUE),
('jwt-weak-secret', 'FLAG{weak_jwt_secret}', 'jwt_admin_endpoint', TRUE),
('reset-password-logic-flaw', 'FLAG{reset_token_not_bound_to_user}', 'reset_flow', TRUE),
('java-deserialization', 'FLAG{java_deserialization_trusted_untrusted_data}', 'deserialization_lab', TRUE),
('deployment-leftovers', 'FLAG{deployment_backup_file_exposed}', 'lab_nginx_backup', TRUE),
('security-logging-alerting', 'FLAG{missing_security_logging_alerting}', 'audit_lab', TRUE);

INSERT INTO audit_logs (actor_user_id, event_type, resource_type, resource_id, result, ip_address, user_agent, metadata_json) VALUES
(1, 'ADMIN_ACCESS', 'ADMIN_PANEL', 'seed', 'SUCCESS', '127.0.0.1', 'seed', JSON_OBJECT('source', 'seed-data')),
(2, 'LOGIN_SUCCESS', 'USER', 'usr-alice-000000000000000000000000001', 'SUCCESS', '127.0.0.1', 'seed', JSON_OBJECT('source', 'seed-data'));

INSERT INTO lab_settings (setting_key, setting_value, environment, public_visible) VALUES
('lab_warning_enabled', 'true', 'local', TRUE),
('fake_backup_path', '/backup/.env.bak', 'lab', TRUE),
('internal_api_flag_url', 'http://internal-api:9090/flag', 'local', FALSE),
('prod_secure_mode_required', 'true', 'prod', TRUE);
