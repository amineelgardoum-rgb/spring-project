# NLP Annotation Platform

Plateforme intelligente d'annotation collaborative et d'apprentissage supervisé en NLP.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 4.0.6 (Java 21) |
| Security | Spring Security + JWT |
| Database | MariaDB/MySQL + JPA/Hibernate |
| Frontend | React 19 + Vite 8 + Tailwind CSS v4 |
| Charts | Chart.js + react-chartjs-2 |
| ML/NLP | Python scripts (train.py / test.py) |

## Quick Start

### Prerequisites
- Java 21
- MariaDB 8.0+ on `localhost:3306`, user `root`
- Node.js 20+
- Python 3.x (only for NLP train/test)

### Running the Application

There are two ways to run the full-stack application (Backend + Frontend):

#### 1. Development Mode (Recommended - No JAR required)
You can compile and run the backend and frontend dynamically with a single command without packaging the JAR:

```bash
chmod +x mvnw
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
* **Backend** starts on `http://localhost:8080` (uses the `dev` profile database).
* **Frontend** starts automatically in the background on `http://localhost:5173` via Vite (no need to open a separate terminal to run `npm run dev`).

#### 2. Standalone Mode (Using the JAR)
If you want to package the application to run as a single `.jar` file:

1. **Build the project** (this will automatically copy the built `app.jar` to the project root):
   ```bash
   chmod +x mvnw
   ./mvnw clean package -DskipTests
   ```

2. **Run the JAR** from the project root:
   * **Linux/macOS:**
     ```bash
     SPRING_PROFILES_ACTIVE=dev java -jar app.jar
     ```
   * **Windows (CMD):**
     ```bash
     set SPRING_PROFILES_ACTIVE=dev
     java -jar app.jar
     ```

### Seeded Users
- **admin / admin** (ADMIN)
- **user1 / user1**, **user2 / user2**, **user3 / user3** (ANNOTATOR)

## API Endpoints

### Auth
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/login` | Login → `{ "username", "password" }` → JWT |
| POST | `/api/auth/refresh` | Refresh JWT |

### Admin — Dashboard
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/admin/dashboard/stats` | Global stats + spammer detection |

### Admin — Users
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/admin/users` | List annotators (paginated) |
| POST | `/api/admin/users` | Create annotator |
| PUT | `/api/admin/users/{id}` | Update user |
| DELETE | `/api/admin/users/{id}` | Soft-delete user |

### Admin — Datasets
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/admin/datasets` | List all datasets |
| GET | `/api/admin/datasets/{id}` | Dataset detail with text items |
| POST | `/api/admin/datasets/upload` | Upload CSV/JSON (multipart) |
| POST | `/api/admin/datasets/{id}/assign` | Assign annotators |
| DELETE | `/api/admin/datasets/{id}/annotators/{userId}` | Remove annotator |

### Admin — Annotations
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/admin/annotations?textItemId=` | List annotations for an item |
| PUT | `/api/admin/annotations/{id}` | Correct annotation |

### Admin — NLP (Train / Test)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/admin/nlp/train` | Start training (async) |
| POST | `/api/admin/nlp/test` | Start testing (async) |
| GET | `/api/admin/nlp/logs` | Training history (accuracy, F1, loss, model path) |
| GET | `/api/admin/nlp/metrics/{jobId}` | Per-job epoch metrics |
| GET | `/api/admin/nlp/metrics/stream` | SSE real-time metrics stream |
| GET | `/api/admin/nlp/models/{logId}/download` | Download trained model |

### Admin — Metrics & Export
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/admin/datasets/{id}/metrics` | Fleiss' kappa + distributions |
| GET | `/api/admin/datasets/{id}/export?format=csv|json` | Export annotated data |

### Annotator
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/annotator/tasks` | My assigned tasks |
| GET | `/api/annotator/tasks/{id}/pairs` | Text pairs to annotate |
| POST | `/api/annotator/tasks/{id}/annotate` | Submit annotation |
| GET | `/api/annotator/stats` | Personal stats |

## Project Structure

```
├── src/main/java/com/ensah/nlp_annotation_platform/
│   ├── config/              # Security, JWT, CORS, Async, Cache, Swagger
│   ├── controller/          # 9 REST controllers
│   ├── domain/              # 9 JPA entities
│   ├── dto/                 # Request & Response DTOs
│   ├── exception/           # Global error handling
│   ├── mapper/              # MapStruct mappers
│   ├── repository/          # Spring Data repositories
│   ├── seed/                # DataInitializer (admin + 3 annotators)
│   └── service/             # Business logic (admin, annotator, dataset, nlp, metrics, job, user)
├── src/main/resources/
│   ├── application.properties        # Default config (DB root/no password, ddl=create)
│   ├── application-dev.properties    # Dev config (password, ddl=update, DEBUG logs)
│   └── application-prod.properties   # Production config (env vars)
├── frontend/
│   ├── src/
│   │   ├── api/             # Axios client (adminApi, annotatorApi, authApi)
│   │   ├── components/      # AdminLayout, AnnotatorLayout, Modal, Toast, Spinner, etc.
│   │   ├── context/         # AuthContext (JWT, role, login/logout)
│   │   ├── hooks/           # useApi, useAuth, useDarkMode
│   │   ├── pages/
│   │   │   ├── admin/       # 7 pages (Dashboard, Datasets, NLP, Users, etc.)
│   │   │   └── annotator/   # 3 pages (Tasks, Workspace, Stats)
│   │   └── utils/           # formatters
│   └── vite.config.js       # @tailwindcss/vite + /api proxy
├── python/                  # NLP Python scripts (train.py, test.py)
├── docs/plan/               # Architecture documentation
└── pom.xml
```

## Key Features

- **Role-based access** — Admin (user/dataset/annotation management) + Annotator (task list, annotation workspace, personal stats)
- **Dataset import** — CSV/JSON upload with auto-assignment to multiple annotators
- **Annotation UI** — One text-pair-at-a-time interface with label selection + duration tracking
- **Quality metrics** — Fleiss' kappa for inter-annotator agreement, spammer detection (≥95% same label)
- **NLP training** — Async Python subprocess with real-time SSE epoch metrics (loss/accuracy charts)
- **Model management** — Download trained `.pt` models from training history
- **Green theme** — Custom CSS variables for primary color, radial gradient login page with glass-card effect

## Profiles

| Profile | DB Password | DDL Mode | Use Case |
|---------|------------|----------|----------|
| default | empty | `create` | Fresh tables each run |
| dev | `***` | `update` | Preserves data across restarts |
| prod | env vars | `update` | Production deployment |
