# Task 2: Dataset Upload & Assignment API (UC2, UC3)

## Description
Endpoints to upload datasets (CSV/JSON) and trigger automatic assignment.

## What we should implement
- `POST /api/admin/datasets/upload`: Accepts `MultipartFile` and a `tags` string. Parses the file and creates `TextPair` rows.
- `GET /api/admin/datasets`: List datasets and their % completion.
- `POST /api/admin/datasets/{id}/assign`: Accepts a JSON list of `annotatorIds` and distributes pairs randomly ensuring redudancy of 3.