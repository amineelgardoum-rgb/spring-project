# BigTask 1 — Spring Boot Foundation & Security

## Goal
Harden the REST backend so it follows Spring Boot best practices and is production-ready.

## What to add (high level)
- Dependency updates to `pom.xml` (validation, MapStruct, OpenAPI, cache, testcontainers).
- Application profiles and tuned configuration files (`application-dev.properties`, `application-prod.properties`).
- Layered packages: `domain`, `dto`, `mapper`, `repository`, `service`, `controller`, `config`, `exception`, `batch`.
- Global exception handler using `@ControllerAdvice`.
- CORS configuration and `SecurityConfig` (stateless JWT).
- Validation (`spring-boot-starter-validation`) and standardized validation error responses.

## Dependencies to add (pom.xml)
- `spring-boot-starter-validation`
- `org.mapstruct:mapstruct` + `mapstruct-processor` (annotationProcessor)
- `org.springdoc:springdoc-openapi-starter-webmvc-ui`
- `org.springframework.boot:spring-boot-starter-cache` and `com.github.ben-manes.caffeine:caffeine` (or Redis starter for prod)
- `org.testcontainers:mariadb` for integration tests

## App configuration (files to create)
- `src/main/resources/application-dev.properties` — local dev DB, logging.
- `src/main/resources/application-prod.properties` — production-safe defaults (no embedded credentials; read from env vars).
- Keep `src/main/resources/application.properties` minimal and profile-agnostic with `spring.jpa.hibernate.ddl-auto=update`.

## Security
- Implement `JwtUtils`, `AuthTokenFilter` (extends `OncePerRequestFilter`), `UserDetailsServiceImpl`, and `SecurityConfig`.
- Use stateless session management and add CORS config into security chain.
- Implement refresh token flow (persist refresh tokens in DB with expiry).
- Enable method-level security with `@EnableMethodSecurity` and use `@PreAuthorize` on admin endpoints.

## Validation & Error Handling
- Annotate DTOs with validation constraints and use `@Valid` in controller method params.
- Add `GlobalExceptionHandler` (`@ControllerAdvice`) to map `MethodArgumentNotValidException`, `ConstraintViolationException`, and custom exceptions to a consistent error payload.

## OpenAPI & Docs
- Add `springdoc-openapi` and document controllers and DTOs so Swagger UI is available at `/swagger-ui/index.html`.

## Admin Bootstrapping (Seed)
- `DataInitializer.java` (`@Component`, `CommandLineRunner`): on first startup, if no admin user exists, create one with credentials from `app.admin.username` / `app.admin.password` properties (defaults: `admin` / `admin123`)
- Hibernate auto-creates tables from entity annotations (`ddl-auto=update`)

## Verification
1. `mvn -DskipTests=false clean verify` should build successfully.
2. Start app and open Swagger UI to check endpoints and authentication.
3. On first start, Hibernate creates tables and DataInitializer seeds the admin account.

## Notes
- Use environment variables for secrets (e.g. `JWT_SECRET`, DB credentials).
- Prefer MapStruct for DTO mapping (compile-time checks and performance).
