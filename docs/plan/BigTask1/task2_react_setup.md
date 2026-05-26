# Task 2: React Frontend Setup with Tailwind CSS

## Description
Initialize the React frontend project with Vite, install dependencies, configure Tailwind CSS, set up environment variables, and create the API layer.

## Steps

### 1. Create project
```
npm create vite@latest frontend -- --template react
cd frontend
```

### 2. Install dependencies
```
npm install axios react-router-dom jwt-decode
npm install -D tailwindcss @tailwindcss/vite
```

### 3. Configure Vite + Tailwind
In `vite.config.js`, add the Tailwind plugin:
```js
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
})
```

In `src/index.css`, add the Tailwind import:
```css
@import "tailwindcss";
```

### 4. Environment configuration
Create `.env.development`:
```
VITE_API_BASE_URL=http://localhost:8080
```

Create `.env.production`:
```
VITE_API_BASE_URL=https://api.example.com
```

### 5. Axios instance with interceptors
Create `src/api/axios.js`:
- Base URL from `import.meta.env.VITE_API_BASE_URL`
- Request interceptor: read JWT from `localStorage`, attach `Authorization: Bearer <token>`
- Response interceptor:
  - On 401: try `POST /api/auth/refresh` once with the stored refresh token
  - If refresh success → update `localStorage` with new token pair, retry the original request
  - If refresh fail → clear `localStorage`, redirect to `/login`
  - Use a mutex/flag so concurrent 401s only trigger one refresh batch

### 6. API modules
Create typed API wrappers:
- `src/api/authApi.js` — `login(username, password)` → `POST /api/auth/login`
- `src/api/adminApi.js` — users CRUD, datasets, metrics, NLP
- `src/api/annotatorApi.js` — tasks, pairs, annotations

### 7. Folder structure to create
```
frontend/
├── .env.development
├── .env.production
├── vite.config.js
├── src/
│   ├── index.css
│   ├── main.jsx
│   ├── App.jsx
│   ├── api/
│   │   ├── axios.js
│   │   ├── authApi.js
│   │   ├── adminApi.js
│   │   └── annotatorApi.js
│   ├── context/
│   │   └── AuthContext.jsx
│   ├── hooks/
│   │   ├── useAuth.js
│   │   └── useApi.js
│   ├── components/
│   │   ├── ProtectedRoute.jsx
│   │   ├── AdminLayout.jsx
│   │   └── AnnotatorLayout.jsx
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

### 8. Global error handling from backend
All pages should use `error.response?.data?.error` from the `GlobalExceptionHandler` for user-facing error messages instead of hardcoded strings:
- `Login.jsx`: Parse `err.response.data.error` (e.g. `"invalid_credentials"`) to show the real backend message
- Future admin/annotator pages: pass server error messages to Tailwind alert components

---

## How to verify
- [ ] 1. `npm run dev` starts without errors
- [ ] 2. Tailwind CSS classes render correctly in browser
- [ ] 3. Axios interceptor attaches JWT token to requests
- [ ] 4. `.env.development` and `.env.production` are loaded correctly
- [ ] 5. Backend GlobalExceptionHandler error messages appear in frontend alerts (not hardcoded)

See `architecture.md` in this folder for architectural decisions.
