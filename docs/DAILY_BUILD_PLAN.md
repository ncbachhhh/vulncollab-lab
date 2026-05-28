# VulnCollab Lab Daily Build Plan

VulnCollab Lab is a self-built vulnerable collaboration platform for legal, controlled web security training. It should feel like a real team collaboration app, not a collection of disconnected vulnerable endpoints. The product combines a mini Slack/Trello/Jira-style application, seeded business data, a challenge board, vulnerable and secure execution modes, writeups, and deployment guidance for an owner-controlled lab domain.

This document is the implementation plan only. It intentionally does not contain application source code.

## Project Overview

VulnCollab Lab is a full-stack training platform where learners register, log in, join workspaces, manage tasks, comment, upload files, import tasks from URLs, and solve security challenges. The normal application must remain useful even without the challenge board, because realistic business logic makes the vulnerabilities easier to understand.

Primary goals:

- Build a realistic Spring Boot 3.x backend with JWT authentication, refresh tokens, password reset, workspace membership, task board, comments, file handling, admin APIs, challenge tracking, and lab-only vulnerable routes.
- Build a React/Vite/TypeScript frontend after the backend API contract is stable.
- Provide vulnerable mode and secure mode for each challenge.
- Seed realistic users, workspaces, tasks, files, flags, and activity logs.
- Deploy safely to an owner-controlled domain with secure mode defaulted and lab mode access-restricted.
- Produce portfolio-quality documentation, screenshots, writeups, and deployment notes.

## Architecture Overview

Repository target structure:

```text
vulncollab-lab/
├── backend/
│   ├── src/main/java/com/vulncollab/
│   │   ├── auth/
│   │   ├── user/
│   │   ├── workspace/
│   │   ├── task/
│   │   ├── comment/
│   │   ├── file/
│   │   ├── admin/
│   │   ├── challenge/
│   │   ├── lab/
│   │   │   ├── sqli/
│   │   │   ├── idor/
│   │   │   ├── xss/
│   │   │   ├── upload/
│   │   │   ├── traversal/
│   │   │   ├── ssrf/
│   │   │   ├── jwt/
│   │   │   ├── deserialization/
│   │   │   ├── deploy/
│   │   │   └── logging/
│   │   ├── security/
│   │   ├── config/
│   │   └── common/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-local.yml
│   │   ├── application-prod.yml
│   │   ├── db/migration/
│   │   └── seed/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── routes/
│   │   ├── stores/
│   │   ├── types/
│   │   └── utils/
│   ├── Dockerfile
│   └── package.json
├── internal-api/
│   ├── src/
│   ├── Dockerfile
│   └── package.json or pom.xml
├── nginx/
│   ├── local.conf
│   ├── prod.conf
│   └── lab.conf
├── docs/
│   ├── DAILY_BUILD_PLAN.md
│   ├── API_DESIGN.md
│   ├── DATABASE_DESIGN.md
│   ├── CHALLENGE_DESIGN.md
│   ├── DEPLOYMENT_GUIDE.md
│   ├── SECURITY_MODEL.md
│   └── TESTING_CHECKLIST.md
├── WRITEUPS/
│   ├── 01-broken-access-control.md
│   ├── 02-sqli-search.md
│   ├── 03-idor-workspace.md
│   ├── 04-stored-xss.md
│   ├── 05-file-upload-bypass.md
│   ├── 06-path-traversal.md
│   ├── 07-ssrf.md
│   ├── 08-jwt-weak-secret.md
│   ├── 09-reset-password-logic-flaw.md
│   ├── 10-java-deserialization.md
│   ├── 11-deploy-misconfiguration.md
│   └── 12-security-logging-alerting.md
├── docker-compose.yml
├── docker-compose.prod.yml
├── docker-compose.lab.yml
├── .env.example
├── README.md
└── LICENSE
```

Runtime components:

- `frontend`: React application served by Vite in development and Nginx in production.
- `backend`: Spring Boot API server.
- `mysql`: MySQL 8 database.
- `redis`: refresh token/password reset helper storage where useful.
- `mailhog`: local email capture for OTP flows.
- `internal-api`: private Docker-network-only service used by the SSRF lab.
- `nginx`: reverse proxy for frontend, backend, lab profile, HTTPS, and deployment challenge simulation.

## Backend-First Strategy

Build backend modules before frontend screens. The frontend should consume a frozen API contract instead of driving backend shape ad hoc.

Backend milestones:

- Days 1-5: infrastructure, project skeleton, auth, JWT, refresh tokens.
- Days 6-12: real collaboration app domain modules.
- Days 13-18: vulnerable lab behavior.
- Day 19: secure-mode implementations.
- Day 20: API contract, backend docs, exploit validation baseline.

Each vulnerable feature must be implemented behind a clear mode switch:

- `app.vulnerable-mode=true`: enables intentionally vulnerable lab behavior.
- `app.vulnerable-mode=false`: enables secure behavior.

Production must default to secure mode. Local and protected lab deployments may enable vulnerable mode.

## Frontend Strategy

Frontend development begins after backend APIs are stable. The frontend must look like a real collaboration product, not a set of security test forms.

Frontend priorities:

- Auth and protected routing.
- Workspace dashboard.
- Task board with comments and file attachments.
- Profile and avatar management.
- Admin panel.
- Challenge board with progress tracking, hints, flag submission, and writeup links.
- Lab mode banners and secure mode indicators.

The frontend may expose vulnerable behavior only when that behavior is part of a challenge. For example, stored XSS rendering may be toggled by lab mode, but mass-assignment abuse should be explained in writeups and tested with request manipulation, not with a visible "become admin" button.

## Database Strategy

Use Flyway or Liquibase. Flyway is recommended for simplicity:

- `V001__create_users.sql`
- `V002__create_auth_tokens.sql`
- `V003__create_workspaces.sql`
- `V004__create_tasks_comments_files.sql`
- `V005__create_challenges.sql`
- `V006__create_audit_and_lab_tables.sql`
- `V007__seed_initial_data.sql`

Use UUID or public IDs for externally visible resources. Keep numeric primary keys internal. The IDOR challenge should use `publicId` but intentionally omit authorization in vulnerable mode.

Seed accounts:

- `admin@test.com` / `Admin123!`
- `alice@test.com` / `Password123!`
- `bob@test.com` / `Password123!`
- `charlie@test.com` / `Password123!`

Seed workspaces:

- Public Engineering
- Mobile Team
- Finance Internal
- Admin Operations
- Deployment Lab

Seed tasks:

- Setup CI pipeline
- Review Docker config
- Fix login UI
- Import partner tasks
- Private payroll review
- Deployment checklist
- Serialized import investigation

Seed files:

- `public-report.txt`
- `private-finance-note.txt`
- `deployment-backup.env.bak`
- `serialized-sample.bin`

Seed flags:

- One flag per challenge, stored in `flags` and referenced by `challenges`.

## Table Design

### users

- Purpose: application identities, login credentials, global roles, profiles, avatar references.
- Key fields: `id`, `public_id`, `email`, `password_hash`, `display_name`, `role`, `enabled`, `avatar_file_id`, `created_at`, `updated_at`.
- Relationships: one user has many refresh tokens, reset tokens, workspace memberships, tasks, comments, files, solved challenges, audit logs.
- Seed data: admin, alice, bob, charlie.
- Challenge dependency: Broken Access Control, Mass Assignment, Weak JWT Secret, Reset Password Logic Flaw, Security Logging.

### refresh_tokens

- Purpose: long-lived refresh token records and logout invalidation.
- Key fields: `id`, `user_id`, `token_hash`, `expires_at`, `revoked_at`, `created_at`, `created_ip`, `user_agent`.
- Relationships: many refresh tokens belong to one user.
- Seed data: none required.
- Challenge dependency: auth foundation, weak token trust comparison.

### password_reset_tokens

- Purpose: forgot-password OTP and reset verification state.
- Key fields: `id`, `user_id`, `email`, `otp_hash`, `reset_token_hash`, `expires_at`, `verified_at`, `used_at`, `created_ip`, `attempt_count`.
- Relationships: many reset tokens belong to one user.
- Seed data: none required.
- Challenge dependency: Reset Password Logic Flaw.

### workspaces

- Purpose: collaboration containers.
- Key fields: `id`, `public_id`, `name`, `description`, `visibility`, `owner_id`, `created_at`, `updated_at`.
- Relationships: one workspace has many members, tasks, files.
- Seed data: Public Engineering, Mobile Team, Finance Internal, Admin Operations, Deployment Lab.
- Challenge dependency: IDOR Workspace, Broken Access Control, Security Logging.

### workspace_members

- Purpose: membership and workspace-level role authorization.
- Key fields: `id`, `workspace_id`, `user_id`, `workspace_role`, `joined_at`, `invited_by`.
- Relationships: joins users to workspaces.
- Seed data: admin owns Admin Operations and Deployment Lab; alice owns Public Engineering; bob is Mobile Team member; charlie is viewer in Public Engineering; Finance Internal limited to admin and alice.
- Challenge dependency: IDOR Workspace, task access control.

### tasks

- Purpose: task board cards.
- Key fields: `id`, `public_id`, `workspace_id`, `title`, `description`, `status`, `priority`, `assignee_id`, `created_by`, `due_date`, `created_at`, `updated_at`.
- Relationships: task belongs to workspace and has comments/files.
- Seed data: listed seed tasks, with Private payroll review in Finance Internal and Serialized import investigation in Deployment Lab.
- Challenge dependency: SQL Injection, Stored XSS context, SSRF task import.

### comments

- Purpose: task discussion.
- Key fields: `id`, `public_id`, `task_id`, `author_id`, `body`, `body_sanitized`, `created_at`, `updated_at`.
- Relationships: many comments per task.
- Seed data: realistic engineering comments, one harmless HTML-looking example.
- Challenge dependency: Stored XSS.

### files

- Purpose: uploaded attachments and avatars.
- Key fields: `id`, `public_id`, `owner_id`, `workspace_id`, `task_id`, `original_filename`, `stored_filename`, `content_type`, `size_bytes`, `storage_path`, `sha256`, `created_at`.
- Relationships: files can belong to users, workspaces, or tasks.
- Seed data: public report, private finance note, fake deployment backup, serialized sample.
- Challenge dependency: File Upload Bypass, Path Traversal, Deployment Misconfiguration.

### user_activities

- Purpose: profile activity feed and realistic audit-adjacent events.
- Key fields: `id`, `user_id`, `activity_type`, `resource_type`, `resource_id`, `summary`, `created_at`.
- Relationships: activities belong to users.
- Seed data: login, task create, comment create, file upload samples.
- Challenge dependency: user activity endpoint, logging lab comparison.

### flags

- Purpose: server-side flag values.
- Key fields: `id`, `challenge_key`, `flag_value`, `storage_location`, `active`, `created_at`.
- Relationships: one active flag per challenge.
- Seed data: all required flags.
- Challenge dependency: all challenges.

### challenges

- Purpose: challenge metadata shown on board.
- Key fields: `id`, `key`, `title`, `category`, `difficulty`, `summary`, `affected_endpoint`, `points`, `enabled`, `sort_order`.
- Relationships: challenge has one flag and many solved records.
- Seed data: 13 required challenges.
- Challenge dependency: challenge board.

### solved_challenges

- Purpose: per-user solve tracking.
- Key fields: `id`, `user_id`, `challenge_id`, `submitted_flag_hash`, `solved_at`, `attempt_count`.
- Relationships: joins users and challenges.
- Seed data: optional one solved example for admin only.
- Challenge dependency: flag submission system.

### xss_collected_events

- Purpose: lab-only capture endpoint for stored XSS proof.
- Key fields: `id`, `user_id`, `task_id`, `payload_preview`, `captured_value`, `request_ip`, `created_at`.
- Relationships: optional user/task references.
- Seed data: none.
- Challenge dependency: Stored XSS. Do not collect real credentials.

