# Task 1: Admin User Management REST API (UC4, UC4-4)

## Description
Provide JSON endpoints for managing annotators.

## What we should implement
- `AdminUserController`: `@RestController` at `/api/admin/users`.
- `GET /api/admin/users`: List all active (`isDeleted=false`) users.
- `POST /api/admin/users`: Create user, auto-generate password, return credentials in JSON.
- `DELETE /api/admin/users/{id}`: Logical deletion.