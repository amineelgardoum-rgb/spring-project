# Task 1: NLP Execution REST API (UC 5.1 & 5.2)

## Description
Endpoints to launch Python scripts from the React GUI.

## What we should implement
- `POST /api/admin/nlp/train`: Triggers `ProcessBuilder` for `train.py`. Accepts optional hyperparameters in request body: `{ hyperparameters: { learningRate: 0.001, epochs: 10, batchSize: 32 } }`. Passed as args or temp config to the Python script.
- `POST /api/admin/nlp/test`: Triggers `test.py` and returns metrics in JSON format including confusion matrix: `{ accuracy: 0.92, f1Score: 0.90, confusionMatrix: [[85,5],[3,7]] }`.
- `GET /api/admin/nlp/logs`: Returns historical `NlpTrainingLog` entries (id, date, userId, hyperparameters, metrics JSON, status, executionLogs).
- Protect controller with `@PreAuthorize("hasRole('ADMIN')")`.
- NLP execution must be async (`@Async`): return a job ID immediately, client polls `GET /api/jobs/{id}` for status.
- Persist job metadata (start time, status, logs, results) in a `Job` table.
- Use `ProcessBuilder` to call Python scripts, capture stdout/stderr.
- Limit concurrent NLP jobs to prevent resource exhaustion.

See `architecture.md` in this folder for architectural decisions.