### audit_logs

- Purpose: security logging and alerting baseline.
- Key fields: `id`, `actor_user_id`, `event_type`, `resource_type`, `resource_id`, `result`, `ip_address`, `user_agent`, `metadata_json`, `created_at`.
- Relationships: optional user reference.
- Seed data: initial admin login/admin access examples.
- Challenge dependency: Security Logging and Alerting Failure.

### deployment_notes or lab_settings

- Purpose: track lab profile, deployment warnings, fake deployment note metadata.
- Key fields: `id`, `setting_key`, `setting_value`, `environment`, `public_visible`, `created_at`, `updated_at`.
- Relationships: none required.
- Seed data: lab warning enabled, fake backup location, secure mode default note.
- Challenge dependency: Deployment Misconfiguration.

## Global Configuration Requirements

Base configuration values:

```yaml
app:
  vulnerable-mode: true
  public-domain: "lab.example.com"
  lab-warning-enabled: true
  challenge-submit-enabled: true
  secure-mode-banner-enabled: true

security:
  jwt:
    secret: "secret123"
    access-token-expiration-ms: 3600000
    refresh-token-expiration-ms: 604800000

upload:
  dir: "./uploads"
  max-size: "5MB"

cors:
  mode: "vulnerable-or-secure"

deployment:
  environment: "local | lab | prod"
  expose-vulnerable-lab: false-by-default
```

Profile behavior:

- `local`: may set `app.vulnerable-mode=true` for development.
- `lab`: may set `app.vulnerable-mode=true`, but must require HTTP basic auth, IP allowlist, VPN-only access, or equivalent protection.
- `prod`: must default to `app.vulnerable-mode=false` and `deployment.expose-vulnerable-lab=false`.

## API Design

Auth:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /api/auth/me`
- `POST /api/auth/forgot-password`
- `POST /api/auth/verify-otp`
- `POST /api/auth/reset-password`

Users:

- `GET /api/users/me`
- `PATCH /api/users/me`
- `GET /api/users/{id}/activity`
- `POST /api/users/me/avatar`

Workspaces:

- `GET /api/workspaces`
- `POST /api/workspaces`
- `GET /api/workspaces/{publicId}`
- `PATCH /api/workspaces/{publicId}`
- `POST /api/workspaces/{publicId}/invite`
- `GET /api/workspaces/{publicId}/members`

Tasks:

- `GET /api/workspaces/{workspaceId}/tasks`
- `POST /api/workspaces/{workspaceId}/tasks`
- `GET /api/tasks/{taskId}`
- `PATCH /api/tasks/{taskId}`
- `GET /api/tasks/search?keyword=`
- `POST /api/tasks/import`

Comments:

- `GET /api/tasks/{taskId}/comments`
- `POST /api/tasks/{taskId}/comments`

Files:

- `POST /api/files/upload`
- `GET /api/files/{publicId}`
- `GET /api/files/download?name=`

Admin:

- `GET /api/admin/users`
- `GET /api/admin/workspaces`
- `GET /api/admin/flag`
- `GET /api/admin/jwt-flag`
- `GET /api/admin/reset-flag`

Challenge:

- `GET /api/challenges`
- `GET /api/challenges/{key}`
- `POST /api/challenges/submit`
- `GET /api/challenges/progress`

Lab:

- `POST /api/lab/deserialization/import-profile`
- `GET /api/lab/debug/info`
- `GET /api/lab/xss/collect`
- `GET /api/lab/deploy/notes`

## Challenge Strategy

Each challenge must include four implementation notes in code comments or docs:

- Vulnerable behavior.
- Expected exploitation path.
- Secure fix.
- Deployment safety guard.

Challenge catalog:

| # | Challenge | Category | Endpoint or feature | Flag |
|---|---|---|---|---|
| 1 | Admin Door Without Guard | Broken Access Control | `GET /api/admin/users` | `FLAG{admin_api_missing_role_check}` |
| 2 | Search Is Not Just Search | SQL Injection | `GET /api/tasks/search?keyword=` | `FLAG{sqli_union_search_task}` |
| 3 | Private Workspace Leak | IDOR | `GET /api/workspaces/{publicId}` | `FLAG{idor_workspace_access}` |
| 4 | Profile Privilege Upgrade | Mass Assignment | `PATCH /api/users/me` | `FLAG{mass_assignment_to_admin}` |
| 5 | Dangerous Comments | Stored XSS | `POST /api/tasks/{taskId}/comments` | `FLAG{stored_xss_comment}` |
| 6 | Avatar Confusion | File Upload Bypass | `POST /api/users/me/avatar` | `FLAG{file_upload_bypass}` |
| 7 | Lost In Path | Path Traversal | `GET /api/files/download?name=` | `FLAG{path_traversal_download}` |
| 8 | Import From Inside | SSRF | `POST /api/tasks/import` | `FLAG{ssrf_internal_service}` |
| 9 | Secret In The Token | Weak JWT Secret | `GET /api/admin/jwt-flag` | `FLAG{weak_jwt_secret}` |
| 10 | Reset Confusion | Reset Password Logic Flaw | password reset flow | `FLAG{reset_token_not_bound_to_user}` |
| 11 | Serialized Trust | Java Deserialization | `POST /api/lab/deserialization/import-profile` | `FLAG{java_deserialization_trusted_untrusted_data}` |
| 12 | Deployment Leftovers | Deployment Misconfiguration | `/backup/.env.bak` on lab domain | `FLAG{deployment_backup_file_exposed}` |
| 13 | Silent Attack | Security Logging Failure | security event coverage | `FLAG{missing_security_logging_alerting}` |

Java deserialization constraints:

- Do not implement arbitrary OS command execution.
- Do not require ysoserial-style gadget chains.
- Demonstrate unsafe `ObjectInputStream` with object tampering, type confusion, or a controlled lab-only `readObject` effect.
- The only sensitive value revealed must be the lab flag.
- Secure mode should replace native serialization with JSON DTOs or apply strict `ObjectInputFilter`, allowlists, integrity signing, and minimal trusted types.

Optional CORS note:

- If the project uses bearer JWT stored in frontend state or localStorage, CORS-with-credentials is less meaningful than in a cookie-based design. Do not add a CORS challenge unless the auth design changes to cookies or a deliberate credentialed cross-origin flow is introduced.

## Deployment Strategy

Domains:

- `app.example.com`: normal frontend.
- `api.example.com`: backend API.
- `lab.example.com`: vulnerable lab environment, access-restricted.

Deployment defaults:

- Production uses secure mode.
- Lab mode is isolated on a separate subdomain or environment.
- Internal services are not publicly exposed.
- `internal-api` has no host port mapping and is reachable only on the Docker network.
- Vulnerable lab public exposure requires HTTP basic auth, IP allowlist, VPN-only access, noindex headers, robots.txt disallow, and a legal warning banner.

Deployment misconfiguration challenge:

- Primary vulnerable artifact: `https://lab.example.com/backup/.env.bak`.
- The file must contain fake lab values only and `FLAG{deployment_backup_file_exposed}`.
- Secure configuration denies hidden files, backup extensions, and directory listing.

## Safety Boundaries

- This project is for the owner's own local lab or owner-controlled domain only.
- Do not include malware, persistence, botnet behavior, phishing, real credential theft, or attacks against external targets.
- All exploit examples must target local lab URLs or the owner's protected lab domain.
- Do not store real secrets in seed files, backup files, writeups, screenshots, or logs.
- Vulnerable mode must never be the default production profile.
- Lab mode must show a warning banner.
- Lab routes must be documented as intentionally vulnerable.
- SSRF examples must target only `internal-api` and local lab resources.
- XSS collection must never ask learners to steal real cookies or credentials.
- Deserialization must be controlled and must not execute host commands.

## Definition of Done for the Entire Project

- Backend implements all core collaboration features.
- Backend implements all 13 challenges in vulnerable mode.
- Backend implements secure behavior for all 13 challenges in secure mode.
- Database migrations and seed data are reproducible from an empty MySQL database.
- Challenge board lists all challenges and tracks solves.
- Frontend supports normal collaboration workflows and challenge workflows.
- Docker Compose starts local dependencies consistently.
- Lab deployment has access restriction and legal warning.
- Production deployment defaults to secure mode.
- Every challenge has a writeup with discovery, exploit path, root cause, secure fix, and secure-mode validation.
- Manual test checklist proves every exploit works in vulnerable mode and fails in secure mode.
- Documentation is portfolio-ready.

## Risk Management

- Risk: shallow lab endpoints disconnected from the app. Mitigation: attach each challenge to real app features.
- Risk: unsafe public vulnerable deployment. Mitigation: secure mode default, access restriction, noindex, robots disallow, warning banner.
- Risk: accidental real secret exposure. Mitigation: `.env.example`, fake lab secrets only, deployment checklist, scan before deploy.
- Risk: broken frontend because backend shifts. Mitigation: freeze API contract on Day 20.
- Risk: fragile seed data. Mitigation: migration-based seeds and deterministic public IDs.
- Risk: Java deserialization crossing into weaponized behavior. Mitigation: controlled lab-only classes and no OS command execution.
- Risk: SSRF reaching real cloud metadata. Mitigation: Docker local target only, secure mode blocks internal ranges, deployment checklist blocks metadata IPs.

## Suggested Git Branch Strategy

- `main`: stable, documented, secure by default.
- `develop`: integration branch.
- `feature/backend-auth`
- `feature/backend-workspaces`
- `feature/backend-challenges`
- `feature/frontend-core`
- `feature/frontend-challenges`
- `feature/deployment`
- `docs/writeups`

Merge only when migrations run, tests pass, and documentation for changed behavior is updated.

## Suggested Commit Convention

Use conventional commits:

- `feat(auth): add JWT login and me endpoint`
- `feat(challenge): add SQL injection lab toggle`
- `fix(security): enforce workspace membership in secure mode`
- `test(auth): add refresh token integration tests`
- `docs(writeup): add stored XSS challenge guide`
- `chore(docker): add MailHog service`

## Testing Strategy

Backend:

- Unit tests for JWT service, password reset service, workspace authorization, file path normalization, SSRF URL validator, and flag submission.
- Integration tests for auth, refresh/logout, workspace CRUD, task CRUD, challenge submission, and admin role checks.
- Profile-based tests where feasible: vulnerable mode expected behavior and secure mode expected blocking.
- Manual exploit validation for every challenge.

Frontend:

- Manual route testing.
- Auth flow testing.
- Protected route redirect testing.
- Challenge board and progress testing.
- Form validation testing.
- Lab banner and secure mode banner testing.

Deployment:

- Verify HTTPS.
- Verify domain routing.
- Verify `prod` profile uses secure mode.
- Verify `lab` profile is access-restricted.
- Verify `internal-api` is not publicly reachable.
- Verify no real secrets are exposed.
- Verify only the fake lab backup file is exposed for the deployment challenge.

## Manual Pentest Validation Strategy

For each challenge:

- Confirm seed data exists.
- Confirm vulnerable mode exposes the intended weakness.
- Confirm the exploit path is reproducible using only local or owner-controlled lab targets.
- Submit the flag through `POST /api/challenges/submit`.
- Confirm solved state appears in `GET /api/challenges/progress`.
- Record exact steps in the matching writeup.

Use a consistent test matrix:

- User context: unauthenticated, normal USER, ADMIN.
- Profile: local vulnerable mode, local secure mode, lab profile, prod profile.
- Expected result: exploit succeeds only in vulnerable mode and only where intended.

## Secure-Mode Validation Strategy

Secure mode must prove the application is still functional while blocking lab exploits:

- Admin APIs require ADMIN.
- Workspace details require membership.
- Task search uses bound parameters.
- Profile update ignores role and authority fields.
- Comments are rendered safely or sanitized.
- Uploads validate bytes, names, sizes, and storage paths.
- Downloads normalize paths and enforce upload root.
- Task import blocks localhost, internal ranges, metadata IPs, and unsafe redirects.
- JWT uses strong secret and role is loaded from trusted server-side source.
- Reset tokens are bound to user/email, expire, and are one-time use.
- Deserialization endpoint rejects native serialized input or uses strict filters.
- Deployment denies backup files and hidden files.
- Security events are logged.

