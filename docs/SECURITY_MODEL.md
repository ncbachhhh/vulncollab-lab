# VulnCollab Lab Security Model

VulnCollab Lab is intentionally vulnerable only in controlled lab mode. Production must default to secure behavior.

## Profiles

### local

Local development profile.

Expected behavior:

- `app.vulnerable-mode=true` is allowed.
- Lab warning may be enabled.
- Access restriction is not required for local-only development.
- MySQL, Redis, and MailHog may expose local host ports for development.

### lab

Owner-controlled vulnerable lab profile.

Expected behavior:

- `app.vulnerable-mode=true` may be enabled.
- Lab warning must be enabled.
- Access restriction is required before public exposure.
- Recommended controls include HTTP basic auth, IP allowlist, VPN-only access, or equivalent protection.
- Noindex and robots disallow should be enabled.
- Internal services must not expose public host ports.

### prod

Normal production profile.

Expected behavior:

- `app.vulnerable-mode=false`.
- `deployment.expose-vulnerable-lab=false`.
- The app fails startup if prod is configured with vulnerable mode enabled.
- The app fails startup if prod is configured to expose the vulnerable lab.
- Real secrets must come from environment variables or a secret manager, not source control.

## Startup Safety Guard

`StartupSafetyValidator` enforces profile safety at startup.

It rejects:

- `deployment.environment=prod` with `app.vulnerable-mode=true`
- `deployment.environment=prod` with `deployment.expose-vulnerable-lab=true`

It logs safe startup state:

- environment
- vulnerable mode flag
- vulnerable lab exposure flag
- lab warning flag

It must not log:

- JWT secret
- database password
- Redis connection secrets
- mail credentials
- internal service URLs
- filesystem paths

## Lab Safety Properties

Typed lab safety config is bound under `lab.safety`.

Current fields:

- `warning-banner-required`
- `access-restriction-required`
- `basic-auth-recommended`
- `ip-allowlist-recommended`
- `vpn-recommended`
- `noindex-required`
- `robots-disallow-required`
- `legal-warning`

These properties support deployment checks, frontend banners, and documentation. They do not replace actual Nginx, VPN, firewall, or authentication controls.

## Vulnerable Mode Rule

Vulnerable behavior must be explicitly gated by config:

```yaml
app:
  vulnerable-mode: true
```

Secure behavior must be the default production behavior:

```yaml
app:
  vulnerable-mode: false
```

Every challenge implementation must clearly separate:

- vulnerable behavior
- expected exploitation path
- secure behavior
- deployment safety guard

## Error Handling Security

API errors use `GlobalExceptionHandler` and `ApiResponse.failure(...)`.

Rules:

- Do not expose stack traces to clients.
- Do not expose internal exception class names.
- Do not expose SQL errors or filesystem paths.
- Log unexpected errors server-side.
- Return stable error codes for frontend behavior.

## Health Endpoint Safety

`GET /api/health` may expose mode and banner metadata for frontend bootstrapping.

It may include:

- status
- service name
- active profiles
- environment
- public domain
- vulnerable mode flag
- lab warning flag
- challenge submit flag
- secure mode banner flag
- lab noindex/robots/access-restriction requirements

It must not include:

- JWT secret
- database credentials
- Redis details
- mail credentials
- internal service URLs
- real deployment secrets
- filesystem storage paths

## Day 2 Security Baseline

Day 2 establishes:

- typed configuration properties
- validated config binding
- profile-specific defaults
- startup guard for production safety
- consistent non-leaky error responses
- health response safe for frontend banners
