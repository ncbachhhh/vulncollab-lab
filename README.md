# VulnCollab Lab

VulnCollab Lab is a Spring Boot and React vulnerable collaboration platform for legal, owner-controlled web security training.

Current status: project and database foundation are initialized. See `docs/DAILY_BUILD_PLAN.md` for the full implementation plan.

## Local Dependencies

```powershell
docker compose up -d mysql redis mailhog
```

Backend defaults to the `local` profile and connects to MySQL at `localhost:3306`.

## Safety

Vulnerable mode is for local or protected owner-controlled lab use only. Do not expose the vulnerable lab publicly without access control.