## Writeup Plan

Create one writeup per vulnerability under `WRITEUPS/`. Each writeup must use this structure:

```markdown
# Challenge Name

## Category

OWASP-style category.

## Difficulty

Medium or Medium-Hard.

## Goal

What the learner must achieve.

## Affected endpoint

Endpoint or feature.

## Vulnerable behavior

What is wrong.

## Discovery process

How a learner would discover the bug.

## Exploit path

Step-by-step legal lab-only exploitation path.

## Payload

Only lab-scoped payloads.

## Expected result

What flag or behavior appears.

## Root cause

Why the vulnerability exists.

## Secure fix

How to fix it.

## Secure-mode validation

How to prove the bug is fixed.

## Lessons learned

What the learner should understand.
```

Do not include payloads for attacking external systems. Keep all URLs local or owner-controlled.

## Day 1 - Repository and Local Runtime Skeleton

### Goal

Create the repository structure, Docker Compose skeleton, backend Spring Boot skeleton, health endpoint, and local service containers.

### Backend tasks

- Create `backend/` Maven Spring Boot 3.x project using Java 17 or Java 21.
- Add dependencies: Spring Web, Spring Security, Spring Data JPA, MySQL driver, Validation, Actuator, Redis support, Lombok if desired, Flyway or Liquibase.
- Create package root `com.vulncollab`.
- Add `GET /api/health` returning service status, active profile, and safe mode summary without secrets.
- Add a basic security config that permits health and blocks everything else until auth is implemented.

### Frontend tasks

- None. Create only the empty `frontend/` folder if desired.

### Database tasks

- Add MySQL 8 container to `docker-compose.yml`.
- Add initial database name, username, and password through `.env.example`.
- Do not create tables yet.

### Security lab tasks

- Add clear comments in `.env.example` that lab values are fake and vulnerable mode is for controlled use.

### API endpoints affected

- `GET /api/health`

### Files/folders to create or modify

- `backend/pom.xml`
- `backend/src/main/java/com/vulncollab/VulnCollabApplication.java`
- `backend/src/main/java/com/vulncollab/common/HealthController.java`
- `backend/src/main/java/com/vulncollab/security/SecurityConfig.java`
- `backend/src/main/resources/application.yml`
- `docker-compose.yml`
- `.env.example`
- `docs/DAILY_BUILD_PLAN.md`

### Manual test checklist

- Run `docker compose up -d mysql redis mailhog`.
- Start backend locally.
- Call `GET http://localhost:8080/api/health`.
- Confirm MySQL, Redis, and MailHog containers are healthy.

### Definition of Done

- Local services start from Compose.
- Backend starts and health endpoint responds.
- No secrets are hardcoded except documented lab defaults.

### Notes for implementation agent

- Keep health output safe. Do not echo JWT secrets, database passwords, or Redis URLs.
- Avoid adding business entities before configuration and boot reliability are stable.

## Day 2 - Backend Structure, Error Handling, Profiles, and Config

### Goal

Establish backend module boundaries, common response format, global exception handling, environment profiles, and configuration binding.

### Backend tasks

- Create packages listed in the required structure.
- Add `common/api` response wrapper such as `ApiResponse<T>` with `success`, `data`, `error`, and `timestamp`.
- Add `common/error` exceptions: `NotFoundException`, `ForbiddenException`, `BadRequestException`, `ConflictException`.
- Add `@ControllerAdvice` for validation and domain errors.
- Add configuration property classes for `app`, `security.jwt`, `upload`, `cors`, and `deployment`.
- Add profile files: `application-local.yml`, `application-lab.yml`, `application-prod.yml`.
- Configure prod profile defaults: `app.vulnerable-mode=false` and `deployment.expose-vulnerable-lab=false`.

### Frontend tasks

- None.

### Database tasks

- Configure Flyway or Liquibase but do not add schema yet.
- Validate backend can connect to MySQL.

### Security lab tasks

- Define `LabModeService` that answers whether vulnerable behavior is enabled.
- Add a `LabSafetyProperties` or equivalent config model for warning banners and exposure guard.

### API endpoints affected

- `GET /api/health`

### Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/common/`
- `backend/src/main/java/com/vulncollab/config/`
- `backend/src/main/java/com/vulncollab/lab/`
- `backend/src/main/resources/application-local.yml`
- `backend/src/main/resources/application-lab.yml`
- `backend/src/main/resources/application-prod.yml`

### Manual test checklist

- Start backend with local profile.
- Start backend with prod profile and confirm vulnerable mode is false in safe health summary.
- Trigger a validation error with a temporary test endpoint or invalid health route and confirm structured errors.

### Definition of Done

- Configuration is typed and profile-specific.
- Error responses are consistent.
- Prod cannot accidentally inherit local vulnerable mode.

### Notes for implementation agent

- Use configuration properties instead of direct `@Value` strings throughout the project.
- Keep the response format stable because the frontend will depend on it.

## Day 3 - User Model, Roles, Passwords, and Seed Users

### Goal

Create user persistence, role model, password hashing, and deterministic seed users.

### Backend tasks

- Implement `User` entity with internal ID, public ID, email, password hash, display name, role, enabled status, timestamps.
- Define global roles: `USER`, `ADMIN`.
- Add `UserRepository`.
- Add `PasswordEncoder` bean using BCrypt.
- Add seed data service or migration-based seed script.
- Ensure seed passwords are BCrypt hashed, not stored as plaintext.

### Frontend tasks

- None.

### Database tasks

- Create `users` table.
- Add unique constraints on `email` and `public_id`.
- Seed:
  - `admin@test.com` / `Admin123!` / `ADMIN`
  - `alice@test.com` / `Password123!` / `USER`
  - `bob@test.com` / `Password123!` / `USER`
  - `charlie@test.com` / `Password123!` / `USER`

### Security lab tasks

- Document that seed accounts are lab-only.
- Do not use seed passwords in production.

### API endpoints affected

- None yet, except health may report database connected.

### Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/user/User.java`
- `backend/src/main/java/com/vulncollab/user/UserRole.java`
- `backend/src/main/java/com/vulncollab/user/UserRepository.java`
- `backend/src/main/java/com/vulncollab/security/PasswordConfig.java`
- `backend/src/main/resources/db/migration/V001__create_users.sql`
- `backend/src/main/resources/db/migration/V007__seed_initial_data.sql` or dedicated seed migration

### Manual test checklist

- Recreate database from scratch.
- Confirm users table exists.
- Confirm four seed users exist.
- Confirm password values are BCrypt hashes.

### Definition of Done

- User table and seed accounts are reproducible.
- Backend starts cleanly against an empty database.

### Notes for implementation agent

- Use deterministic public IDs for seed records so writeups can refer to stable examples.
- Never expose `password_hash` in API DTOs.

## Day 4 - Register, Login, JWT, Security Filter, and Me Endpoint

### Goal

Implement registration, login, JWT access tokens, Spring Security authentication, and current-user lookup.

### Backend tasks

- Add `AuthController`, `AuthService`, request/response DTOs.
- Implement `POST /api/auth/register`.
- Implement `POST /api/auth/login`.
- Implement JWT service for signing and validating access tokens.
- Add authentication filter that reads `Authorization: Bearer <token>`.
- Implement `GET /api/auth/me`.
- Add `UserPrincipal` or equivalent security principal.
- Configure route authorization: auth routes public, health public, application routes authenticated by default.

### Frontend tasks

- None.

### Database tasks

- No new tables.
- Registration creates users with `USER` role only.

### Security lab tasks

- Use configured weak JWT secret only in local vulnerable profile for later challenge.
- Document that prod must override JWT secret with a strong env var.

### API endpoints affected

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

### Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/auth/`
- `backend/src/main/java/com/vulncollab/security/JwtService.java`
- `backend/src/main/java/com/vulncollab/security/JwtAuthenticationFilter.java`
- `backend/src/main/java/com/vulncollab/security/SecurityConfig.java`

### Manual test checklist

- Register a new user.
- Log in as `alice@test.com`.
- Call `/api/auth/me` with token and confirm profile is returned.
- Call `/api/auth/me` without token and confirm 401.
- Try duplicate registration and confirm 409.

### Definition of Done

- JWT login flow works.
- Protected routes require authentication.
- User data is returned through safe DTOs.

### Notes for implementation agent

- Do not include refresh tokens yet.
- Keep access token response shape stable for frontend: token, expiresAt, user.

## Day 5 - Refresh Tokens, Logout, and Auth Validation

### Goal

Add refresh token persistence, token rotation or revocation, logout, and auth test coverage.

### Backend tasks

- Create `refresh_tokens` table and entity.
- Implement `POST /api/auth/refresh`.
- Implement `POST /api/auth/logout`.
- Hash refresh tokens before storing.
- Decide rotation policy: rotate on every refresh is preferred.
- Store expiration, revocation timestamp, IP, and user agent.
- Add integration tests or detailed manual auth test plan.

### Frontend tasks

- None.

### Database tasks

- Migration for `refresh_tokens`.
- Add index on `user_id`, `token_hash`, and `expires_at`.

### Security lab tasks

- Document why refresh token storage is server-side and revocable.
- Ensure weak JWT challenge later focuses on access token trust, not refresh token theft.

### API endpoints affected

- `POST /api/auth/refresh`
- `POST /api/auth/logout`

### Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/auth/RefreshToken.java`
- `backend/src/main/java/com/vulncollab/auth/RefreshTokenRepository.java`
- `backend/src/main/java/com/vulncollab/auth/RefreshTokenService.java`
- `backend/src/main/resources/db/migration/V002__create_auth_tokens.sql`
- `backend/src/test/java/com/vulncollab/auth/`

### Manual test checklist

- Log in and receive access and refresh token.
- Refresh access token.
- Reuse old refresh token if rotation is enabled and confirm rejection.
- Logout and confirm refresh token no longer works.

### Definition of Done

- Refresh flow is functional and revocable.
- Auth behavior is documented for frontend integration.

### Notes for implementation agent

- If refresh tokens are returned in JSON rather than HttpOnly cookies, document the tradeoff. Keep CORS challenge out unless using credentialed cookies.

## Day 6 - Workspace Domain and Membership

### Goal

Implement workspace entities, membership roles, workspace CRUD APIs, and seed workspaces.

### Backend tasks

- Add `Workspace`, `WorkspaceMember`, and `WorkspaceRole`.
- Implement workspace service methods for list, create, detail, update, invite, members.
- For secure baseline, list only workspaces where user is a member.
- Owner can update workspace and invite members.
- Admin may list all through admin module later.

### Frontend tasks

- None.

### Database tasks

- Create `workspaces` and `workspace_members`.
- Seed Public Engineering, Mobile Team, Finance Internal, Admin Operations, Deployment Lab.
- Seed memberships with OWNER, MEMBER, VIEWER roles.

### Security lab tasks

- Mark IDOR target: Finance Internal or Admin Operations should be inaccessible to Bob or Charlie in secure mode.
- Store stable `public_id` values for writeups.

### API endpoints affected

- `GET /api/workspaces`
- `POST /api/workspaces`
- `GET /api/workspaces/{publicId}`
- `PATCH /api/workspaces/{publicId}`
- `POST /api/workspaces/{publicId}/invite`
- `GET /api/workspaces/{publicId}/members`

### Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/workspace/`
- `backend/src/main/resources/db/migration/V003__create_workspaces.sql`
- Seed migration updates

### Manual test checklist

- Log in as Alice and list her workspaces.
- Log in as Bob and confirm Finance Internal is absent.
- Create a workspace and confirm creator becomes OWNER.
- Invite Charlie as VIEWER.
- Confirm unauthorized update fails in secure baseline.

