# BigTask 6 — Frontend Architecture (React + Vite + Tailwind CSS)

## Goal
Deliver React admin and annotator UIs with role-based routing, protected routes, Tailwind CSS styling, and API integration.

---

## Routing Structure (React Router v6)

```
/login                       → Login.jsx               (public)
/admin                       → AdminLayout              (requires ADMIN_ROLE)
  /admin/dashboard           → AdminDashboard.jsx
  /admin/datasets            → DatasetsList.jsx
  /admin/datasets/:id        → DatasetDetail.jsx
  /admin/datasets/upload     → DatasetUpload.jsx
  /admin/annotators          → AnnotatorManagement.jsx
  /admin/nlp                 → NlpDashboard.jsx
/annotator                   → AnnotatorLayout          (requires ANNOTATOR_ROLE)
  /annotator/tasks           → AnnotatorDashboard.jsx
  /annotator/tasks/:id       → AnnotationWorkspace.jsx
  /annotator/stats           → AnnotatorStats.jsx
*                            → NotFound.jsx             (404)
```

---

## Auth Flow

1. **Login:** User submits credentials → `POST /api/auth/login` → backend returns `{ token, role }`
2. **Store:** Save token + role to `localStorage` and React `AuthContext`
3. **ProtectedRoute component** checks:
   - Token exists? No → redirect `/login`
   - Role matches required role? No → redirect to their own dashboard (admin → `/admin/dashboard`, annotator → `/annotator/tasks`)
4. **Axios request interceptor** reads token from `localStorage` and attaches `Authorization: Bearer <token>` header to every request
5. **Axios response interceptor** on 401: clear `localStorage`, redirect to `/login`
6. **Logout:** Clear `localStorage`, update `AuthContext`, redirect to `/login`

---

## Component Tree

```
<App>
  <AuthProvider>
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/admin" element={<ProtectedRoute role="ADMIN"><AdminLayout /></ProtectedRoute>}>
          <Route index element={<Navigate to="dashboard" />} />
           <Route path="dashboard" element={<AdminDashboard />} />
          <Route path="datasets" element={<DatasetsList />} />
          <Route path="datasets/:id" element={<DatasetDetail />} />
          <Route path="datasets/upload" element={<DatasetUpload />} />
          <Route path="annotators" element={<AnnotatorManagement />} />
          <Route path="nlp" element={<NlpDashboard />} />
        </Route>
        <Route path="/annotator" element={<ProtectedRoute role="ANNOTATOR"><AnnotatorLayout /></ProtectedRoute>}>
          <Route index element={<Navigate to="tasks" />} />
          <Route path="tasks" element={<AnnotatorDashboard />} />
          <Route path="tasks/:taskId" element={<AnnotationWorkspace />} />
          <Route path="stats" element={<AnnotatorStats />} />
        </Route>
        <Route path="*" element={<NotFound />} />
      </Routes>
    </BrowserRouter>
  </AuthProvider>
</App>
```

---

## Component Details

### Shared Components
- **`ProtectedRoute.jsx`** — Reads `AuthContext`, checks token existence and role; redirects accordingly
- **`AdminLayout.jsx`** — Sidebar navigation (Dashboard, Datasets, Upload, Annotators, NLP) + header (user info, logout)
- **`AnnotatorLayout.jsx`** — Simple header with app name + logout button

### Page Components (Admin)
| Component | API Calls | Description |
|-----------|-----------|-------------|
| `AdminDashboard.jsx` | `GET /api/admin/dashboard/stats` | Summary cards, global class distribution, annotator progress table, spammer alerts |
| `DatasetsList.jsx` | `GET /api/admin/datasets?page=0&size=20` | Table with progress bars, click row → detail |
| `DatasetDetail.jsx` | `GET /api/admin/datasets/{id}`, `DELETE /.../annotators/{userId}`, `GET /api/admin/annotations?textPairId=X`, `PUT /api/admin/annotations/{id}` | Detail view: text pairs, assigned annotators, annotation correction |
| `DatasetUpload.jsx` | `POST /api/admin/datasets/upload` | File input + tags field + submit |
| `AnnotatorManagement.jsx` | `GET/POST/PUT/DELETE /api/admin/users` | Table + Add/Edit/Delete modals |
| `NlpDashboard.jsx` | `POST /api/admin/nlp/train\|test`, `GET /api/admin/nlp/logs` | Hyperparams form, Train/Test buttons, confusion matrix grid, logs table |

### Page Components (Annotator)
| Component | API Calls | Description |
|-----------|-----------|-------------|
| `AnnotatorDashboard.jsx` | `GET /api/annotator/tasks` | Table of assigned tasks with progress % + "Travailler" link |
| `AnnotationWorkspace.jsx` | `GET /api/annotator/tasks/:id/pairs?page=n`, `POST /api/annotator/tasks/:id/annotate` | Displays text(s), radio buttons for classes, Prev/Next/Save |
| `AnnotatorStats.jsx` | `GET /api/annotator/stats` | Cards: total annotated, avg time. Class distribution chart/badges |

---

## State Management

- **Auth state** → React Context (`AuthContext`) — token, role, login(), logout()
- **Per-page state** → local `useState` / `useReducer`
- **Data fetching** → custom `useApi` hook returning `{ data, loading, error }`
- **No Redux** — app scope does not warrant it

---

## CSS Strategy (Tailwind CSS)

- Utility-first: all styling via Tailwind classes, no custom CSS files
- Theme configured in `tailwind.config.js`:
  - Primary color: indigo
  - Success: green
  - Danger: red
  - Warning: amber
- Responsive: mobile-first using Tailwind breakpoints (`sm:`, `md:`, `lg:`)
- Dark mode: optional, can be added later via `class` strategy

---

## Environment Variables

| Variable | Default | Purpose |
|----------|---------|---------|
| `VITE_API_BASE_URL` | `http://localhost:8080` | Backend REST API URL |

Two `.env` files:
- `.env.development` — `VITE_API_BASE_URL=http://localhost:8080`
- `.env.production` — `VITE_API_BASE_URL=https://api.example.com`

---

## Verification

1. `npm run dev` starts without errors
2. Login page renders and authentication works
3. Protected routes redirect unauthenticated users to `/login`
4. Admin sidebar navigation works for all 5 pages (Dashboard, Datasets, Upload, Annotators, NLP)
5. Annotator can view tasks and navigate through the annotation workspace
6. `npm run build` produces a production bundle without errors
