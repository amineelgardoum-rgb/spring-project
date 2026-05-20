# Task 3: Metrics & Export API (UC5)

## Description
Endpoints to compute Kappa metrics, find spam, and export JSON/CSV data.

## What we should implement
- `GET /api/admin/datasets/{id}/metrics`: Returns a JSON with `fleissKappa` score and an array of `spammerIds`.
- `GET /api/admin/datasets/{id}/export?format=csv|json`: Generates the export string. If format=csv returns `Content-Type: text/csv`; if format=json returns `Content-Type: application/json`. Default is csv.
  - **Columns**: `id`, `texte`, `classe`, `annotateur`, `date_annotation`. The `texte` column contains the primary text (or concatenation of Text1 and Text2 for pair datasets).
- `GET /api/admin/annotations?textPairId=X`: Returns all annotations for a given text pair. Used by admin to review/correct annotations.
- `PUT /api/admin/annotations/{id}`: Accepts corrected annotation body. Overrides the existing annotation value. Records who corrected and when for traceability.
- Protect controller with `@PreAuthorize("hasRole('ADMIN')")`.
- Use `StreamingResponseBody` for export to avoid memory pressure.
- Metrics computation should be done in a `@Service` class (not in controller).

See `architecture.md` in this folder for architectural decisions.