### Definition of Done

- Workspace membership model works.
- Seed workspaces and memberships are deterministic.

### Notes for implementation agent

- Keep workspace role checks centralized in `WorkspaceAccessService`.

## Day 7 - Task Board APIs and Seed Tasks

### Goal

Implement task board persistence, CRUD APIs, status updates, and realistic seeded tasks.

### Backend tasks

- Add `Task` entity with public ID, workspace, title, description, status, priority, assignee, creator, due date.
- Implement list by workspace, create, detail, and patch.
- Enforce workspace membership for task access in secure baseline.
- Add status enum: `TODO`, `IN_PROGRESS`, `REVIEW`, `DONE`.
- Add priority enum: `LOW`, `MEDIUM`, `HIGH`, `URGENT`.

### Frontend tasks

- None.

### Database tasks

- Create `tasks`.
- Seed tasks:
  - Setup CI pipeline
  - Review Docker config
  - Fix login UI
  - Import partner tasks
  - Private payroll review
  - Deployment checklist
  - Serialized import investigation

### Security lab tasks

- Seed searchable task data for SQL injection challenge.
- Ensure at least one private task contains a hint-like business value but not the final flag unless intended.

### API endpoints affected

- `GET /api/workspaces/{workspaceId}/tasks`
- `POST /api/workspaces/{workspaceId}/tasks`
- `GET /api/tasks/{taskId}`
- `PATCH /api/tasks/{taskId}`

### Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/task/`
- `backend/src/main/resources/db/migration/V004__create_tasks_comments_files.sql`
- Seed migration updates

### Manual test checklist

- Alice lists tasks in Public Engineering.
- Bob cannot access Finance Internal task in secure baseline.
- Create a task and move it across statuses.
- Confirm task detail includes safe workspace and assignee DTOs.

### Definition of Done

- Task board API is usable for a frontend kanban board.
- Task access respects workspace membership.

### Notes for implementation agent

- Do not build SQL injection yet. Day 14 introduces vulnerable search deliberately.

## Day 8 - Comments Module and Stored Comment Foundation

### Goal

Implement task comments and document secure rendering assumptions before adding stored XSS lab behavior.

### Backend tasks

- Add `Comment` entity.
- Implement list and create comments for a task.
- Enforce that commenter must have workspace membership.
- Store raw body and optionally sanitized body field for secure mode.
- Add validation length limits.

### Frontend tasks

- None, except note future UI must render safely by default.

### Database tasks

- Create `comments` table if not already included.
- Seed realistic comments on engineering and finance tasks.

### Security lab tasks

- Document that vulnerable mode will allow raw HTML to be stored and rendered by frontend.
- Do not collect cookies or real credentials for the XSS lab.
- Plan `xss_collected_events` table for later.

### API endpoints affected

- `GET /api/tasks/{taskId}/comments`
- `POST /api/tasks/{taskId}/comments`

### Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/comment/`
- Migration updates for comments

### Manual test checklist

- Add comment as workspace member.
- Try adding comment as non-member and confirm rejection.
- Confirm comment order is stable by creation time.
- Confirm large comment body is rejected.

### Definition of Done

- Comments work as a normal product feature.
- Stored XSS lab requirements are documented but not yet exposed.

### Notes for implementation agent

- Preserve raw body if needed for lab mode, but make secure mode return escaped or sanitized content.

## Day 9 - File Module, Upload, Download, and Seed Files

### Goal

Implement file metadata, upload/download services, safe storage layout, and sample seeded files.

### Backend tasks

- Add `FileRecord` entity.
- Implement upload endpoint for general attachments.
- Implement metadata endpoint by public ID.
- Implement download endpoint by file name for later traversal challenge.
- Add `StorageService` with upload root config.
- Generate stored filenames server-side.
- Enforce upload size limit.

### Frontend tasks

- None.

### Database tasks

- Create `files`.
- Seed metadata and physical files for:
  - `public-report.txt`
  - `private-finance-note.txt`
  - `deployment-backup.env.bak`
  - `serialized-sample.bin`

### Security lab tasks

- Do not implement bypass yet; prepare extension/content-type validation strategy.
- Prepare private finance note as traversal target if appropriate.

### API endpoints affected

- `POST /api/files/upload`
- `GET /api/files/{publicId}`
- `GET /api/files/download?name=`

### Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/file/`
- `backend/src/main/resources/seed/files/`
- Migration updates for files
- `uploads/.gitkeep` if needed

### Manual test checklist

- Upload a small text file.
- Download by valid name.
- Confirm oversized upload fails.
- Confirm metadata does not expose full server paths.

### Definition of Done

- File upload/download works for normal use.
- Storage paths are configurable.

### Notes for implementation agent

- Keep physical seed files fake and harmless.
- Avoid storing files inside source-controlled directories except seed fixtures.

## Day 10 - Admin Module and Basic Role Checks

### Goal

Implement admin APIs and enforce role checks in secure baseline.

### Backend tasks

- Add `AdminController` and `AdminService`.
- Implement admin user listing.
- Implement admin workspace listing.
- Implement placeholder admin flag endpoints for later challenges.
- Use method security or route authorization for ADMIN checks.
- Add audit log planning hook for admin access attempts.

### Frontend tasks

- None.

### Database tasks

- No new tables unless audit logs are created early.

### Security lab tasks

- Identify `GET /api/admin/users` as Broken Access Control challenge target.
- In secure baseline, verify USER cannot access it.
- Day 13 will add vulnerable mode branch that checks authentication only.

### API endpoints affected

- `GET /api/admin/users`
- `GET /api/admin/workspaces`
- `GET /api/admin/flag`
- `GET /api/admin/jwt-flag`
- `GET /api/admin/reset-flag`

### Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/admin/`
- `backend/src/main/java/com/vulncollab/security/SecurityConfig.java`

### Manual test checklist

- Admin can list users.
- Alice cannot list users in secure baseline.
- Unauthenticated request returns 401.
- Admin workspace list includes all seed workspaces.

### Definition of Done

- Admin module exists and is secure by default.
- Later broken access challenge has a clear target.

### Notes for implementation agent

- Do not put flags in admin responses except the intended lab flag endpoints.

## Day 11 - Challenge Module, Flags, and Solve Tracking

### Goal

Create challenge metadata, flag storage, flag submission, and solved challenge tracking.

### Backend tasks

- Add `Challenge`, `Flag`, and `SolvedChallenge` entities.
- Implement challenge list, detail, submit, and progress APIs.
- Compare submitted flags exactly after trimming surrounding whitespace.
- Track attempts and solve timestamp.
- Avoid returning flag values in challenge metadata.

### Frontend tasks

- None.

### Database tasks

- Create `challenges`, `flags`, `solved_challenges`.
- Seed all 13 challenges and flags.

### Security lab tasks

- Add challenge keys matching writeup filenames.
- Store flags server-side only.

### API endpoints affected

- `GET /api/challenges`
- `GET /api/challenges/{key}`
- `POST /api/challenges/submit`
- `GET /api/challenges/progress`

### Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/challenge/`
- `backend/src/main/resources/db/migration/V005__create_challenges.sql`
- Seed migration updates

### Manual test checklist

- List challenges as Alice.
- Submit wrong flag and confirm attempt count increments.
- Submit correct flag and confirm solved state.
- Resubmit solved flag and confirm idempotent response.

### Definition of Done

- Challenge board backend is functional.
- Flags are never leaked by metadata APIs.

### Notes for implementation agent

- Consider hashing submitted flags in `solved_challenges`, but keep canonical flag value in `flags` for validation.

## Day 12 - Forgot Password OTP and MailHog

### Goal

Implement forgot-password, OTP verification, reset token issuance, and MailHog integration.

### Backend tasks

- Create password reset entity/table.
- Implement `POST /api/auth/forgot-password`.
- Generate OTP and send email through local MailHog.
- Implement `POST /api/auth/verify-otp`.
- Implement `POST /api/auth/reset-password`.
- Add expiration, attempt count, one-time use, and user binding in secure baseline.

### Frontend tasks

- None.

### Database tasks

- Create `password_reset_tokens`.
- Add indexes for user, email, expiration, and reset token hash.

### Security lab tasks

- Identify reset logic flaw target for Day 17.
- In secure baseline, token must be bound to user/email.

### API endpoints affected

- `POST /api/auth/forgot-password`
- `POST /api/auth/verify-otp`
- `POST /api/auth/reset-password`

### Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/auth/passwordreset/`
- `backend/src/main/resources/db/migration/V002__create_auth_tokens.sql` or new migration
- `docker-compose.yml` MailHog config

### Manual test checklist

- Request reset for Alice.
- Read OTP in MailHog.
- Verify OTP and receive reset token.
- Reset password and log in with new password.
- Confirm reset token cannot be reused.

### Definition of Done

- Password reset works safely in baseline.
- MailHog captures local email.

### Notes for implementation agent

- For unknown email, return generic success message to avoid account enumeration.

## Day 13 - Broken Access Control and Mass Assignment

### Goal

Implement vulnerable-mode and secure-mode behavior for Admin Door Without Guard and Profile Privilege Upgrade.

### Backend tasks

- Add lab switch in admin user listing:
  - Vulnerable mode: any authenticated user can call `GET /api/admin/users`.
  - Secure mode: require ADMIN.
- Implement `PATCH /api/users/me`.
- Vulnerable mode: map arbitrary request fields directly or dangerously into user entity, allowing role changes.
- Secure mode: use strict DTO permitting only display name and safe profile fields.
- Add user activity entries for profile updates.

### Frontend tasks

- None.

### Database tasks

- Add `user_activities` if not already present.
- Ensure users table role changes are observable.

### Security lab tasks

- Challenge 1 flag: `FLAG{admin_api_missing_role_check}` from admin API behavior.
- Challenge 4 flag: `FLAG{mass_assignment_to_admin}` after successful role escalation or through admin-only flag access.
- Document request manipulation path in writeup, not visible malicious UI.

### API endpoints affected

- `GET /api/admin/users`
- `PATCH /api/users/me`
- `GET /api/admin/flag`

### Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/admin/`
- `backend/src/main/java/com/vulncollab/user/`
- `backend/src/main/java/com/vulncollab/lab/idor/` only if shared helpers are needed
- `WRITEUPS/01-broken-access-control.md`
- Mass assignment writeup may be included as an extra or documented in challenge design if not in required file list.

### Manual test checklist

- In vulnerable mode, Alice calls `/api/admin/users` and sees user list or flag hint.
- In secure mode, Alice receives 403.
- In vulnerable mode, Alice sends role field in profile patch and becomes ADMIN.
- In secure mode, role field is ignored or rejected.

### Definition of Done

- Both vulnerabilities work only in vulnerable mode.
- Secure mode blocks both without breaking normal profile updates.

### Notes for implementation agent

- If role escalation affects later tests, reset seed database or provide cleanup steps.

## Day 14 - IDOR Workspace and SQL Injection Search

### Goal

Implement vulnerable-mode and secure-mode behavior for workspace IDOR and task search SQL injection.

### Backend tasks

- Modify workspace detail service:
  - Vulnerable mode: fetch by public ID and return without membership check.
  - Secure mode: require membership or ADMIN.
- Add task search endpoint.
- Vulnerable mode: intentionally build native SQL with string concatenation.
- Secure mode: use JPA query parameters or Criteria API.
- Keep SQLi lab scoped to seeded local database.

### Frontend tasks

- None.

### Database tasks

- Ensure Finance Internal and Admin Operations have stable public IDs.
- Ensure searchable seed rows can reveal SQLi flag through controlled query result.

### Security lab tasks

- Challenge 2 flag: `FLAG{sqli_union_search_task}`.
- Challenge 3 flag: `FLAG{idor_workspace_access}`.
- Writeups must explain legal local-only payloads.

### API endpoints affected

- `GET /api/workspaces/{publicId}`
- `GET /api/tasks/search?keyword=`

### Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/workspace/WorkspaceService.java`
- `backend/src/main/java/com/vulncollab/task/TaskSearchService.java`
- `backend/src/main/java/com/vulncollab/lab/sqli/`
- `backend/src/main/java/com/vulncollab/lab/idor/`
- `WRITEUPS/02-sqli-search.md`
- `WRITEUPS/03-idor-workspace.md`

### Manual test checklist

- In vulnerable mode, Bob accesses Finance Internal public ID.
- In secure mode, Bob receives 403.
- In vulnerable mode, SQLi payload retrieves intended lab flag or hidden task.
- In secure mode, same payload is treated as a search string.

### Definition of Done

- IDOR and SQLi are reproducible in vulnerable mode.
- Secure mode has clear authorization and parameter binding.

### Notes for implementation agent

- Avoid destructive SQLi examples. The challenge should demonstrate data disclosure only inside the lab.

## Day 15 - Stored XSS Backend Support and File Upload Bypass

### Goal

Implement vulnerable-mode support for stored XSS and avatar upload bypass, plus secure-mode protections.

### Backend tasks

- Add `xss_collected_events` table and collection endpoint.
- Comments:
  - Vulnerable mode: return raw comment body to frontend.
  - Secure mode: return sanitized or escaped body.
- Avatar upload:
  - Vulnerable mode: trust filename extension or client content type.
  - Secure mode: validate magic bytes, restrict type, rename file, store outside web root, serve with safe headers.
- Add avatar endpoint `POST /api/users/me/avatar`.

### Frontend tasks

- None now, but document that Day 24 must use lab mode to decide unsafe rendering.

### Database tasks

- Create `xss_collected_events`.
- Ensure `users.avatar_file_id` relationship works.

### Security lab tasks

- Challenge 5 flag: `FLAG{stored_xss_comment}`.
- Challenge 6 flag: `FLAG{file_upload_bypass}`.
- XSS collector must not capture real credentials; use lab-only marker values.

### API endpoints affected

- `POST /api/tasks/{taskId}/comments`
- `GET /api/tasks/{taskId}/comments`
- `GET /api/lab/xss/collect`
- `POST /api/users/me/avatar`

### Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/comment/`
- `backend/src/main/java/com/vulncollab/file/`
- `backend/src/main/java/com/vulncollab/lab/xss/`
- `backend/src/main/java/com/vulncollab/lab/upload/`
- `WRITEUPS/04-stored-xss.md`
- `WRITEUPS/05-file-upload-bypass.md`

### Manual test checklist

- In vulnerable mode, submit a harmless lab XSS payload that calls the local collector.
- Confirm collector records event and flag path is available.
- In secure mode, same payload renders as text or sanitized HTML.
- In vulnerable mode, upload a mislabeled avatar file and observe intended lab behavior.
- In secure mode, invalid bytes are rejected.

### Definition of Done

- Stored XSS and upload bypass work only in vulnerable mode.
- Secure mode protections are testable.

### Notes for implementation agent

- Do not design XSS around stealing auth tokens. Use a lab collector and synthetic marker.

## Day 16 - Path Traversal and SSRF with Internal API

### Goal

Implement path traversal and SSRF labs, including the private `internal-api` service.

### Backend tasks

- Download endpoint:
  - Vulnerable mode: concatenate upload directory and user `name`.
  - Secure mode: normalize path and enforce resolved path starts with upload root.
- Task import:
  - Vulnerable mode: fetch arbitrary user URL and convert response into a task draft.
  - Secure mode: allowlist domains, block localhost/internal ranges, block metadata IPs, restrict schemes, and disable unsafe redirects.
- Add import request DTO with `url` and optional workspace target.

### Frontend tasks

- None.

### Database tasks

- Ensure private file exists outside normal listing but inside lab-accessible path.
- Seed `internal-api` response flag.

### Security lab tasks

- Add `internal-api` service at `http://internal-api:9090/flag`.
- Do not expose `internal-api` host port.
- Challenge 7 flag: `FLAG{path_traversal_download}`.
- Challenge 8 flag: `FLAG{ssrf_internal_service}`.

### API endpoints affected

- `GET /api/files/download?name=`
- `POST /api/tasks/import`
- Internal: `GET http://internal-api:9090/flag`

### Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/lab/traversal/`
- `backend/src/main/java/com/vulncollab/lab/ssrf/`
- `internal-api/`
- `docker-compose.yml`
- `WRITEUPS/06-path-traversal.md`
- `WRITEUPS/07-ssrf.md`

### Manual test checklist

- In vulnerable mode, traversal payload retrieves only intended lab file.
- In secure mode, traversal payload is rejected.
- From host browser, confirm `internal-api` is not reachable.
- From backend container, vulnerable import can fetch `http://internal-api:9090/flag`.
- Secure mode blocks internal host.

### Definition of Done

- Internal API is private to Docker network.
- SSRF and traversal are scoped and reproducible.

### Notes for implementation agent

- Add URL parser tests. SSRF filters are easy to bypass with redirects, DNS tricks, or alternate IP notation.

## Day 17 - Weak JWT Secret and Reset Password Logic Flaw

### Goal

Implement weak JWT/token trust lab and vulnerable reset-token binding behavior.

### Backend tasks

- JWT challenge:
  - Vulnerable mode: use weak secret `secret123` and trust role claim in token for `GET /api/admin/jwt-flag`.
  - Secure mode: use strong secret from env and load role from database/security context.
- Reset challenge:
  - Vulnerable mode: reset token verification is not properly bound to the requesting email/user.
  - Secure mode: bind token to user ID/email, expiration, one-time use, and verification state.
- Add reset flag endpoint as needed.

### Frontend tasks

- None.

### Database tasks

- Ensure password reset token records capture enough state to demonstrate secure fix.

### Security lab tasks

- Challenge 9 flag: `FLAG{weak_jwt_secret}`.
- Challenge 10 flag: `FLAG{reset_token_not_bound_to_user}`.
- Do not teach attacks against external JWT systems; keep examples local.

### API endpoints affected

- `GET /api/admin/jwt-flag`
- `POST /api/auth/forgot-password`
- `POST /api/auth/verify-otp`
- `POST /api/auth/reset-password`
- `GET /api/admin/reset-flag`

### Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/lab/jwt/`
- `backend/src/main/java/com/vulncollab/auth/passwordreset/`
- `WRITEUPS/08-jwt-weak-secret.md`
- `WRITEUPS/09-reset-password-logic-flaw.md`

### Manual test checklist

- In vulnerable mode, craft local lab JWT with changed role claim and access JWT flag.
- In secure mode, modified token is rejected or role is ignored.
- In vulnerable mode, demonstrate reset token confusion using seed users.
- In secure mode, same reset attempt fails.

### Definition of Done

- JWT and reset challenges are implemented and scoped.
- Secure mode validates server-side trust decisions.

### Notes for implementation agent

- Keep JWT writeup focused on weak secret and trusting claims. Do not include real-world target instructions.

## Day 18 - Java Deserialization and Deployment Misconfiguration Planning

### Goal

Implement controlled Java deserialization lab and plan deployment leftover challenge assets.

### Backend tasks

- Add `POST /api/lab/deserialization/import-profile`.
- Vulnerable mode: accept Base64 Java serialized object and deserialize with `ObjectInputStream`.
- Use controlled lab classes such as `SerializedProfileImport`, `LabAction`, or `DebugTask`.
- The tampered object may trigger a lab-only flag reveal but must not execute OS commands.
- Secure mode: reject native serialized input and accept JSON DTO, or use `ObjectInputFilter` with strict allowlist and integrity checks.
- Add deploy notes endpoint for lab deployment metadata.

### Frontend tasks

- None.

### Database tasks

- Ensure `serialized-sample.bin` seed file is available.
- Add `deployment_notes` or `lab_settings` table if not already created.

### Security lab tasks

- Challenge 11 flag: `FLAG{java_deserialization_trusted_untrusted_data}`.
- Challenge 12 planning: fake backup file contains only fake values and `FLAG{deployment_backup_file_exposed}`.

### API endpoints affected

- `POST /api/lab/deserialization/import-profile`
- `GET /api/lab/deploy/notes`

### Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/lab/deserialization/`
- `backend/src/main/java/com/vulncollab/lab/deploy/`
- `backend/src/main/resources/seed/serialized-sample.bin`
- `WRITEUPS/10-java-deserialization.md`
- `WRITEUPS/11-deploy-misconfiguration.md`

### Manual test checklist

- In vulnerable mode, submit sample serialized object and receive normal import result.
- Submit tampered lab object and receive only the lab flag.
- In secure mode, native serialized input is rejected.
- Confirm no command execution path exists.

### Definition of Done

- Deserialization lab demonstrates trust boundary failure safely.
- Deployment challenge assets are planned but not publicly deployed yet.

### Notes for implementation agent

- Keep deserialization classes small and isolated under `lab/deserialization`.
- Do not add gadget-chain dependencies.

## Day 19 - Secure Mode Implementations for All Backend Vulnerabilities

### Goal

Complete secure-mode behavior for every challenge and ensure vulnerable mode toggles are explicit.

### Backend tasks

- Review all challenge modules and ensure each has secure branch behavior.
- Centralize mode checks in `LabModeService`.
- Add tests or manual validators for both modes.
- Add security headers where backend controls them.
- Ensure prod profile refuses to start with vulnerable mode enabled unless an explicit override is present, preferably failing fast.

### Frontend tasks

- None.

### Database tasks

- No schema changes unless missing audit/log fields are found.

### Security lab tasks

- Verify secure fixes:
  - ADMIN role enforcement.
  - Workspace membership check.
  - Parameterized search.
  - Strict profile DTO.
  - XSS sanitization/escaping.
  - Upload magic byte validation.
  - Path normalization.
  - SSRF allowlist and internal IP blocking.
  - Strong JWT/server-side role.
  - Bound reset token.
  - Safe deserialization alternative.
  - Backup denial config planned.
  - Audit logs for important events.

### API endpoints affected

- All challenge endpoints.

### Files/folders to create or modify

- All `backend/src/main/java/com/vulncollab/lab/**`
- `backend/src/main/java/com/vulncollab/security/**`
- `backend/src/main/java/com/vulncollab/config/**`
- `docs/SECURITY_MODEL.md`

### Manual test checklist

- Run vulnerable mode exploit checks.
- Switch to secure mode.
- Re-run every exploit and confirm it fails safely.
- Confirm normal app behavior still works in secure mode.

### Definition of Done

- Every challenge has vulnerable and secure behavior.
- Production cannot casually start in vulnerable mode.

### Notes for implementation agent

- Avoid scattered `if` logic where possible. Use service methods with clearly named vulnerable and secure implementations.

## Day 20 - Backend Validation, API Docs, and Contract Freeze

### Goal

Validate backend manually, document API design, and freeze the frontend contract.

### Backend tasks

- Run integration tests.
- Complete manual exploit validation checklist.
- Complete secure-mode validation checklist.
- Write API request/response examples.
- Confirm error response consistency.
- Confirm CORS policy for frontend origin.

### Frontend tasks

- None, but produce API contract for Day 21 onward.

### Database tasks

- Recreate database from scratch and validate migrations/seeds.
- Add seed reset instructions.

### Security lab tasks

- Create `docs/CHALLENGE_DESIGN.md`.
- Create initial writeup drafts for all challenge files.

### API endpoints affected

- All backend endpoints.

### Files/folders to create or modify

- `docs/API_DESIGN.md`
- `docs/DATABASE_DESIGN.md`
- `docs/CHALLENGE_DESIGN.md`
- `docs/TESTING_CHECKLIST.md`
- `WRITEUPS/*.md`

### Manual test checklist

- New database migration succeeds.
- Login works for all seed users.
- Each normal app module works.
- Each challenge has a manual vulnerable-mode proof.
- Each challenge has a secure-mode blocking proof.

### Definition of Done

