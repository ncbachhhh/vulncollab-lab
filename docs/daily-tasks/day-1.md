# Day 1 - Repository, Docker, Backend Skeleton, and Health Check

## Goal

Create the repository structure, Docker Compose skeleton, backend Spring Boot skeleton, health endpoint, and local service containers.

## Backend tasks

- Create `backend/` Maven Spring Boot 3.x project using Java 17 or Java 21.
- Add dependencies: Spring Web, Spring Security, Spring Data JPA, MySQL driver, Validation, Actuator, Redis support, Lombok if desired, Flyway or Liquibase.
- Create package root `com.vulncollab`.
- Add `GET /api/health` returning service status, active profile, and safe mode summary without secrets.
- Add a basic security config that permits health and blocks everything else until auth is implemented.

## Frontend tasks

- None. Create only the empty `frontend/` folder if desired.

## Database tasks

- Add MySQL 8 container to `docker-compose.yml`.
- Add initial database name, username, and password through `.env.example`.
- Do not create tables yet.

## Security lab tasks

- Add clear comments in `.env.example` that lab values are fake and vulnerable mode is for controlled use.

## API endpoints affected

- `GET /api/health`

## Files/folders to create or modify

- `backend/pom.xml`
- `backend/src/main/java/com/vulncollab/VulnCollabApplication.java`
- `backend/src/main/java/com/vulncollab/common/HealthController.java`
- `backend/src/main/java/com/vulncollab/security/SecurityConfig.java`
- `backend/src/main/resources/application.yml`
- `docker-compose.yml`
- `.env.example`
- `docs/DAILY_BUILD_PLAN.md`

## Manual test checklist

- Run `docker compose up -d mysql redis mailhog`.
- Start backend locally.
- Call `GET http://localhost:8080/api/health`.
- Confirm MySQL, Redis, and MailHog containers are healthy.

## Definition of Done

- Local services start from Compose.
- Backend starts and health endpoint responds.
- No secrets are hardcoded except documented lab defaults.

## Notes for implementation agent

- Keep health output safe. Do not echo JWT secrets, database passwords, or Redis URLs.
- Avoid adding business entities before configuration and boot reliability are stable.

## Detailed execution

1. Create the initial repository layout exactly as planned: `backend/`, `frontend/`, `internal-api/`, `nginx/`, `docs/`, and `WRITEUPS/`.
2. Generate the Spring Boot backend under `backend/` and confirm Java version, Maven wrapper, and package root `com.vulncollab`.
3. Add `docker-compose.yml` with MySQL 8, Redis, and MailHog. Use named volumes for MySQL and simple local ports only for development.
4. Add `.env.example` with fake local values and comments explaining that real deployment secrets must not be committed.
5. Implement `GET /api/health` and verify the backend can start before any business logic exists.
6. End-of-day check: a fresh developer can run Compose dependencies, start backend, and call the health endpoint.
