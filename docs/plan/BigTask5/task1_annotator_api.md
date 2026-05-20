# Task 1: Annotator Dashboard & Tasks API (UC6, UC7)

## Description
Endpoints serving the Annotator UI.

## What we should implement
- `GET /api/annotator/tasks`: Returns tasks assigned to the JWT's logged-in user, their deadlines, and percentage completions.
- `GET /api/annotator/tasks/{taskId}/pairs?page=0`: Returns a specific `TextPair` and available classes (tags).
- `POST /api/annotator/tasks/{taskId}/annotate`: Receives a JSON body `{textPairId: X, classId: Y}`, saves the `Annotation`, and returns success.
- `GET /api/annotator/stats`: Returns personal statistics for the authenticated annotator:
  ```json
  {
    "totalAnnotated": 45,
    "avgTimePerAnnotation": 12.5,
    "classDistribution": {
      "positive": 20,
      "negative": 15,
      "neutral": 10
    }
  }
  ```
- Always return DTOs (never expose entities directly). Use MapStruct for mapping.
- Use `@PreAuthorize("hasRole('ANNOTATOR')")` for protection.
- Use optimistic locking (`@Version` on `Annotation` entity) to prevent lost updates.
- Support pagination (`Pageable`) for task listing.
- The annotator identity is derived from the JWT (not from request body).

See `architecture.md` in this folder for architectural decisions.