- Backend API is stable enough for frontend implementation.
- Documentation captures endpoint contracts.

### Notes for implementation agent

- Do not begin frontend until endpoint names, DTOs, and error formats are stable.

## Day 21 - React Vite Setup, Tailwind, Routing, Layout, and Axios

### Goal

Create the frontend project foundation and connect it to the backend API shape.

### Backend tasks

- Adjust CORS if the frontend dev server origin differs from expected.
- Fix only contract issues discovered during frontend setup.

### Frontend tasks

- Create React Vite TypeScript app.
- Install Tailwind CSS and configure content paths.
- Add React Router.
- Add Axios client with base URL from env.
- Add route shell with authenticated layout and public auth layout.
- Add app-level error boundary or fallback.
- Add design system primitives: buttons, inputs, forms, panels, alerts, badges, tables.

### Database tasks

- None.

### Security lab tasks

- Add frontend config for lab warning and secure mode banner based on backend health/config endpoint.

### API endpoints affected

- `GET /api/health`

### Files/folders to create or modify

- `frontend/package.json`
- `frontend/src/api/client.ts`
- `frontend/src/routes/`
- `frontend/src/components/`
- `frontend/src/pages/`
- `frontend/src/types/`
- `frontend/src/utils/`
- `frontend/Dockerfile`

### Manual test checklist

- Run frontend dev server.
- Confirm Tailwind styles load.
- Confirm Axios can call health endpoint.
- Confirm unknown route shows not-found page.

### Definition of Done

- Frontend shell is running.
- API client and routing are ready for auth.

### Notes for implementation agent

- Build the application UI first, not a landing page. The first authenticated screen should be the dashboard.

## Day 22 - Login, Register, Auth Store, Protected Routes, and Me Integration

### Goal

Implement frontend authentication flows.

### Backend tasks

- Fix CORS/auth response issues if discovered.
- Confirm refresh/logout behavior supports frontend flow.

### Frontend tasks

- Add login page.
- Add register page.
- Add auth store with Zustand or React Context.
- Store access token and refresh token according to chosen auth design.
- Implement protected routes.
- Implement `/me` bootstrap on page reload.
- Add logout action.
- Add token refresh behavior on 401 if using refresh tokens in frontend.

### Database tasks

- None.

### Security lab tasks

- Add lab warning banner after login when vulnerable mode is active.
- Do not expose seed passwords in UI except documentation.

### API endpoints affected

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /api/auth/me`

### Files/folders to create or modify

- `frontend/src/pages/LoginPage.tsx`
- `frontend/src/pages/RegisterPage.tsx`
- `frontend/src/stores/authStore.ts`
- `frontend/src/routes/ProtectedRoute.tsx`
- `frontend/src/api/authApi.ts`

### Manual test checklist

- Log in as Alice.
- Refresh browser and remain authenticated if refresh strategy supports it.
- Logout clears session.
- Invalid login shows useful error.
- Protected route redirects unauthenticated users.

### Definition of Done

- Authenticated frontend session works end to end.

### Notes for implementation agent

- Keep admin navigation hidden for normal users later, but never rely on frontend hiding for authorization.

## Day 23 - Dashboard, Workspace List, and Workspace Detail

### Goal

Build the main collaboration navigation and workspace screens.

### Backend tasks

- Fix workspace DTO issues found by frontend.
- Add pagination or sorting if workspace lists are unwieldy.

### Frontend tasks

- Build dashboard page with user's workspaces, assigned tasks summary, and challenge progress preview.
- Build workspace list page.
- Build workspace detail page with members and metadata.
- Add create workspace form.
- Add invite member form for owners.
- Handle 403 and 404 states cleanly.

### Database tasks

- Validate seed memberships support meaningful dashboard differences between Alice, Bob, Charlie, and Admin.

### Security lab tasks

- Do not make IDOR exploit a visible UI feature. The writeup should instruct changing URL/API request.
- Show access denied state in secure mode.

### API endpoints affected

- `GET /api/workspaces`
- `POST /api/workspaces`
- `GET /api/workspaces/{publicId}`
- `PATCH /api/workspaces/{publicId}`
- `POST /api/workspaces/{publicId}/invite`
- `GET /api/workspaces/{publicId}/members`

### Files/folders to create or modify

- `frontend/src/pages/DashboardPage.tsx`
- `frontend/src/pages/WorkspaceListPage.tsx`
- `frontend/src/pages/WorkspaceDetailPage.tsx`
- `frontend/src/api/workspaceApi.ts`
- `frontend/src/types/workspace.ts`

### Manual test checklist

- Alice sees Public Engineering and Finance Internal as seeded.
- Bob does not see Finance Internal.
- Owner can invite a user.
- Viewer cannot update workspace.
- Direct forbidden workspace route shows proper error.

### Definition of Done

- Workspace UI supports real collaboration workflows.

### Notes for implementation agent

- Use dense but readable SaaS-style layout. Avoid marketing hero sections.

## Day 24 - Task Board, Task Detail, Comments UI, and XSS Lab Rendering

### Goal

Build the task board and comments experience, including controlled stored XSS rendering behavior.

### Backend tasks

- Fix task/comment DTO fields needed by board UI.
- Ensure comment response indicates whether content is sanitized or raw if helpful.

### Frontend tasks

- Build kanban task board grouped by status.
- Add create task form.
- Add task detail panel/page.
- Add task status update controls.
- Add comments list and comment form.
- In vulnerable mode only, render comment content in the intentionally unsafe lab path.
- In secure mode, render comments as text or sanitized HTML.

### Database tasks

- Validate seeded tasks cover all board columns.

### Security lab tasks

- Stored XSS challenge must be solvable from comments UI.
- Lab banner must be visible when unsafe rendering is active.

### API endpoints affected

- `GET /api/workspaces/{workspaceId}/tasks`
- `POST /api/workspaces/{workspaceId}/tasks`
- `GET /api/tasks/{taskId}`
- `PATCH /api/tasks/{taskId}`
- `GET /api/tasks/{taskId}/comments`
- `POST /api/tasks/{taskId}/comments`
- `GET /api/lab/xss/collect`

### Files/folders to create or modify

- `frontend/src/pages/TaskBoardPage.tsx`
- `frontend/src/pages/TaskDetailPage.tsx`
- `frontend/src/components/tasks/`
- `frontend/src/components/comments/`
- `frontend/src/api/taskApi.ts`
- `frontend/src/api/commentApi.ts`

### Manual test checklist

- Create task and move it between columns.
- Add comments as a member.
- Non-member cannot load task comments in secure mode.
- Stored XSS lab payload works only in vulnerable mode.
- Secure mode displays payload harmlessly.

### Definition of Done

- Task board feels like a real app feature.
- Stored XSS rendering toggle is explicit and mode-driven.

### Notes for implementation agent

- Do not add visible instructions explaining the XSS exploit inside the app; use challenge hints and writeups.

## Day 25 - File Upload, Avatar Upload, Profile Page, and Mass Assignment Docs

### Goal

Build file and profile UI without adding malicious controls for mass assignment.

### Backend tasks

- Fix upload response shape if frontend needs public file metadata.
- Verify secure content headers on downloads.

### Frontend tasks

- Build profile page with display name update and avatar upload.
- Build file upload component for task/workspace attachments.
- Build file download links.
- Show upload validation errors clearly.
- Keep profile form limited to safe fields.

### Database tasks

- Validate avatar file relationship persists.

### Security lab tasks

- File upload bypass should be testable through avatar upload and request manipulation.
- Mass assignment should be documented as API manipulation, not a visible malicious UI.

### API endpoints affected

- `GET /api/users/me`
- `PATCH /api/users/me`
- `POST /api/users/me/avatar`
- `POST /api/files/upload`
- `GET /api/files/{publicId}`
- `GET /api/files/download?name=`

### Files/folders to create or modify

- `frontend/src/pages/ProfilePage.tsx`
- `frontend/src/components/files/`
- `frontend/src/api/userApi.ts`
- `frontend/src/api/fileApi.ts`
- `frontend/src/types/file.ts`
- `WRITEUPS/05-file-upload-bypass.md`

### Manual test checklist

- Update display name.
- Upload valid avatar.
- Upload invalid avatar in secure mode and confirm rejection.
- Download seeded public file.
- Verify path traversal is not exposed through normal UI.

### Definition of Done

- Profile and files work for normal users.
- Lab abuse paths remain documented and controlled.

### Notes for implementation agent

- Avoid storing uploaded files in frontend-public directories.

## Day 26 - Admin Panel, Challenge Board, and Flag Submission UI

### Goal

Build admin screens and challenge board.

### Backend tasks

- Fix challenge progress DTOs if frontend requires solved counts and points.
- Ensure admin endpoints still enforce backend authorization in secure mode.

### Frontend tasks

- Build admin panel with users and workspaces tables.
- Hide admin navigation for USER, but treat backend as authority.
- Build challenge board page with categories, difficulty, points, solved state, and hints.
- Build challenge detail page.
- Build flag submission form.
- Show solve progress.

### Database tasks

- Validate challenge metadata seed order and categories.

### Security lab tasks

- Challenge board should not reveal flags.
- Failed submissions should not disclose flag patterns.
- Add rate-limit or audit logging for repeated submissions if available.

### API endpoints affected

- `GET /api/admin/users`
- `GET /api/admin/workspaces`
- `GET /api/challenges`
- `GET /api/challenges/{key}`
- `POST /api/challenges/submit`
- `GET /api/challenges/progress`

### Files/folders to create or modify

- `frontend/src/pages/AdminPage.tsx`
- `frontend/src/pages/ChallengeBoardPage.tsx`
- `frontend/src/pages/ChallengeDetailPage.tsx`
- `frontend/src/components/challenges/`
- `frontend/src/api/adminApi.ts`
- `frontend/src/api/challengeApi.ts`

### Manual test checklist

- USER does not see admin nav.
- Direct admin URL as USER fails in secure mode.
- Challenge list loads.
- Submit wrong and correct flags.
- Solved challenge is marked solved after refresh.

### Definition of Done

- Challenge workflow is usable from frontend.
- Admin UI exists but does not replace backend checks.

### Notes for implementation agent

- Use challenge hints carefully. Hints should guide discovery without giving away the full payload.

## Day 27 - Forgot Password UI, SSRF Import UI, and Deserialization Lab UI

### Goal

Complete remaining user flows and lab-specific UI for import and deserialization.

### Backend tasks

- Fix password reset response messages for frontend usability.
- Confirm task import endpoint validates workspace membership.
- Confirm deserialization endpoint is lab-gated.

### Frontend tasks

- Build forgot password page.
- Build OTP verification page.
- Build reset password page.
- Add task import from URL UI inside workspace/task area.
- Add Java deserialization import lab UI with file/base64 input and clear lab warning.
- Show results safely.

### Database tasks

- Validate reset tokens expire and can be cleaned up.

### Security lab tasks

- SSRF UI should allow local lab URL input in vulnerable mode but not encourage external targeting.
- Deserialization UI should clearly label lab-only sample import.

### API endpoints affected

- `POST /api/auth/forgot-password`
- `POST /api/auth/verify-otp`
- `POST /api/auth/reset-password`
- `POST /api/tasks/import`
- `POST /api/lab/deserialization/import-profile`

### Files/folders to create or modify

- `frontend/src/pages/ForgotPasswordPage.tsx`
- `frontend/src/pages/VerifyOtpPage.tsx`
- `frontend/src/pages/ResetPasswordPage.tsx`
- `frontend/src/components/tasks/TaskImportForm.tsx`
- `frontend/src/pages/DeserializationLabPage.tsx`
- `frontend/src/api/labApi.ts`

### Manual test checklist

- Complete reset flow through MailHog.
- Import a normal external-looking allowed URL in secure mode if configured.
- In vulnerable local mode, import from `http://internal-api:9090/flag`.
- Submit serialized sample and tampered lab object.
- Secure mode blocks internal URL and serialized payload.

