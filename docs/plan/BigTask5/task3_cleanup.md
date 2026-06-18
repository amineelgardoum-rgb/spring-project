# Task 3: Cleanup — Unused fields & Type Safety Warnings

## Description
Resolve compiler/IDE warnings (unused fields, unchecked conversions) to keep the codebase clean.

## Changes

### 1. Remove unused `Logger log` from service classes
- **`MetricsServiceImpl.java`** — removed unused `log` field and its imports
- **`AnnotatorServiceImpl.java`** — removed unused `log` field and its imports

### 2. Use unused `assignmentRepository` in `AdminDashboardServiceImpl`
- Added `totalAssignments` count via `assignmentRepository.count()`
- `totalAnnotators` now counts only annotators with at least one assignment (using distinct IDs from `assignmentRepository`)
- `annotatorProgress` loops only over assigned annotators (via `findAllById`) instead of all users
- Added `totalAssignments` field to `DashboardStatsResponse.java`

### 3. Fix unchecked `Map.class` conversion in JSON parsing
- `DatasetServiceImpl.parseJsonLines()` — added `@SuppressWarnings("unchecked")` on the method

### 4. Fix `Long::sum` null type safety warning
- `MetricsServiceImpl.computeFleissKappa()` — replaced `Long::sum` with `(a, b) -> a + b` lambda to avoid unchecked `Long → long` conversion

### 5. Use previously unused `totalAnnotations` variable
- Added `totalAnnotations` field to `DatasetResponse.java`
- `DatasetServiceImpl.toDatasetResponse()` now sets `dto.setTotalAnnotations(totalAnnotations)` instead of discarding the value

## Files modified
| File | Change |
|------|--------|
| `dto/response/dataset/DatasetResponse.java` | Added `totalAnnotations` field |
| `dto/response/admin/DashboardStatsResponse.java` | Added `totalAssignments` field |
| `service/dataset/impl/DatasetServiceImpl.java` | Set `totalAnnotations` on DTO; added `@SuppressWarnings("unchecked")` |
| `service/metrics/impl/MetricsServiceImpl.java` | Removed unused `log`; replaced `Long::sum` lambda |
| `service/annotator/impl/AnnotatorServiceImpl.java` | Removed unused `log` |
| `service/admin/impl/AdminDashboardServiceImpl.java` | Used `assignmentRepository` for counts + annotator scoping |
