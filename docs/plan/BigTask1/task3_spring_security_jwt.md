# Task 3: Spring Security & JWT Authentication (Stateless)

## Description
Secure the REST application using Spring Security and JSON Web Tokens (JWT). Since we are using a separate React frontend, the authentication must be completely stateless.

## What we should implement
- **`JwtUtils.java`**: A utility class to generate, validate, and extract information (like the username/role) from the JWT using the `jjwt` library.
- **`AuthTokenFilter.java`**: A filter that extends `OncePerRequestFilter`. It intercepts every HTTP request, checks for the `Authorization: Bearer <token>` header, validates the token, and sets the Spring `SecurityContext`.
- **`UserDetailsServiceImpl.java`**: Implements Spring's `UserDetailsService`. It loads the `AppUser` from the database (via `UserRepository`) for authentication.
- **`SecurityConfig.java`**: The main configuration class annotated with `@Configuration` and `@EnableWebSecurity`. It configures CORS, disables CSRF, sets session management to `STATELESS`, and defines URL authorizations.
- **`AuthController.java`**: An endpoint (`POST /api/auth/login`) that accepts a username and password, authenticates them using the `AuthenticationManager`, and returns a JSON response containing the JWT and the user's role.

## How to execute
- Define your security constraints in `SecurityConfig.java`:
  - `OPTIONS /**` -> Permit all (for CORS preflight requests).
  - `/api/auth/**` -> Permit all.
  - `/api/admin/**` -> Require authority `ADMIN_ROLE`.
  - `/api/annotator/**` -> Require authority `ANNOTATOR_ROLE`.
- Add `AuthTokenFilter` before the `UsernamePasswordAuthenticationFilter` in the security filter chain.