### Definition of Done

- Password reset and lab import screens are functional.
- Dangerous flows are clearly lab-scoped.

### Notes for implementation agent

- Do not make frontend call `internal-api` directly. The SSRF lesson requires backend-side fetching.

## Day 28 - Frontend Polish, States, Hints, Progress, and Warning Banners

### Goal

Polish frontend usability and lab clarity.

### Backend tasks

- Add a small config/status endpoint if health is not enough for frontend banners.
- Fix inconsistent DTO fields found during polish.

### Frontend tasks

- Add loading states, empty states, and error states.
- Add challenge progress indicators.
- Add warning banners for vulnerable lab mode.
- Add secure mode banner when `secure-mode-banner-enabled=true`.
- Add challenge hint reveal UI.
- Improve responsive layouts.
- Ensure text does not overflow buttons, cards, or tables.
- Verify no admin-only UI breaks for USER.

### Database tasks

- None.

### Security lab tasks

- Warning banner should state the app is intentionally vulnerable and for authorized lab use only.
- Add no real secret reminders in docs, not as intrusive UI text.

### API endpoints affected

- `GET /api/health` or `GET /api/lab/debug/info`
- `GET /api/challenges`
- `GET /api/challenges/progress`

### Files/folders to create or modify

- `frontend/src/components/layout/`
- `frontend/src/components/common/`
- `frontend/src/styles/`
- `frontend/src/pages/*`

### Manual test checklist

- Test desktop and mobile widths.
- Test loading and failed API states by stopping backend.
- Verify lab warning appears in vulnerable mode.
- Verify secure banner appears in secure mode if enabled.
- Confirm no UI text overlaps.

### Definition of Done

- Frontend is portfolio-quality and usable.
- Lab and secure modes are visually clear.

### Notes for implementation agent

- Keep UI practical and task-focused. This is an operations app, not a marketing page.

## Day 29 - Production Docker Build, Nginx, Domains, HTTPS, and Access Restriction

### Goal

Prepare production and lab deployment with Docker builds, Nginx reverse proxy, domain routing, HTTPS, environment variables, secure defaults, and lab access controls.

### Backend tasks

- Build production Dockerfile for backend.
- Ensure backend reads secrets from env.
- Confirm prod profile defaults to secure mode.
- Add startup guard against vulnerable mode in prod.

### Frontend tasks

- Build production frontend Dockerfile.
- Configure API base URL for domain deployment.

### Database tasks

- Prepare production MySQL volume and backup plan.
- Ensure migrations run on deploy.

### Security lab tasks

- Configure lab subdomain separately from normal app.
- Add HTTP basic auth, IP allowlist, or VPN requirement for `lab.example.com`.
- Add `X-Robots-Tag: noindex, nofollow` and `robots.txt` disallow for lab.
- Do not expose MySQL, Redis, MailHog, or internal-api publicly.

### API endpoints affected

- All endpoints through reverse proxy.

### Files/folders to create or modify

- `backend/Dockerfile`
- `frontend/Dockerfile`
- `docker-compose.prod.yml`
- `docker-compose.lab.yml`
- `nginx/prod.conf`
- `nginx/lab.conf`
- `docs/DEPLOYMENT_GUIDE.md`
- `.env.example`

### Manual test checklist

- Build backend image.
- Build frontend image.
- Start prod compose locally or staging.
- Confirm HTTPS config plan.
- Confirm `app.example.com`, `api.example.com`, and `lab.example.com` routing.
- Confirm vulnerable mode is false in prod.
- Confirm lab route requires access restriction.

### Definition of Done

- Deployment architecture is ready and secure by default.
- Lab profile is isolated and protected.

### Notes for implementation agent

- Do not publish MailHog in production. If email is needed, use a real transactional provider with secrets outside source control.

## Day 30 - Deployment Misconfiguration Challenge and Rollback Plan

### Goal

Implement the fake exposed backup file challenge in lab profile and document secure Nginx rules plus rollback.

### Backend tasks

- Add deploy notes endpoint if needed to guide challenge metadata.
- Confirm backend does not serve real env files.

### Frontend tasks

- Add challenge detail content for Deployment Leftovers.
- Link writeup after solve if the platform supports it.

### Database tasks

- Seed deployment notes/lab settings for fake backup location.

### Security lab tasks

- Create fake backup file for lab only: `/backup/.env.bak`.
- Include only fake values and `FLAG{deployment_backup_file_exposed}`.
- Add vulnerable Nginx example that exposes `/backup/`.
- Add secure Nginx example that denies hidden files, `.bak`, `.backup`, `.env`, `.sql`, and disables autoindex.
- Add rollback plan:
  - Switch lab compose to secure Nginx config.
  - Remove mounted backup directory.
  - Restart Nginx.
  - Verify 403/404 on backup path.

### API endpoints affected

- Static: `https://lab.example.com/backup/.env.bak`
- `GET /api/lab/deploy/notes`

### Files/folders to create or modify

- `nginx/lab.conf`
- `nginx/prod.conf`
- `nginx/local.conf`
- `docs/DEPLOYMENT_GUIDE.md`
- `WRITEUPS/11-deploy-misconfiguration.md`
- Lab-only fake backup fixture directory

### Manual test checklist

- In vulnerable lab config, fetch `/backup/.env.bak` and see fake lab flag.
- In secure config, same path returns 403 or 404.
- Confirm directory listing is disabled.
- Confirm no real `.env` file is mounted under web root.

### Definition of Done

- Deployment challenge is solvable but safe.
- Secure deployment config blocks the same issue.

### Notes for implementation agent

- Name fake values clearly, such as `FAKE_LAB_DB_PASSWORD=not-a-real-secret`.

## Day 31 - End-to-End Testing and Challenge Solve Pass

### Goal

Run full functional, exploit, secure-mode, and deployment validation.

### Backend tasks

- Run all backend tests.
- Fix regressions.
- Verify logs and audit events.
- Validate both vulnerable and secure profiles.

### Frontend tasks

- Run frontend build.
- Manually test routes and forms.
- Fix UX blockers.

### Database tasks

- Drop and recreate local database.
- Run migrations and seed from scratch.
- Confirm deterministic seed IDs and data.

### Security lab tasks

- Manually solve all 13 challenges:
  - Admin Door Without Guard
  - Search Is Not Just Search
  - Private Workspace Leak
  - Profile Privilege Upgrade
  - Dangerous Comments
  - Avatar Confusion
  - Lost In Path
  - Import From Inside
  - Secret In The Token
  - Reset Confusion
  - Serialized Trust
  - Deployment Leftovers
  - Silent Attack
- Switch to secure mode and verify each exploit fails.

### API endpoints affected

- All application and lab endpoints.

### Files/folders to create or modify

- `docs/TESTING_CHECKLIST.md`
- `docs/SECURITY_MODEL.md`
- Writeup updates based on actual solve steps

### Manual test checklist

- Register/login/refresh/logout works.
- Forgot password works through MailHog.
- Workspace, tasks, comments, files work.
- Admin panel works for admin only in secure mode.
- Challenge board tracks solves.
- Secure mode blocks all challenge payloads.
- `internal-api` is not publicly reachable.
- No real secrets are exposed.

### Definition of Done

- Full lab can be solved in vulnerable mode.
- Full lab resists the same attacks in secure mode.
- No critical deployment safety gaps remain.

### Notes for implementation agent

- Record exact command/request examples while testing so writeups match reality.

## Day 32 - Documentation, Portfolio Polish, Screenshots, and Final Checklist

### Goal

Complete documentation, writeups, README, screenshots, demo plan, and portfolio polish.

### Backend tasks

- Remove debug-only code not intended for lab.
- Confirm prod profile and env examples are clean.

### Frontend tasks

- Capture screenshots:
  - Dashboard
  - Workspace task board
  - Challenge board
  - Admin panel
  - Lab warning banner
  - Solve progress
- Prepare short demo GIF plan if desired.

### Database tasks

- Document seed data reset and default accounts.

### Security lab tasks

- Complete all writeups using required template.
- Add legal and safety warning to README.
- Add deployment safety checklist.

### API endpoints affected

- None unless documentation reveals a mismatch.

### Files/folders to create or modify

- `README.md`
- `docs/API_DESIGN.md`
- `docs/DATABASE_DESIGN.md`
- `docs/CHALLENGE_DESIGN.md`
- `docs/DEPLOYMENT_GUIDE.md`
- `docs/SECURITY_MODEL.md`
- `docs/TESTING_CHECKLIST.md`
- `WRITEUPS/*.md`
- `LICENSE`

### Manual test checklist

- Follow README from fresh clone through local startup.
- Confirm all links to docs and writeups work.
- Confirm screenshots do not show real secrets.
- Confirm `.env.example` contains fake placeholders only.
- Confirm GitHub portfolio description states owner-controlled lab use.

### Definition of Done

- Project is ready for GitHub portfolio presentation.
- A new developer can build, run, solve, secure, and deploy the lab using docs.

### Notes for implementation agent

- Keep documentation honest. If a feature is intentionally lab-only or incomplete, label it clearly.

## Detailed Daily Execution Guide

This section expands the daily plan into a concrete execution checklist. Use it when assigning work to another coding agent or when implementing the project day by day. Each day should end with a working commit, a short note in the related docs, and a manual verification result.

### Day 1 Detailed Execution

1. Create the initial repository layout exactly as planned: `backend/`, `frontend/`, `internal-api/`, `nginx/`, `docs/`, and `WRITEUPS/`.
2. Generate the Spring Boot backend under `backend/` and confirm Java version, Maven wrapper, and package root `com.vulncollab`.
3. Add `docker-compose.yml` with MySQL 8, Redis, and MailHog. Use named volumes for MySQL and simple local ports only for development.
4. Add `.env.example` with fake local values and comments explaining that real deployment secrets must not be committed.
5. Implement `GET /api/health` and verify the backend can start before any business logic exists.
6. End-of-day check: a fresh developer can run Compose dependencies, start backend, and call the health endpoint.

### Day 2 Detailed Execution

1. Create all backend packages early so future modules land in predictable places.
2. Implement common response and error models before writing domain controllers.
3. Add global exception handling for validation errors, not-found errors, forbidden errors, bad requests, and unexpected server errors.
4. Bind application configuration into typed property classes instead of scattering raw config strings.
5. Create `local`, `lab`, and `prod` profiles and verify `prod` defaults to secure mode.
6. End-of-day check: switching profiles changes mode safely, and all errors use one JSON structure.

### Day 3 Detailed Execution

1. Design the `users` table with internal numeric ID and external `public_id`.
2. Add the `User` entity, role enum, repository, and password encoder configuration.
3. Seed admin, Alice, Bob, and Charlie with BCrypt password hashes.
4. Confirm registration later cannot assign ADMIN role directly.
5. Confirm seed data is deterministic so writeups can reference stable accounts.
6. End-of-day check: dropping and recreating the database produces the same four users.

### Day 4 Detailed Execution

1. Implement register DTOs with email, display name, and password validation.
2. Implement login DTOs and safe auth responses containing access token, expiry, and user summary.
3. Add JWT signing, validation, subject extraction, and authority extraction.
4. Add the Spring Security JWT filter and configure public versus protected routes.
5. Implement `/api/auth/me` using the authenticated principal, not a user ID supplied by request.
6. End-of-day check: Alice can log in and call `/api/auth/me`; anonymous users cannot.

### Day 5 Detailed Execution

1. Add refresh-token persistence with hashed token values.
2. Implement token creation on login and token rotation or revocation on refresh.
3. Implement logout by revoking the current refresh token.
4. Add manual auth scenarios covering login, refresh, refresh reuse, logout, expired token, and invalid token.
5. Document token storage assumptions for the future frontend.
6. End-of-day check: access token renewal works and logout invalidates refresh access.

### Day 6 Detailed Execution

