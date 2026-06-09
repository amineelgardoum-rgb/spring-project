# NLP Annotation Platform

## Quick Start (for the professor)

### Prerequisites
- Java 21
- MariaDB 8.0 on `localhost:3306`, user `root`, empty password
- Python 3.x (only if using NLP training/test endpoints)

### Run
```bash
mvn clean package -DskipTests
java -jar target/app.jar
```

The app is pre-configured for zero-argument startup:
- Database `annotationbase` is created automatically
- Tables + FK constraints created fresh on each start
- Admin user **admin / admin** seeded automatically
- Annotators **user1 / user1**, **user2 / user2**, **user3 / user3** seeded
- Server starts on `http://localhost:8080`

### Important notes
- **NLP Python scripts**: The `python/` directory must be in the working directory (where you run `java -jar`). Alternatively, override with `--nlp.scripts.dir=/absolute/path`.
- **NLP concurrency**: Max 2 simultaneous NLP jobs by default. Configure with `--nlp.max-concurrent-jobs=N`.
- **JWT tokens** expire after 30 seconds (short for testing). Configure with `--app.jwt.expiration-ms=MILLIS`.

## API Endpoints

### Auth
- `POST /api/auth/login` — `{ "username": "admin", "password": "admin" }`
- `POST /api/auth/refresh` — `{ "refreshToken": "..." }`

### Admin
- `GET /api/admin/users` — list all users
- `POST /api/admin/users` — create annotator (auto-generates password)
- `PUT /api/admin/users/{id}` — update user
- `DELETE /api/admin/users/{id}` — soft-delete user
- `POST /api/admin/nlp/train` — start training `{ "hyperparameters": { ... } }`
- `POST /api/admin/nlp/test` — start evaluation
- `GET /api/admin/nlp/logs` — training history

### Jobs
- `GET /api/jobs/{id}` — poll async job status

## Profiles

| Profile | Database password | DDL mode | Use case |
|---------|------------------|----------|----------|
| default (none) | empty | `create` | Professor — fresh tables each run |
| `dev` | `Abdo2013@` | `update` | Local dev — preserves data |

Activate dev profile: `java -jar app.jar --spring.profiles.active=dev`

## Project Structure
```
├── python/              # NLP Python scripts
├── src/main/java/.../
│   ├── config/          # Security, JWT, CORS, Async
│   ├── controller/      # REST controllers
│   ├── domain/          # JPA entities
│   ├── dto/             # Request/Response DTOs
│   ├── exception/       # Global error handling
│   ├── mapper/          # MapStruct mappers
│   ├── repository/      # Spring Data repos
│   ├── seed/            # DataInitializer
│   └── service/         # Business logic
├── docs/plan/           # Project documentation
└── frontend/            # React frontend (Vite)
```
