# BigTask 2 — Entities, DB Schema

## Goal
Define normalized JPA entities with auditing and appropriate indexes.

## Entities (suggested)
- `User` (`@Table(name = "users")`) — `id`, `firstName` (nom), `lastName` (prénom), `username` (login, unique + index), `passwordHash` (BCrypt), `roles`, `enabled`, `deleted`, `createdAt`, `updatedAt`.
- `Dataset` — `id`, `name`, `description`, `filePath`, `numRecords`, `createdBy`, `createdAt`.
- `TextItem` — `id`, `dataset_id` (indexed), `content`, `pairContent?`, `metadata` (JSON), `createdAt`.
- `Annotation` — `id`, `text_item_id` (indexed), `annotator_id` (indexed), `label`, `comment`, `createdAt`.
- `RefreshToken` — `token`, `user_id`, `expiry`.
- `NlpTrainingLog` — `id`, `user_id` (indexed), `hyperparameters` (JSON), `metrics` (JSON: accuracy, f1Score, confusionMatrix), `status` (PENDING/RUNNING/SUCCESS/FAILED), `executionLogs` (TEXT), `startedAt`, `completedAt`.

## Auditing
- Enable `@EnableJpaAuditing` in `NlpAnnotationPlatformApplication` and add `@CreatedDate` / `@LastModifiedDate` fields on entities (or a `BaseEntity`).

## Indexes & Constraints
- Add DB indexes on `username`, `dataset_id`, `text_item_id`, `createdAt` for query performance via JPA annotation.

## Schema Management
- Hibernate `ddl-auto=update` creates/updates tables from entity JPA annotations on startup.

## Transactions
- Use `@Transactional` at service layer; prefer `readOnly=true` for read operations.

## Verification
1. Start app with empty database and confirm tables are auto-created.
2. Run integration tests with Testcontainers to validate schema and queries.
