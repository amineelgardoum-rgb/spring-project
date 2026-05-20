# Task 2: Dataset Upload & Assignment API (UC2, UC3)

## Description
Endpoints to upload datasets (CSV/JSON) and trigger automatic assignment.

## What we should implement
- `POST /api/admin/datasets/upload`: Accepts `MultipartFile` and a `tags` string. Parses the file and creates `TextItem` rows.
  - **Validation**: Each row must have at least `id` and `texte` fields. Reject rows missing these with a clear error. For text pair datasets, also accept `texte2` column.
- `GET /api/admin/datasets`: List datasets and their % completion.
- `GET /api/admin/datasets/{id}`: Returns full dataset detail including list of text pairs and assigned annotators with their progress.
- `POST /api/admin/datasets/{id}/assign`: Accepts a JSON list of `annotatorIds` and distributes pairs randomly ensuring redundancy of 3.
- `DELETE /api/admin/datasets/{id}/annotators/{userId}`: Remove an annotator from a dataset. Existing annotations are preserved (not deleted).
- Protect controller with `@PreAuthorize("hasRole('ADMIN')")`.
- Use `@Transactional` at service layer for dataset import and assignment operations.
- Return DTOs and use streaming responses for large exports.
- Dataset CSV/JSON parsing should run asynchronously with job status tracking.

See `architecture.md` in this folder for architectural decisions.