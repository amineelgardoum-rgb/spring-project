# Task 3: Metrics & Export API (UC5)

## Description
Endpoints to compute Kappa metrics, find spam, and export JSON/CSV data.

## What we should implement
- `GET /api/admin/datasets/{id}/metrics`: Returns a JSON with `fleissKappa` score and an array of `spammerIds`.
- `GET /api/admin/datasets/{id}/export`: Generates the CSV string and returns it with header `Content-Type: text/csv`.