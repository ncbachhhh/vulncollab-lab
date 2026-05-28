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

## Current Day 2 Contract

Implemented foundation classes:

- `ApiResponse<T>`
- `ApiError`
- `HealthResponse`
- `GlobalExceptionHandler`

Future APIs must use this response contract unless there is a clear infrastructure reason not to, such as health checks or file downloads.
