# Task 1: Spring Boot REST Initialization

## Description
Set up the Spring Boot backend as a RESTful API provider with profiles, validation, MapStruct, OpenAPI, and caching.

## What to implement

### pom.xml dependencies
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `spring-boot-starter-cache`
- `mariadb-java-client` (runtime)
- `jjwt-api` / `jjwt-impl` / `jjwt-jackson` (0.11.5)
- `com.opencsv:opencsv` (5.8)
- `org.mapstruct:mapstruct` + `mapstruct-processor` (annotationProcessor)
- `org.springdoc:springdoc-openapi-starter-webmvc-ui`
- `com.github.ben-manes.caffeine:caffeine`
- `org.testcontainers:mariadb` (test)
- `lombok` (optional)

### Application profiles
Create 3 files:
- `src/main/resources/application.properties` — profile-agnostic (app name, active profile, `ddl-auto=update`)
- `src/main/resources/application-dev.properties` — local MariaDB (localhost:3306), debug logging
- `src/main/resources/application-prod.properties` — env-var-based config (DB, JWT secret), production logging

### Configuration classes
- `CorsConfig` — allow React origin (localhost:5173), methods, headers
- `SecurityConfig` — see task 3 for details
- `AsyncConfig` — `@EnableAsync` with a `ThreadPoolTaskExecutor` (core=4, max=8)
- `CacheConfig` — Caffeine cache with TTL for frequently-read data

### Schema management
- Hibernate `ddl-auto=update` creates/updates tables from entity annotations on startup
- No manual migration files needed

### DataInitializer
- `com.ensah.nlp_annotation_platform.seed.DataInitializer`
- `@Component` implementing `CommandLineRunner`
- On first run, if no user with admin role exists, create one:
  - Username: from `app.admin.username` property (default: `admin`)
  - Password: from `app.admin.password` property (default: `admin123`)

## How to verify
1. `mvn clean compile` succeeds
2. App starts and connects to MariaDB
3. Hibernate auto-creates tables on startup
4. Admin user is created automatically on first run
5. Swagger UI available at `/swagger-ui/index.html`

See `architecture.md` in this folder for architectural decisions.
