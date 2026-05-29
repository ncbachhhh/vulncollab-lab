# VulnCollab Lab API Design

This document records API conventions that must stay stable as backend modules are added.

## Base URL

Local backend:

```text
http://localhost:8080
```

API routes use the `/api` prefix unless they are Spring Actuator endpoints.

## Health Endpoint

```http
GET /api/health
```

Health is intentionally not wrapped in `ApiResponse` so infrastructure and frontend bootstrapping can read it directly.

Example response:

```json
{
  "status": "UP",
  "service": "vulncollab-backend",
  "timestamp": "2026-05-28T03:41:53.148117400Z",
  "profiles": ["local"],
  "environment": "local",
  "publicDomain": "lab.example.com",
  "vulnerableMode": true,
  "labWarningEnabled": true,
  "challengeSubmitEnabled": true,
  "secureModeBannerEnabled": true,
  "labAccessRestrictionRequired": false,
  "labNoindexRequired": true,
  "labRobotsDisallowRequired": true
}
```

Health responses must never include secrets, database credentials, Redis details, mail credentials, internal service URLs, or filesystem paths.

## Standard Success Response

Business APIs should return `ApiResponse<T>`.

```json
{
  "success": true,
  "data": {
    "example": "value"
  },
  "timestamp": "2026-05-28T03:41:53.148117400Z"
}
```

Rules:

- `success` is `true`.
- `data` contains the endpoint payload.
- `error` is omitted when null.
- `timestamp` is generated server-side.

## Standard Error Response

Errors should return `ApiResponse.failure(ApiError)`.

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed",
    "details": {
      "email": "must be a well-formed email address"
    }
  },
  "timestamp": "2026-05-28T03:41:53.148117400Z"
}
```

Rules:

- `success` is `false`.
- `data` is omitted when null.
- `error.code` is stable enough for frontend branching.
- `error.message` is safe for display.
- `error.details` may contain field-level validation errors.
- Stack traces and internal exception class names must not be returned.

## Error Code Mapping

| Condition | HTTP status | Error code |
|---|---:|---|
| Bad request domain error | 400 | `BAD_REQUEST` or domain-specific code |
| Validation failure | 400 | `VALIDATION_ERROR` |
| Missing request parameter | 400 | `MISSING_REQUEST_PARAMETER` |
| Invalid request parameter | 400 | `INVALID_REQUEST_PARAMETER` |
| Malformed JSON/body | 400 | `MALFORMED_REQUEST` |
| Missing/invalid authentication | 401 | `UNAUTHORIZED` |
| Authenticated but forbidden | 403 | `FORBIDDEN` |
| Resource not found | 404 | `NOT_FOUND` |
| Duplicate/conflict state | 409 | `CONFLICT` |
| Unexpected server error | 500 | `INTERNAL_SERVER_ERROR` |

## Exception Handling

Domain services should throw exceptions from `com.vulncollab.common.error`:

- `BadRequestException`
- `UnauthorizedException`
- `ForbiddenException`
- `NotFoundException`
- `ConflictException`

`GlobalExceptionHandler` owns HTTP mapping and response shape. Controllers should not manually duplicate error JSON.

## Auth Contract

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
GET /api/auth/me
```

Login and registration return both a short-lived access token and a one-time refresh token:

```json
{
  "success": true,
  "data": {
    "token": "access.jwt.value",
    "expiresAt": "2026-05-29T11:00:00Z",
    "refreshToken": "opaque-refresh-token",
    "refreshExpiresAt": "2026-06-05T10:00:00Z",
    "user": {
      "publicId": "22222222-2222-4222-8222-222222222222",
      "email": "alice@test.com",
      "displayName": "Alice Nguyen",
      "role": "USER"
    }
  },
  "timestamp": "2026-05-29T10:00:00Z"
}
```

Refresh tokens are opaque random values. The server stores only a SHA-256 digest in `refresh_tokens`, plus expiry, revocation time, created IP, and user agent. Refresh uses rotation: a successful `POST /api/auth/refresh` revokes the submitted refresh token and returns a new access token plus a new refresh token. Reusing an old, revoked, expired, blank, or unknown refresh token returns `401 INVALID_REFRESH_TOKEN`.

Logout accepts the current refresh token and revokes it:

```json
{
  "refreshToken": "opaque-refresh-token"
}
```

Frontend storage assumption for the initial React build: keep the access token in memory when possible and persist the refresh token only if the UX needs session restore across page reloads. If persistence is used, treat the refresh token as sensitive client-side state and clear it on logout, failed refresh, and account switch. Do not trust any client-side token state without server verification.

## Current Day 5 Contract

Implemented foundation classes:

- `ApiResponse<T>`
- `ApiError`
- `HealthResponse`
- `GlobalExceptionHandler`
- Access-token auth with JWT
- Refresh-token persistence, rotation, and logout

Future APIs must use this response contract unless there is a clear infrastructure reason not to, such as health checks or file downloads.
