# BigTask 3 — Admin APIs & Management

## Goal
Secure and implement admin APIs for user and dataset management, assignment, and export.

## Endpoints
- `POST /api/admin/users` — create user (enforce password policy).
- `GET /api/admin/users` — paged list with filters (use `Pageable`).
- `PUT /api/admin/users/{id}` — update user (firstName, lastName, username).
- `DELETE /api/admin/users/{id}` — soft-delete user.
- `POST /api/admin/datasets/upload` — upload dataset (multipart), store metadata and path.
- `GET /api/admin/datasets/{id}` — dataset detail with pairs and assigned annotators.
- `POST /api/admin/datasets/{id}/assign` — assign dataset items to annotators (idempotent, batch-safe).
- `DELETE /api/admin/datasets/{id}/annotators/{userId}` — de-assign annotator, keep existing annotations.
- `GET /api/admin/datasets/{id}/metrics` — aggregated annotation metrics (Cohen’s Kappa, Fleiss’ Kappa).
- `GET /api/admin/datasets/{id}/export?format=csv|json` — streaming export (CSV/JSON) using `StreamingResponseBody`.
- `GET /api/admin/annotations?textPairId=X` — list annotations for a text pair (admin correction).
- `PUT /api/admin/annotations/{id}` — correct/override an annotation.
- `GET /api/admin/dashboard/stats` — aggregated global stats for admin dashboard: total texts annotated, total annotators, global class distribution, per-annotator progress summaries.

## Security
- Protect with `@PreAuthorize("hasRole('ADMIN')")` or similar method-level checks.
- Audit admin actions (user, timestamp, action).

## Pagination & Export
- All list endpoints must accept `Pageable` and return `Page<TDto>`.
- Use streaming responses for exports to avoid memory pressure.

## Verification
- Integration tests for admin flows using Testcontainers (MariaDB).
