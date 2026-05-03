# Task 1: Entities (Users, Datasets, Annotations)

## Description
Define the entities mapping to MariaDB.

## What we should implement
- `AppUser` & `Role` (with logical deletion).
- `Dataset`, `AnnotationClass` (Tags), and `TextPair`.
- `AnnotationTask` (Assignments) and `Annotation` (Results).

## How to execute
- Use standard `@Entity` annotations.
- Replace any bidirectional cyclical references with `@JsonIgnore` or use separate DTOs so JSON serialization in REST doesn't fail with Infinite Recursion.