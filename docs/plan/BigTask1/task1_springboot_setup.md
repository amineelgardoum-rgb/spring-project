# Task 1: Spring Boot REST Initialization

## Description
Set up the Spring Boot backend as a RESTful API provider instead of MVC.

## What we should implement
- `pom.xml`: Add `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `mariadb-java-client`, `spring-boot-starter-security`, `jjwt` (for JWT tokens). Remove Thymeleaf.
- `application.properties`: Connect to MariaDB.
- `CorsConfig`: Allow requests from the React frontend port (usually 3000 or 5173).

## How to execute
- Configure generic `@RestController` advice to handle exceptions globally.
- Ensure Spring Security is configured as stateless (SessionCreationPolicy.STATELESS).