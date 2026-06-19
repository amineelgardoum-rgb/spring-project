# Global Architecture — NLP Annotation Platform

## Overview

Full-stack web application for collaborative text annotation and supervised NLP model training with real-time metrics visualization.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 4.0.6 (Java 21) |
| Security | Spring Security + JWT (stateless) |
| Database | MariaDB/MySQL + JPA/Hibernate |
| DDL | Hibernate ddl-auto=create (default), update (dev) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Mapping | MapStruct (compile-time DTO mapping) |
| Caching | Caffeine (local) |
| Frontend | React 19 + Vite 8 + Tailwind CSS v4 |
| Charts | Chart.js + react-chartjs-2 |
| ML/NLP | Python scripts (train.py / test.py) via ProcessBuilder |
| Proxy | Vite dev server proxies /api → localhost:8080 |

---

## Backend Package Structure

```
com.ensah.nlp_annotation_platform/
├── NlpAnnotationPlatformApplication.java
├── config/
│   ├── AsyncConfig.java              # @EnableAsync + thread pool
│   ├── AuthTokenFilter.java          # OncePerRequestFilter JWT extraction
│   ├── CacheConfig.java              # Caffeine cache manager
│   ├── CorsConfig.java               # CORS allow localhost:3000,5173
│   ├── JwtUtils.java                 # JWT generate/validate/parse
│   ├── OpenApiConfig.java            # Swagger BearerAuth scheme
│   ├── SecurityConfig.java           # Spring Security filter chain
│   └── WebConfig.java                # Empty WebMvcConfigurer
├── domain/                           # JPA entities
│   ├── Annotation.java               # @Table: annotations; unique(text_item_id, annotator_id)
│   ├── Assignment.java               # @Table: assignments; unique(dataset_id, annotator_id)
│   ├── Dataset.java                  # @Table: datasets; labels element collection
│   ├── Job.java                      # @Table: jobs; type(TRAIN/TEST), status(PENDING/RUNNING/SUCCESS/FAILED)
│   ├── NlpTrainingLog.java           # @Table: nlp_training_logs; hyperparameters/metrics JSON
│   ├── RefreshToken.java             # @Table: refresh_tokens
│   ├── Role.java                     # Enum {ROLE_ADMIN, ROLE_ANNOTATOR}
│   ├── TextItem.java                 # @Table: text_items; content, pairContent, metadata(JSON)
│   ├── TrainingMetric.java           # @Table: training_metrics; per-epoch loss/accuracy
│   └── User.java                     # @Table: users; roles element collection, soft-delete
├── dto/
│   ├── request/
│   │   ├── EpochMetricRequest.java
│   │   ├── LoginRequest.java
│   │   ├── NlpTrainRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   ├── admin/
│   │   │   ├── CreateUserRequest.java
│   │   │   ├── UpdateAnnotationRequest.java
│   │   │   └── UpdateUserRequest.java
│   │   ├── annotator/
│   │   │   └── AnnotateRequest.java
│   │   └── dataset/
│   │       └── DatasetAssignmentRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── JobResponse.java
│       ├── NlpLogResponse.java
│       ├── UserResponse.java
│       ├── admin/
│       │   ├── AnnotationAdminResponse.java
│       │   ├── AnnotatorProgressEntry.java
│       │   ├── CreatedUserResponse.java
│       │   ├── DashboardStatsResponse.java
│       │   ├── SpammerInfo.java
│       │   └── UserAdminResponse.java
│       ├── annotator/
│       │   ├── AnnotatorStatsResponse.java
│       │   ├── AnnotatorTaskResponse.java
│       │   └── TextPairResponse.java
│       ├── dataset/
│       │   ├── DatasetDetailResponse.java
│       │   └── DatasetResponse.java
│       └── metrics/
│           └── MetricsResponse.java
├── exception/
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── SpammerDetectedException.java
│   ├── UnauthorizedException.java
│   └── ValidationException.java
├── mapper/
│   ├── UserAdminMapper.java          # User → UserAdminResponse
│   └── UserMapper.java               # User → UserResponse
├── repository/
│   ├── AnnotationRepository.java
│   ├── AssignmentRepository.java
│   ├── DatasetRepository.java
│   ├── JobRepository.java
│   ├── NlpTrainingLogRepository.java
│   ├── RefreshTokenRepository.java
│   ├── TextItemRepository.java
│   ├── TrainingMetricRepository.java
│   └── UserRepository.java
├── seed/
│   └── DataInitializer.java          # Seeds admin + 3 annotators
├── service/
│   ├── admin/
│   │   ├── AdminAnnotationService.java
│   │   ├── AdminDashboardService.java
│   │   ├── AdminUserService.java
│   │   └── impl/
│   │       ├── AdminAnnotationServiceImpl.java
│   │       ├── AdminDashboardServiceImpl.java
│   │       └── AdminUserServiceImpl.java
│   ├── annotator/
│   │   ├── AnnotatorService.java
│   │   └── impl/
│   │       └── AnnotatorServiceImpl.java
│   ├── dataset/
│   │   ├── DatasetService.java
│   │   └── impl/
│   │       └── DatasetServiceImpl.java
│   ├── job/
│   │   ├── JobService.java
│   │   └── impl/
│   │       └── JobServiceImpl.java
│   ├── metrics/
│   │   ├── MetricsService.java
│   │   └── impl/
│   │       └── MetricsServiceImpl.java
│   ├── nlp/
│   │   ├── MetricsSseService.java    # SSE emitter broadcast
│   │   ├── NlpAsyncExecutor.java     # @Async Python subprocess
│   │   ├── NlpService.java
│   │   └── impl/
│   │       └── NlpServiceImpl.java
│   └── user/
│       ├── UserService.java
│       └── impl/
│           ├── UserDetailsServiceImpl.java
│           └── UserServiceImpl.java
└── controller/
    ├── AdminAnnotationController.java
    ├── AdminDashboardController.java
    ├── AdminUserController.java
    ├── AnnotatorController.java
    ├── AuthController.java
    ├── DatasetController.java
    ├── JobController.java
    ├── MetricsExportController.java
    └── NlpController.java
```

