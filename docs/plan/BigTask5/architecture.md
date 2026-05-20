# BigTask 5 — Annotator APIs & Workflow

## Goal
Provide scalable, consistent annotation workflows for annotators with concurrency controls.

## Endpoints
- `GET /api/annotator/tasks` — paged, filterable tasks assigned to the annotator.
- `GET /api/annotator/tasks/{taskId}/pairs?page=0` — returns a specific `TextPair` and available classes (tags).
- `POST /api/annotator/tasks/{taskId}/annotate` — submit annotation; validate and persist.
- `GET /api/annotator/stats` — personal progress and accuracy metrics.

## DTOs and Mapping
- Always return DTOs (never expose entities directly).
- Use MapStruct for mapping between entities and DTOs.

## Concurrency
- Use optimistic locking for assignments and/or `@Version` on entities to prevent lost updates.

## Bulk & Performance
- Support batch annotation uploads and process them asynchronously.
- Use `Pageable` and streaming for large datasets; cache frequently-read metadata.

## Verification
- Unit tests for annotation logic and concurrency scenarios.
