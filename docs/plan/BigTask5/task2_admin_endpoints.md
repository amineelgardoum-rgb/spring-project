# Task 2: Admin Dashboard & Annotation Correction Endpoints

## Description
Endpoints serving the Admin UI dashboard and annotation correction feature.

## What we should implement

### 1. `GET /api/admin/dashboard/stats`
Returns global aggregation data for the admin dashboard home page.

```json
{
  "totalDatasets": 10,
  "totalTexts": 5000,
  "totalAnnotators": 25,
  "totalAnnotations": 3400,
  "overallAnnotationPercent": 68.0,
  "globalClassDistribution": { "positif": 1200, "negatif": 1100, "neutre": 1100 },
  "annotatorProgress": [
    {
      "annotatorId": 1,
      "firstName": "Jean",
      "lastName": "Dupont",
      "annotatedCount": 150,
      "avgTimePerAnnotation": 12.5,
      "classDistribution": { "positif": 60, "negatif": 50, "neutre": 40 }
    }
  ],
  "spammerIds": [3, 7]
}
```

- `totalDatasets` — count all datasets
- `totalTexts` — count all `TextItem` rows
- `totalAnnotators` — count all `User` with role `ANNOTATOR` where `deleted = false`
- `totalAnnotations` — count all `Annotation` rows
- `overallAnnotationPercent` — `(totalAnnotations / (totalTexts * assignedAnnotators)) * 100` if possible, else approximate
- `globalClassDistribution` — across all datasets, group by `Annotation.label`
- `annotatorProgress` — for each annotator (with `ANNOTATOR` role), aggregate their annotations across all datasets, compute average time (if `createdAt` diff is trackable), and their per-label distribution
- `spammerIds` — list of annotator IDs flagged as spammers (e.g., > 95% same label, or avg time < 2s). The heuristic can be simple: flag annotators whose annotations are 95%+ a single label

Use `@PreAuthorize("hasRole('ADMIN')")`.

### 2. `GET /api/admin/annotations?textItemId={textItemId}`
Returns all annotations for a given text item (so the admin can view and correct them).

- Query param: `textItemId` (Long, required)
- Returns a list:

```json
[
  {
    "id": 1,
    "annotatorId": 5,
    "annotatorName": "jdupont",
    "label": "positif",
    "comment": null,
    "createdAt": "2026-05-20T10:30:00Z"
  }
]
```

- Use `@PreAuthorize("hasRole('ADMIN')")`
- Add a corresponding method in `AnnotationRepository`: `findByTextItem_Id(Long textItemId)`

### 3. `PUT /api/admin/annotations/{id}`
Allows the admin to correct an annotation's label and/or add a comment.

- Request body:
```json
{
  "label": "negatif",
  "comment": "Correction manuelle par l'admin — l'annotateur a mal classé"
}
```

- Response: the updated annotation DTO
- Use `@PreAuthorize("hasRole('ADMIN')")`
- Throw `ResourceNotFoundException` if the annotation does not exist
- Validate that the new label is valid for the annotation's dataset (`dataset.labels` contains the label)

## DTOs

Create these DTOs under `dto/response/admin/`:
- `DashboardStatsResponse` — top-level stats object
- `AnnotatorProgressEntry` — per-annotator entry inside `annotatorProgress`
- `AnnotationAdminResponse` — annotation view/correction DTO

Create under `dto/request/admin/`:
- `UpdateAnnotationRequest` — `{ label: String, comment: String }`

## Which files to create/modify

| File | Action |
|------|--------|
| `controller/AdminDashboardController.java` | **Create** — `@RestController @RequestMapping("/api/admin/dashboard")` with `getStats()` |
| `service/admin/AdminDashboardService.java` | **Create** — interface with `getStats()` |
| `service/admin/impl/AdminDashboardServiceImpl.java` | **Create** — implementation aggregating stats from all repositories |
| `controller/AdminAnnotationController.java` | **Create** — `@RestController @RequestMapping("/api/admin/annotations")` with `listAnnotations(textItemId)` and `updateAnnotation(id, request)` |
| `service/admin/AdminAnnotationService.java` | **Create** — interface |
| `service/admin/impl/AdminAnnotationServiceImpl.java` | **Create** — implementation |
| `repository/AnnotationRepository.java` | **Modify** — add `findByTextItem_Id(Long textItemId)` |
| DTOs under `dto/response/admin/` and `dto/request/admin/` | **Create** as listed above |

## Spammer heuristic (simple)
In `AdminDashboardServiceImpl.getStats()`, for each annotator:
- Count their annotations per label
- If any single label accounts for >= 95% of their total annotations → flag as spammer
- Also compute `avgTimePerAnnotation` by grouping annotations and checking time between consecutive annotations (if available)

## Verification
1. `GET /api/admin/dashboard/stats` returns valid JSON with all fields
2. `GET /api/admin/annotations?textItemId=1` returns the annotations for that text item
3. `PUT /api/admin/annotations/1` with a valid `{ "label": "negatif" }` updates the annotation
4. `PUT /api/admin/annotations/999` returns 404
5. All endpoints reject non-ADMIN roles with 403
6. Existing tests still pass

See `architecture.md` in this folder for architectural decisions.