---

## Frontend Structure (React + Vite + Tailwind CSS)

```
frontend/
├── .env.development               # VITE_API_BASE_URL=http://localhost:8080
├── vite.config.js                 # @tailwindcss/vite plugin + /api proxy
├── src/
│   ├── main.jsx                   # Entry point
│   ├── App.jsx                    # BrowserRouter with routes
│   ├── index.css                  # Tailwind @import "tailwindcss" + green theme tokens
│   ├── api/
│   │   ├── axios.js               # Axios instance + JWT interceptor
│   │   ├── adminApi.js            # All /api/admin/* calls + downloadModel
│   │   ├── annotatorApi.js        # /api/annotator/* calls
│   │   └── authApi.js             # POST /api/auth/login + refresh
│   ├── assets/
│   │   └── react.svg
│   ├── components/
│   │   ├── AdminLayout.jsx        # Sidebar for admin routes
│   │   ├── AnnotatorLayout.jsx    # Header for annotator routes
│   │   ├── DarkModeToggle.jsx     # Dark/light theme toggle
│   │   ├── ErrorBoundary.jsx      # React error boundary
│   │   ├── Modal.jsx              # Reusable modal with backdrop
│   │   ├── ProtectedRoute.jsx     # Role-based route guard
│   │   ├── Spinner.jsx            # Loading spinner
│   │   └── Toast.jsx              # Toast notification system
│   ├── context/
│   │   └── AuthContext.jsx        # Auth state + login/logout/refresh
│   ├── hooks/
│   │   ├── useApi.js              # Generic API hook (loading/error/data)
│   │   ├── useAuth.js             # Shortcut to AuthContext
│   │   └── useDarkMode.js         # Dark mode + localStorage
│   ├── pages/
│   │   ├── Login.jsx              # Green glass-card login page
│   │   ├── NotFound.jsx           # 404 page
│   │   ├── admin/
│   │   │   ├── AdminDashboard.jsx       # Stats cards, annotator progress, spammer alerts, class distribution chart
│   │   │   ├── AnnotatorManagement.jsx  # CRUD annotator table with modal
│   │   │   ├── DatasetDetail.jsx        # View text pairs, assign/remove annotators, inline annotation edit
│   │   │   ├── DatasetUpload.jsx        # Upload CSV/JSON form
│   │   │   ├── DatasetsList.jsx         # All datasets with progress bars
│   │   │   ├── NlpDashboard.jsx         # Train/test controls, real-time charts, history table, model download
│   │   │   └── OptionsAvancees.jsx      # Dataset metrics (Fleiss' kappa) + export
│   │   └── annotator/
│   │       ├── AnnotatorDashboard.jsx   # Assigned tasks with completion %
│   │       ├── AnnotationWorkspace.jsx  # One-pair-at-a-time annotation UI
│   │       └── AnnotatorStats.jsx       # Personal stats + class distribution chart
│   └── utils/
│       └── formatters.js           # formatPercent, formatDecimal, formatTime
```

---

## Data Flow

```
Browser (React :5173)
    │
    │ HTTP (JSON) + JWT Bearer token
    ▼
Vite Dev Server (proxy /api → :8080)
    │
    ▼
Spring Boot REST API (:8080)
    │
    ├──▶ JPA / Hibernate ──▶ MariaDB/MySQL (:3306)
    │
    ├──▶ ProcessBuilder ──▶ Python train.py / test.py
    │                        │
    │                        ▼
    │                    POST /api/admin/nlp/metrics  (per-epoch SSE broadcast)
    │
    ├──▶ SSE /api/admin/nlp/metrics/stream ──▶ Browser EventSource
    │
    └──▶ Model download via /api/admin/nlp/models/{logId}/download
```

---

## Route Map

