# Day 2 - Backend Structure, Error Handling, Profiles, and Config

## Goal

Establish backend module boundaries, common response format, global exception handling, environment profiles, and configuration binding.

## Backend tasks

- Create packages listed in the required structure.
- Add `common/api` response wrapper such as `ApiResponse<T>` with `success`, `data`, `error`, and `timestamp`.
- Add `common/error` exceptions: `NotFoundException`, `ForbiddenException`, `BadRequestException`, `ConflictException`.
- Add `@ControllerAdvice` for validation and domain errors.
- Add configuration property classes for `app`, `security.jwt`, `upload`, `cors`, and `deployment`.
- Add profile files: `application-local.yml`, `application-lab.yml`, `application-prod.yml`.
- Configure prod profile defaults: `app.vulnerable-mode=false` and `deployment.expose-vulnerable-lab=false`.

## Frontend tasks

- None.

## Database tasks

- Configure Flyway or Liquibase but do not add schema yet.
- Validate backend can connect to MySQL.

## Security lab tasks

- Define `LabModeService` that answers whether vulnerable behavior is enabled.
- Add a `LabSafetyProperties` or equivalent config model for warning banners and exposure guard.

## API endpoints affected

- `GET /api/health`

## Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/common/`
- `backend/src/main/java/com/vulncollab/config/`
- `backend/src/main/java/com/vulncollab/lab/`
- `backend/src/main/resources/application-local.yml`
- `backend/src/main/resources/application-lab.yml`
- `backend/src/main/resources/application-prod.yml`

## Manual test checklist

- Start backend with local profile.
- Start backend with prod profile and confirm vulnerable mode is false in safe health summary.
- Trigger a validation error with a temporary test endpoint or invalid health route and confirm structured errors.

## Definition of Done

- Configuration is typed and profile-specific.
- Error responses are consistent.
- Prod cannot accidentally inherit local vulnerable mode.

## Notes for implementation agent

- Use configuration properties instead of direct `@Value` strings throughout the project.
- Keep the response format stable because the frontend will depend on it.

## Detailed execution

1. Create all backend packages early so future modules land in predictable places.
2. Implement common response and error models before writing domain controllers.
3. Add global exception handling for validation errors, not-found errors, forbidden errors, bad requests, and unexpected server errors.
4. Bind application configuration into typed property classes instead of scattering raw config strings.
5. Create `local`, `lab`, and `prod` profiles and verify `prod` defaults to secure mode.
6. End-of-day check: switching profiles changes mode safely, and all errors use one JSON structure.
