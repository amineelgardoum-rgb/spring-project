# Task 1: Entities (Users, Datasets, Annotations)

## Description
Define the entities mapping to MariaDB.

## What we should implement
- `User` (`@Table(name = "users")`) & `Role` (with logical deletion). Fields: `id`, `firstName` (nom), `lastName` (prénom), `username` (login, unique, indexed), `passwordHash` (BCrypt), `enabled`, `deleted` (logical deletion), `createdAt`, `updatedAt`.
- `Dataset`, `AnnotationClass` (Tags), and `TextItem` (single text or text pair).
- `AnnotationTask` (Assignments) and `Annotation` (Results).
- `NlpTrainingLog` — id, userId, hyperparameters (JSON), metrics (JSON: accuracy, f1Score, confusionMatrix), status (PENDING/RUNNING/SUCCESS/FAILED), executionLogs (TEXT), startedAt, completedAt.

## How to execute
- Use standard `@Entity` annotations.
- Replace any bidirectional cyclical references with `@JsonIgnore` or use separate DTOs so JSON serialization in REST doesn't fail with Infinite Recursion.
- Enable `@EnableJpaAuditing` in the main application class and add `@CreatedDate` / `@LastModifiedDate` on entities.
- Use `@Version` for optimistic locking on `Annotation` entity.
- Add DB indexes on `username`, `dataset_id`, `text_item_id`, `created_at` for query performance.

See `architecture.md` in this folder for architectural decisions.