| Path | Method | Auth | Description |
|------|--------|------|-------------|
| `/api/auth/login` | POST | Public | Returns JWT + refresh token |
| `/api/auth/refresh` | POST | Public | Refresh JWT |
| `/api/jobs/{id}` | GET | Authenticated | Poll job status |
| `/api/admin/dashboard/stats` | GET | ADMIN | Global stats + spammer detection |
| `/api/admin/users` | GET | ADMIN | List annotators (paginated) |
| `/api/admin/users` | POST | ADMIN | Create annotator |
| `/api/admin/users/{id}` | PUT | ADMIN | Update annotator |
| `/api/admin/users/{id}` | DELETE | ADMIN | Soft-delete annotator |
| `/api/admin/datasets` | GET | ADMIN | List datasets with progress |
| `/api/admin/datasets/{id}` | GET | ADMIN | Dataset detail (items + annotators) |
| `/api/admin/datasets/upload` | POST | ADMIN | Upload CSV/JSON dataset |
| `/api/admin/datasets/{id}/assign` | POST | ADMIN | Assign annotators |
| `/api/admin/datasets/{id}/annotators/{userId}` | DELETE | ADMIN | Remove annotator |
| `/api/admin/datasets/{id}/metrics` | GET | ADMIN | Fleiss' kappa + distributions |
| `/api/admin/datasets/{id}/export?format=csv|json` | GET | ADMIN | Export annotations |
| `/api/admin/annotations?textItemId=` | GET | ADMIN | List annotations for item |
| `/api/admin/annotations/{id}` | PUT | ADMIN | Correct annotation |
| `/api/admin/nlp/train` | POST | ADMIN | Trigger training (async) |
| `/api/admin/nlp/test` | POST | ADMIN | Trigger testing (async) |
| `/api/admin/nlp/logs` | GET | ADMIN | Training history |
| `/api/admin/nlp/metrics` | POST | Public* | Receive epoch metric from Python |
| `/api/admin/nlp/metrics/stream` | GET | Public* | SSE real-time metrics stream |
| `/api/admin/nlp/metrics/{jobId}` | GET | ADMIN | Per-job metrics |
| `/api/admin/nlp/models/{logId}/download` | GET | ADMIN | Download trained model |
| `/api/annotator/tasks` | GET | ANNOTATOR | My assigned tasks |
| `/api/annotator/tasks/{id}/pairs` | GET | ANNOTATOR | Text pairs to annotate |
| `/api/annotator/tasks/{id}/annotate` | POST | ANNOTATOR | Save annotation |
| `/api/annotator/stats` | GET | ANNOTATOR | Personal stats |

*SSE metrics endpoint is public because Python runs locally without JWT.

---

## Architectural Decisions

| Decision | Rationale |
|----------|-----------|
| Stateless JWT (no HttpSession) | SPA frontend, scales horizontally |
| DTOs vs exposing entities | Prevents circular JSON, lazy-loading issues, over-fetching |
| MapStruct | Compile-time mapping, no reflection overhead |
| Hibernate ddl-auto=update (dev) | Auto-creates tables from entities for dev speed |
| Service layer | Isolates business logic from controllers, testable |
| @Async job processing | Long NLP tasks cannot block HTTP threads |
| Optimistic locking (`@Version`) | Prevents lost updates on concurrent annotation |
| Tailwind CSS v4 (`@tailwindcss/vite`) | No config file needed, JIT compiled |
| SSE for training metrics | Push real-time epoch data without polling |
| `java.io.tmpdir` for scripts/models | Avoids filesystem permission issues across environments |
| No manual `Content-Type: multipart/form-data` | Browser sets boundary automatically; explicit header breaks upload |

---

## Database Tables

| Table | Key Columns | Notes |
|-------|------------|-------|
| `users` | id, username, password_hash, roles, enabled, deleted | Soft-delete via `deleted` flag |
| `datasets` | id, name, description, file_path, num_records, labels, created_by_id | `labels` stored as element collection |
| `text_items` | id, content, pair_content, metadata(JSON), dataset_id, version | Optimistic locking |
| `annotations` | id, label, comment, duration, text_item_id, annotator_id, version | Unique(text_item_id, annotator_id) |
| `assignments` | id, dataset_id, annotator_id, assigned_at | Unique(dataset_id, annotator_id) |
| `jobs` | id, type(TRAIN/TEST), status, progress, hyperparameters(JSON), result(JSON) | Polling-based progress tracking |
| `nlp_training_logs` | id, hyperparameters(JSON), metrics(JSON), status, execution_logs | Full training history |
| `training_metrics` | id, job_id, epoch, loss, accuracy, eval_loss, eval_accuracy | Per-epoch metrics |
| `refresh_tokens` | id, token(UUID), expiry, user_id | Token rotation support |

---

## Security

- **CSRF**: disabled (stateless JWT)
- **Session**: STATELESS
- **Public endpoints**: `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/api/admin/nlp/metrics/**`
- **Admin**: `/api/admin/**` requires `ROLE_ADMIN`
- **Annotator**: `/api/annotator/**` requires `ROLE_ANNOTATOR`
- **Filter**: `AuthTokenFilter` (JWT) before `UsernamePasswordAuthenticationFilter`
- **CORS**: Allows `localhost:3000` and `localhost:5173`
