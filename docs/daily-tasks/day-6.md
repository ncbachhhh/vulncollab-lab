# Day 6 - Workspace Domain and Membership

## Goal

Implement workspace entities, membership roles, workspace CRUD APIs, and secure baseline access checks.

## Backend tasks completed

- Added `Workspace`, `WorkspaceMember`, `WorkspaceRole`, and `WorkspaceVisibility`.
- Added repositories for workspace lookup and workspace membership lookup.
- Added `WorkspaceAccessService` to centralize member and owner checks.
- Added `WorkspaceService` for list, create, detail, update, invite, and member listing.
- Added `WorkspaceController` for `/api/workspaces` routes.
- Kept secure baseline behavior: users can only list or view workspaces where they are members.
- Restricted update, invite, and member listing to workspace owners.

## Database tasks

- Reused existing `workspaces` and `workspace_members` schema from `V001__create_core_schema.sql`.
- Reused deterministic workspace and membership seed data from `V002__seed_lab_data.sql`.
- No new migration was required for Day 6 because the schema and seeds already matched the plan.

## Security lab notes

- `WorkspaceAccessService.requireMember` returns `WORKSPACE_NOT_FOUND` when membership is missing, so private workspace existence is not confirmed.
- Finance Internal and Admin Operations remain suitable IDOR targets for later vulnerable-mode work.
- Stable seeded `public_id` values remain available for writeups.

## API endpoints implemented

- `GET /api/workspaces`
- `POST /api/workspaces`
- `GET /api/workspaces/{publicId}`
- `PATCH /api/workspaces/{publicId}`
- `POST /api/workspaces/{publicId}/invite`
- `GET /api/workspaces/{publicId}/members`

## Files/folders created or modified

- `backend/src/main/java/com/vulncollab/workspace/`
- `backend/src/test/java/com/vulncollab/workspace/`
- `backend/src/test/java/com/vulncollab/BackendApplicationTests.java`
- `docs/API_DESIGN.md`
- `docs/daily-tasks/day-6.md`

## Manual test checklist

- Log in as Alice and list her workspaces.
- Log in as Bob and confirm Finance Internal is absent from normal workspace list.
- Create a workspace and confirm creator becomes OWNER.
- Invite Charlie as VIEWER.
- Confirm non-owner update fails in secure baseline.

## Verification

- `./mvnw.cmd test`

## Definition of Done

- Workspace membership model works.
- Seed workspaces and memberships remain deterministic.
- Workspace role checks are centralized in `WorkspaceAccessService`.
