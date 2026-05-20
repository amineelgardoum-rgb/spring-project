# Task 1: Admin User Management REST API (UC4, UC4-4)

## Description
Provide JSON endpoints for managing annotators.

## What we should implement
- `AdminUserController`: `@RestController` at `/api/admin/users`.
- `GET /api/admin/users`: List all active (`isDeleted=false`) users.
- `POST /api/admin/users`: Create user, auto-generate password, return credentials in JSON.
- `PUT /api/admin/users/{id}`: Update firstName, lastName, and/or username. Accepts `UpdateUserRequest` DTO with validation. Returns updated user DTO.
- `DELETE /api/admin/users/{id}`: Logical deletion.
- Protect controller with `@PreAuthorize("hasRole('ADMIN')")`.
- Use `Pageable` for `GET` to support pagination and sorting.
- Return DTOs (not entities) using MapStruct mappers.

See `architecture.md` in this folder for architectural decisions.