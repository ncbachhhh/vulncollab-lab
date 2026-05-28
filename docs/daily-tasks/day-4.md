# Day 4 - Register, Login, JWT, Security Filter, and Me Endpoint

## Goal

Implement registration, login, JWT access tokens, Spring Security authentication, and current-user lookup.

## Backend tasks

- Add `AuthController`, `AuthService`, request/response DTOs.
- Implement `POST /api/auth/register`.
- Implement `POST /api/auth/login`.
- Implement JWT service for signing and validating access tokens.
- Add authentication filter that reads `Authorization: Bearer <token>`.
- Implement `GET /api/auth/me`.
- Add `UserPrincipal` or equivalent security principal.
- Configure route authorization: auth routes public, health public, application routes authenticated by default.

## Frontend tasks

- None.

## Database tasks

- No new tables.
- Registration creates users with `USER` role only.
- Newly registered users receive UUID string `public_id` values; the numeric database primary key stays internal.

## Security lab tasks

- Use configured weak JWT secret only in local vulnerable profile for later challenge.
- Document that prod must override JWT secret with a strong env var.

## API endpoints affected

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

## Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/auth/`
- `backend/src/main/java/com/vulncollab/security/JwtService.java`
- `backend/src/main/java/com/vulncollab/security/JwtAuthenticationFilter.java`
- `backend/src/main/java/com/vulncollab/security/SecurityConfig.java`

## Manual test checklist

- Register a new user.
- Log in as `alice@test.com`.
- Call `/api/auth/me` with token and confirm profile is returned.
- Call `/api/auth/me` without token and confirm `401`.
- Try duplicate registration and confirm `409`.

## Definition of Done

- JWT login flow works.
- Protected routes require authentication.
- User data is returned through safe DTOs.

## Notes for implementation agent

- Do not include refresh tokens yet.
- Keep access token response shape stable for frontend: token, expiresAt, user.

## Detailed execution

1. Implement register DTOs with email, display name, and password validation.
2. Implement login DTOs and safe auth responses containing access token, expiry, and user summary.
3. Add JWT signing, validation, subject extraction, and authority extraction.
4. Add the Spring Security JWT filter and configure public versus protected routes.
5. Implement `/api/auth/me` using the authenticated principal, not a user ID supplied by request.
6. End-of-day check: Alice can log in and call `/api/auth/me`; anonymous users cannot.
