# BigTask 4 — NLP Integration & Long-Running Tasks

## Goal
Integrate model training/inference in an asynchronous, scalable and observable manner.

## Hyperparameter Configuration
- `POST /api/admin/nlp/train` accepts a JSON body with an optional `hyperparameters` object:
  ```json
  {
    "hyperparameters": {
      "learningRate": 0.001,
      "epochs": 10,
      "batchSize": 32
    }
  }
  ```
- Pass these as command-line arguments or a temporary config file to the Python script.
- Store the hyperparameters in the training log for reproducibility.

## Results & Visualization
- `POST /api/admin/nlp/test` returns:
  ```json
  {
    "accuracy": 0.92,
    "f1Score": 0.90,
    "confusionMatrix": [[85, 5], [3, 7]]
  }
  ```
- The confusion matrix is stored in the training log (JSON column) for later retrieval.
- Frontend renders the confusion matrix as a styled HTML table or a simple grid.

## File Storage
- Store uploaded dataset files on a configurable base path; keep metadata and path in DB.
- Provide an abstraction (interface) for storage to allow migrating to S3 later.

## Asynchronous Processing
- Enable `@EnableAsync` and use `@Async` for CSV imports and model training.
- Return job IDs and implement job-status endpoints `GET /api/jobs/{id}`.
- Persist job metadata, progress and logs in a `Job` table.

## Python / Worker Integration
- Preferred: run a Python NLP worker in a separate Docker container and communicate over HTTP or a message broker (RabbitMQ/Redis Streams).
- Alternative: use a REST-based microservice for model inference.
- The Python environment can be a local `venv` or `conda` environment when running via `ProcessBuilder` directly. Use a configurable path to the Python executable and script directory.

## Resource Control
- Limit concurrent training jobs; provide queueing and backpressure.

## Verification
- Run a sample async CSV import and ensure job completes and artifacts are saved.
