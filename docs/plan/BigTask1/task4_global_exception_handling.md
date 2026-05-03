# Task 4: Global Exception Handling

## Description
Since we are building a REST API, it is crucial to handle exceptions globally and return standardized JSON error responses instead of default HTML error pages or stack traces.

## What we should implement
Inside `pack.core.web` (or a sub-package `pack.core.web.exceptions`):
- **`GlobalExceptionHandler.java`**: A class annotated with `@RestControllerAdvice`.
- **`ErrorResponse.java`**: A DTO to structure the error messages (e.g., `timestamp`, `status`, `error`, `message`, `path`).
- Custom Exception classes (e.g., `ResourceNotFoundException`, `UnauthorizedException`, `SpammerDetectedException`).

## How to execute
- Create `GlobalExceptionHandler` and write methods annotated with `@ExceptionHandler(ExceptionClass.class)`.
- For example, catch `EntityNotFoundException` and return an `ErrorResponse` with HTTP 404 (Not Found).
- Catch `MethodArgumentNotValidException` to return HTTP 400 (Bad Request) with specific field validation errors.
- Ensure all unhandled exceptions return a clean generic JSON error with HTTP 500.