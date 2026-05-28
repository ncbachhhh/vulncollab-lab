# Day 3 - User Model, Roles, Passwords, and Seed Users

## Goal

Create user persistence, role model, password hashing, and deterministic seed users.

## Backend tasks

- Implement `User` entity with internal ID, public ID, email, password hash, display name, role, enabled status, timestamps.
- Define global roles: `USER`, `ADMIN`.
- Add `UserRepository`.
- Add `PasswordEncoder` bean using BCrypt.
- Add seed data service or migration-based seed script.
- Ensure seed passwords are BCrypt hashed, not stored as plaintext.

## Frontend tasks

- None.

## Database tasks

- Create `users` table.
- Add unique constraints on `email` and `public_id`.
- Seed:
  - `admin@test.com` / `Admin123!` / `ADMIN`
  - `alice@test.com` / `Password123!` / `USER`
  - `bob@test.com` / `Password123!` / `USER`
  - `charlie@test.com` / `Password123!` / `USER`

## Security lab tasks

- Document that seed accounts are lab-only.
- Do not use seed passwords in production.

## API endpoints affected

- None yet, except health may report database connected.

## Files/folders to create or modify

- `backend/src/main/java/com/vulncollab/user/User.java`
- `backend/src/main/java/com/vulncollab/user/UserRole.java`
- `backend/src/main/java/com/vulncollab/user/UserRepository.java`
- `backend/src/main/java/com/vulncollab/security/PasswordConfig.java`
- `backend/src/main/resources/db/migration/V001__create_users.sql`
- `backend/src/main/resources/db/migration/V007__seed_initial_data.sql` or dedicated seed migration

## Manual test checklist

- Recreate database from scratch.
- Confirm users table exists.
- Confirm four seed users exist.
- Confirm password values are BCrypt hashes.

## Definition of Done

- User table and seed accounts are reproducible.
- Backend starts cleanly against an empty database.

## Notes for implementation agent

- Use deterministic public IDs for seed records so writeups can refer to stable examples.
- Never expose `password_hash` in API DTOs.

## Detailed execution

1. Design the `users` table with internal numeric ID and external `public_id`.
2. Add the `User` entity, role enum, repository, and password encoder configuration.
3. Seed admin, Alice, Bob, and Charlie with BCrypt password hashes.
4. Confirm registration later cannot assign ADMIN role directly.
5. Confirm seed data is deterministic so writeups can reference stable accounts.
6. End-of-day check: dropping and recreating the database produces the same four users.
