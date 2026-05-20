# Global Architecture — NLP Annotation Platform

## Overview

Full-stack web application for collaborative text annotation and supervised NLP model training.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 4.0.6 (Java 21) |
| Security | Spring Security + JWT (stateless) |
| Database | MariaDB + JPA/Hibernate |
| Migrations | Hibernate ddl-auto=update (dev) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Mapping | MapStruct (compile-time DTO mapping) |
| Caching | Caffeine (local) |
| Frontend | React + Vite + Tailwind CSS |
| ML/NLP | Python scripts (train.py / test.py) |
| CI | GitHub Actions |

---

## Backend Package Structure

```
com.ensah.nlp_annotation_platform/
├── NlpAnnotationPlatformApplication.java
├── config/
│   ├── SecurityConfig.java        # Spring Security, CORS, CSRF, URL rules
│   ├── CorsConfig.java            # CORS permissive for React origin
│   ├── AsyncConfig.java           # @EnableAsync + thread pool
│   └── WebConfig.java             # General web MVC config
├── domain/                        # JPA entities
│   ├── User.java                  # @Table(name="users"): id, firstName, lastName, username, passwordHash, roles, enabled, deleted, createdAt, updatedAt
│   ├── Role.java
│   ├── Dataset.java
│   ├── TextItem.java
│   ├── Annotation.java
│   ├── RefreshToken.java
│   └── NlpTrainingLog.java        # id, userId, hyperparameters, metrics, status, executionLogs, startedAt, completedAt
├── dto/                           # Request/Response DTOs
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── CreateUserRequest.java
│   │   ├── UpdateUserRequest.java
│   │   ├── AnnotationRequest.java
│   │   ├── AnnotationCorrectionRequest.java
│   │   └── NlpTrainRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── UserResponse.java
│       ├── DatasetResponse.java
│       ├── DatasetDetailResponse.java
│       ├── MetricsResponse.java
│       ├── AnnotatorStatsResponse.java
│       └── ErrorResponse.java
├── mapper/                        # MapStruct interfaces
│   ├── UserMapper.java
│   ├── DatasetMapper.java
│   └── AnnotationMapper.java
├── repository/                    # Spring Data JPA
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   ├── DatasetRepository.java
│   ├── TextItemRepository.java
│   └── AnnotationRepository.java
├── service/                       # Business logic (@Service)
│   ├── AuthService.java
│   ├── UserService.java
│   ├── DatasetService.java
│   ├── AssignmentService.java
│   ├── AnnotationService.java
│   ├── MetricsService.java
│   ├── ExportService.java
│   └── NlpExecutionService.java
├── controller/                    # REST controllers
│   ├── AuthController.java
│   ├── admin/
│   │   ├── AdminUserController.java
│   │   ├── AdminDatasetController.java
│   │   ├── AdminAnnotationController.java
│   │   └── AdminNlpController.java
│   └── annotator/
│       └── AnnotatorController.java
├── exception/                     # Custom exceptions + handler
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedException.java
│   └── SpammerDetectedException.java
├── batch/                         # Async job processing
│   ├── Job.java                   # Job entity (status, progress, logs)
│   ├── JobRepository.java
│   └── JobService.java
└── seed/                          # Bootstrap data
    └── DataInitializer.java       # Creates admin user on first run
```

---

## Frontend Structure (React + Vite + Tailwind CSS)

```
frontend/
├── .env.development               # VITE_API_BASE_URL=http://localhost:8080
├── .env.production                # VITE_API_BASE_URL=https://api.example.com
├── vite.config.js                 # Tailwind plugin + proxy
├── tailwind.config.js             # Theme colors, plugins
├── src/
│   ├── main.jsx                   # Entry point
│   ├── App.jsx                    # Router setup
│   ├── index.css                  # Tailwind directives @import "tailwindcss"
│   ├── api/
│   │   ├── axios.js               # Base Axios instance + interceptors
│   │   ├── authApi.js             # POST /api/auth/login
│   │   ├── adminApi.js            # Users, datasets, metrics, NLP
│   │   └── annotatorApi.js        # Tasks, annotations
│   ├── context/
│   │   └── AuthContext.jsx        # JWT storage, role, login/logout
│   ├── hooks/
│   │   ├── useAuth.js             # Access AuthContext
│   │   └── useApi.js              # Generic data fetching (loading/error/data)
│   ├── components/
│   │   ├── ProtectedRoute.jsx     # Role-based route guard
│   │   ├── AdminLayout.jsx        # Sidebar + header for admin
│   │   └── AnnotatorLayout.jsx    # Simple header for annotator
│   └── pages/
│       ├── Login.jsx
│       ├── NotFound.jsx
│       ├── admin/
│       │   ├── AdminDashboard.jsx
│       │   ├── DatasetsList.jsx
│       │   ├── DatasetDetail.jsx
│       │   ├── DatasetUpload.jsx
│       │   ├── AnnotatorManagement.jsx
│       │   └── NlpDashboard.jsx
│       └── annotator/
│           ├── AnnotatorDashboard.jsx
│           ├── AnnotationWorkspace.jsx
│           └── AnnotatorStats.jsx
```

