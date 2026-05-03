# Task 1: NLP Execution REST API (UC 5.1 & 5.2)

## Description
Endpoints to launch Python scripts from the React GUI.

## What we should implement
- `POST /api/admin/nlp/train`: Triggers `ProcessBuilder` for `train.py`.
- `POST /api/admin/nlp/test`: Triggers `test.py` and returns metrics in JSON format (`{accuracy: 0.92, f1: 0.90}`).
- `GET /api/admin/nlp/logs`: Returns historical `NLPTrainingLog` entries.