1. Create workspace and workspace member schema with clear ownership and membership roles.
2. Implement workspace list so users see only workspaces they belong to.
3. Implement create workspace and automatically assign creator as OWNER.
4. Implement invite/member list endpoints with owner-only permissions.
5. Seed workspaces and memberships so each user has a different view of the product.
6. End-of-day check: Bob cannot see Finance Internal through the normal secure workspace list.

### Day 7 Detailed Execution

1. Create task schema with status, priority, assignee, creator, and workspace relationship.
2. Implement task list by workspace and enforce membership.
3. Implement task create, detail, and patch endpoints.
4. Seed realistic tasks across multiple workspaces and statuses.
5. Keep task DTOs ready for a kanban frontend: ID, title, status, priority, assignee, dates.
6. End-of-day check: tasks can be created and moved across board columns.

### Day 8 Detailed Execution

1. Create comments schema tied to task and author.
2. Implement comment creation and listing with task/workspace membership checks.
3. Store raw comment content, but define secure response behavior early.
4. Add length limits and validation messages.
5. Seed realistic comments so the future UI does not look empty.
6. End-of-day check: members can comment; non-members cannot read or write comments.

### Day 9 Detailed Execution

1. Create file metadata schema and storage service.
2. Implement upload to a configurable local upload directory.
3. Generate server-side stored filenames and preserve original filename only as metadata.
4. Implement file metadata lookup and download by name for the later traversal lab.
5. Seed sample files and document which files are public, private, or lab-only.
6. End-of-day check: upload and download work, but server filesystem paths are never exposed.

### Day 10 Detailed Execution

1. Create the admin module separately from user/workspace modules.
2. Implement admin user and workspace listing.
3. Require ADMIN role in secure baseline.
4. Add placeholder admin flag routes that will be wired into challenge behavior later.
5. Prepare audit hooks for admin access attempts.
6. End-of-day check: admin can access admin APIs; Alice receives 403.

### Day 11 Detailed Execution

1. Create challenge, flag, and solved challenge schema.
2. Seed all required challenge metadata and flags.
3. Implement challenge list and detail without ever returning flag values.
4. Implement flag submission with attempt tracking and idempotent solved behavior.
5. Implement challenge progress per authenticated user.
6. End-of-day check: Alice can submit a correct seeded flag and see progress update.

### Day 12 Detailed Execution

1. Create password reset token schema with user, email, OTP hash, reset token hash, expiry, verification, and used state.
2. Implement forgot-password with generic response for both known and unknown emails.
3. Send OTP through MailHog for local testing.
4. Implement OTP verification and reset token issuance.
5. Implement reset-password with one-time token use and password update.
6. End-of-day check: the full reset flow works through MailHog and token reuse fails.

### Day 13 Detailed Execution

1. Add vulnerable-mode branch for `GET /api/admin/users` that checks only authentication.
2. Preserve secure-mode branch requiring ADMIN role.
3. Implement profile update endpoint.
4. In vulnerable mode, allow unsafe direct mapping that can alter role-like fields.
5. In secure mode, accept only strict safe DTO fields.
6. End-of-day check: Alice can exploit both issues in vulnerable mode and cannot in secure mode.

### Day 14 Detailed Execution

1. Modify workspace detail behavior so vulnerable mode omits membership verification.
2. Keep secure mode membership verification centralized and reusable.
3. Implement task search endpoint.
4. In vulnerable mode, use intentionally unsafe string-built SQL for controlled lab data disclosure.
5. In secure mode, use bound parameters.
6. End-of-day check: IDOR and SQLi are reproducible only in vulnerable mode.

### Day 15 Detailed Execution

1. Add `xss_collected_events` schema and lab collector endpoint.
2. Return raw comment content in vulnerable mode and sanitized or escaped content in secure mode.
3. Add avatar upload endpoint.
4. In vulnerable mode, trust extension or client content type.
5. In secure mode, validate magic bytes, rename files, restrict types, and serve safely.
6. End-of-day check: stored XSS proof and avatar bypass work only in vulnerable mode.

### Day 16 Detailed Execution

1. Implement vulnerable file download path concatenation for the traversal lab.
2. Implement secure path normalization and upload-root enforcement.
3. Create `internal-api` service with `/flag` and no public host port.
4. Implement vulnerable task import that fetches user-provided URLs.
5. Implement secure SSRF filtering for schemes, localhost, internal ranges, metadata IPs, and redirects.
6. End-of-day check: backend can reach `internal-api`, host cannot, and secure mode blocks the request.

### Day 17 Detailed Execution

1. Configure vulnerable mode to use weak JWT secret and trust token role claim for the JWT challenge endpoint.
2. Configure secure mode to use strong env-based secret and server-side role lookup.
3. Add vulnerable reset-password path where token validation is not correctly bound to user/email.
4. Keep secure reset path bound to user/email, expiry, verified state, and one-time use.
5. Add writeup notes for exact local-only JWT and reset-token testing.
6. End-of-day check: forged lab JWT and reset confusion work only in vulnerable mode.

### Day 18 Detailed Execution

1. Create isolated deserialization lab package and DTOs/classes.
2. Implement vulnerable Base64 Java deserialization using `ObjectInputStream`.
3. Use only controlled lab classes and a lab-only flag reveal. Do not execute OS commands.
4. Implement secure behavior using JSON input or strict object filtering and allowlists.
5. Prepare fake backup file plan for deployment challenge but do not expose it in normal prod.
6. End-of-day check: tampered serialized lab object reveals only the lab flag in vulnerable mode.

### Day 19 Detailed Execution

1. Review every challenge and confirm there is a vulnerable branch and a secure branch.
2. Add tests or manual checks for both branches.
3. Add prod startup guard so vulnerable mode cannot be accidentally enabled in prod.
4. Verify normal product workflows still work in secure mode.
5. Update `docs/SECURITY_MODEL.md` with the exact secure behavior for every challenge.
6. End-of-day check: all exploit paths fail safely in secure mode.

### Day 20 Detailed Execution

1. Rebuild the database from scratch and confirm migrations plus seeds work.
2. Run backend tests and manual API tests.
3. Write `API_DESIGN.md` with request/response examples for frontend work.
4. Write `DATABASE_DESIGN.md` and `CHALLENGE_DESIGN.md`.
5. Draft every writeup with at least title, category, endpoint, vulnerable behavior, and secure fix.
6. End-of-day check: frontend can start tomorrow without guessing API contracts.

### Day 21 Detailed Execution

1. Create Vite React TypeScript project.
2. Install and configure Tailwind CSS, React Router, Axios, and auth state library.
3. Build the app shell with navigation, protected layout, and public layout.
4. Add reusable UI components for buttons, inputs, alerts, tables, badges, and loading states.
5. Connect Axios to the backend health endpoint.
6. End-of-day check: frontend starts, routing works, and API client can reach backend.

### Day 22 Detailed Execution

1. Build login and register forms with validation and API error display.
2. Implement auth store and token persistence strategy.
3. Add protected route behavior and `/api/auth/me` bootstrapping.
4. Implement logout and optional refresh-on-401 behavior.
5. Display lab warning banner after login when backend reports vulnerable mode.
6. End-of-day check: users can register, log in, refresh page, and log out.

### Day 23 Detailed Execution

1. Build dashboard with workspace summary, assigned task summary, and challenge progress preview.
2. Build workspace list with empty, loading, and error states.
3. Build workspace detail with members and workspace metadata.
4. Add create workspace and invite member forms.
5. Test USER, ADMIN, OWNER, MEMBER, and VIEWER views.
6. End-of-day check: workspace UI supports real daily use, not only lab testing.

### Day 24 Detailed Execution

1. Build kanban board grouped by task status.
2. Add task create, detail, edit, and status update interactions.
3. Add comments list and comment form.
4. Implement vulnerable-mode unsafe comment rendering only when lab mode is active.
5. Implement secure-mode safe rendering by default.
6. End-of-day check: task workflow is usable and stored XSS behavior follows backend mode.

### Day 25 Detailed Execution

1. Build profile page with display name update.
2. Add avatar upload UI and preview.
3. Add file upload component for attachments.
4. Add file metadata and download interactions.
5. Keep mass assignment exploit out of visible UI and document it in the writeup.
6. End-of-day check: profile and file workflows work, and unsafe actions require manual lab request manipulation.

### Day 26 Detailed Execution

1. Build admin users and workspaces tables.
2. Hide admin navigation from normal users while preserving backend enforcement.
3. Build challenge board with categories, difficulty, points, solved state, and progress.
4. Build challenge detail and flag submission form.
5. Add failed-submission and solved-submission states.
6. End-of-day check: challenge board is usable and does not leak flags.

### Day 27 Detailed Execution

1. Build forgot-password, OTP verification, and reset-password pages.
2. Build task import from URL form inside the workspace/task workflow.
3. Build deserialization lab page with Base64/file input and lab warning.
4. Validate SSRF and deserialization flows in vulnerable mode.
5. Validate secure mode blocks internal URL import and serialized input.
6. End-of-day check: all remaining frontend flows are connected to backend.

### Day 28 Detailed Execution

1. Add consistent loading, empty, success, and error states across pages.
2. Add challenge hints and progress indicators.
3. Add vulnerable-mode and secure-mode banners.
4. Test responsive layouts for dashboard, board, admin panel, and challenge board.
5. Fix text overflow, cramped buttons, broken tables, and confusing error messages.
6. End-of-day check: the frontend looks portfolio-ready and remains practical for repeated use.

### Day 29 Detailed Execution

1. Build production Docker images for backend and frontend.
2. Configure Nginx reverse proxy for app, API, and lab subdomains.
3. Add HTTPS plan using Let's Encrypt.
4. Move secrets to environment variables and keep `.env.example` fake.
5. Protect lab subdomain using basic auth, IP allowlist, VPN, or equivalent control.
6. End-of-day check: prod is secure by default and lab is isolated.

### Day 30 Detailed Execution

1. Add lab-only fake `/backup/.env.bak` file with fake values and the deployment challenge flag.
2. Add vulnerable Nginx lab config that exposes the backup path only for the challenge.
3. Add secure Nginx config that denies hidden files, backup files, and directory listing.
4. Document rollback steps from vulnerable lab config to secure lab config.
5. Verify no real `.env`, database dump, source code, or private key is under web root.
6. End-of-day check: deployment challenge is solvable and safely reversible.

### Day 31 Detailed Execution

1. Run backend tests, frontend build, and Compose startup from a clean state.
2. Solve every challenge manually in vulnerable mode.
3. Submit every flag through the challenge board.
4. Switch to secure mode and repeat each exploit attempt.
5. Verify HTTPS, domain routing, lab access restriction, and internal-api isolation.
6. End-of-day check: vulnerable mode teaches every issue and secure mode blocks every issue.

### Day 32 Detailed Execution

1. Finish README with project purpose, safety warning, local setup, deployment overview, and screenshots.
2. Complete all writeups using the required template.
3. Finish API, database, challenge, deployment, security, and testing docs.
4. Capture portfolio screenshots and ensure they show no real secrets.
5. Run a final fresh-clone walkthrough using the docs.
6. End-of-day check: a new developer can build, run, solve, secure, and understand the full lab.

## Final Implementation Checklist

- Backend skeleton, profiles, config, and health endpoint complete.
- MySQL, Redis, MailHog, and internal-api Compose services complete.
- Auth, JWT, refresh, logout, and password reset complete.
- Workspace, tasks, comments, files, profile, and admin modules complete.
- Challenge board backend and frontend complete.
- All 13 vulnerabilities implemented in vulnerable mode.
- All 13 secure fixes implemented in secure mode.
- Database migrations and seeds reproducible.
- Frontend is usable and polished.
- Lab deployment is access-restricted.
- Prod deployment defaults to secure mode.
- Fake deployment backup challenge contains no real secrets.
- Writeups complete and lab-scoped.
- Testing checklist complete.
- README and portfolio assets complete.
