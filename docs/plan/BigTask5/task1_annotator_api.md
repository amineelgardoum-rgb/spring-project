# Task 1: Annotator Dashboard & Tasks API (UC6, UC7)

## Description
Endpoints serving the Annotator UI.

## What we should implement
- `GET /api/annotator/tasks`: Returns tasks assigned to the JWT's logged-in user, their deadlines, and percentage completions.
- `GET /api/annotator/tasks/{taskId}/pairs?page=0`: Returns a specific `TextPair` and available classes (tags).
- `POST /api/annotator/tasks/{taskId}/annotate`: Receives a JSON body `{textPairId: X, classId: Y}`, saves the `Annotation`, and returns success.