---

## Data Flow

```
Browser (React :5173)
    │
    │ HTTP (JSON) + JWT Bearer token
    ▼
Spring Boot REST API (:8080)
    │
    ├──▶ JPA / Hibernate ──▶ MariaDB (:3306)
    │
    ├──▶ ProcessBuilder ──▶ Python train.py / test.py
    │
    └──▶ Async Job Queue ──▶ Job table (status, progress, logs)
```

---

## Route Map

| Path | Method | Auth | Description |
|------|--------|------|-------------|
| `/api/auth/login` | POST | Public | Returns JWT + role |
| `/api/auth/refresh` | POST | Public (with valid refresh token) | Returns new JWT |
| `/api/jobs/{id}` | GET | ADMIN | Poll job status (CSV import, NLP training) |
| `/api/admin/dashboard/stats` | GET | ADMIN | Global dashboard stats + class distribution + spammer alerts |
| `/api/admin/users` | GET | ADMIN | List annotators (paginated) |
| `/api/admin/users` | POST | ADMIN | Create annotator |
| `/api/admin/users/{id}` | DELETE | ADMIN | Soft-delete annotator |
| `/api/admin/users/{id}` | PUT | ADMIN | Update annotator |
| `/api/admin/datasets` | GET | ADMIN | List datasets with progress |
| `/api/admin/datasets/{id}` | GET | ADMIN | Dataset detail (pairs + annotators) |
| `/api/admin/datasets/upload` | POST | ADMIN | Upload CSV/JSON dataset |
| `/api/admin/datasets/{id}/assign` | POST | ADMIN | Assign annotators to dataset |
| `/api/admin/datasets/{id}/annotators/{userId}` | DELETE | ADMIN | De-assign annotator |
| `/api/admin/datasets/{id}/metrics` | GET | ADMIN | Kappa scores + spammers |
| `/api/admin/datasets/{id}/export` | GET | ADMIN | Export CSV or JSON (?format=) |
| `/api/admin/annotations` | GET | ADMIN | List annotations (filter by textPairId) |
| `/api/admin/annotations/{id}` | PUT | ADMIN | Correct an annotation |
| `/api/admin/nlp/train` | POST | ADMIN | Trigger NLP training (accepts hyperparams) |
| `/api/admin/nlp/test` | POST | ADMIN | Trigger NLP testing (returns confusion matrix) |
| `/api/admin/nlp/logs` | GET | ADMIN | Training history |
| `/api/annotator/tasks` | GET | ANNOTATOR | My assigned tasks |
| `/api/annotator/tasks/{id}/pairs` | GET | ANNOTATOR | Text pair to annotate |
| `/api/annotator/tasks/{id}/annotate` | POST | ANNOTATOR | Save annotation |
| `/api/annotator/stats` | GET | ANNOTATOR | Personal annotation stats |

---

## Architectural Decisions

| Decision | Rationale |
|----------|-----------|
| Stateless JWT (no HttpSession) | SPA frontend, scales horizontally |
| DTOs vs exposing entities | Prevents circular JSON, lazy-loading issues, over-fetching |
| MapStruct | Compile-time mapping, no reflection overhead |
| Hibernate ddl-auto=update | Auto-creates tables from entities for dev speed |
| Service layer | Isolates business logic from controllers, testable |
| Async job processing | Long NLP tasks cannot block HTTP threads |
| Optimistic locking (`@Version`) | Prevents lost updates on concurrent annotation |
| Tailwind CSS | Utility-first, no context-switching, small bundle |
