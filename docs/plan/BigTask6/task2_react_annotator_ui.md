# Task 2: React Annotator Components

## Description
Implement the frontend screens for the Annotator role using React + Tailwind CSS.

## Prerequisites
- React project initialized (see BigTask1/task2_react_setup.md)
- Axios instance with JWT interceptor configured
- AuthContext and ProtectedRoute in place
- AnnotatorLayout component with header

## Route setup
All annotator pages are rendered inside `<AnnotatorLayout>` which provides:
- Top header bar with app name and logout button
- Clean, focused UI for annotation work

```
/annotator               → AnnotatorDashboard.jsx   (index route)
/annotator/tasks/:taskId → AnnotationWorkspace.jsx
/annotator/stats         → AnnotatorStats.jsx
```

## Components

### AnnotatorDashboard.jsx
- Calls `GET /api/annotator/tasks` on mount
- Displays a table with columns (matching forms.md UC6):
  - Id
  - Nom dataset
  - Date limite (deadline)
  - % Avancement (personal progress bar, e.g. "45/100")
  - Taille (total assigned pairs)
  - Actions: `Travailler` button → navigates to `/annotator/tasks/{id}`
- At bottom of page: "Mes Statistiques" button/link → navigates to `/annotator/stats`
- Empty state: "Aucune tâche assignée" message
- Loading state: skeleton rows using Tailwind `animate-pulse`

### AnnotationWorkspace.jsx
- URL param: `taskId` from route, `page` from query string (default 0)
- Fetches current pair: `GET /api/annotator/tasks/{taskId}/pairs?page={currentPage}`
- Displays:
  - **Header info**: "Id couple: X" and a description link/tooltip
  - **Progress indicator**: "Question 5 / 100"
  - **Texte 1**: Read-only text card/box (always present)
  - **Texte 2**: Read-only text card/box (only if pair-based task, i.e. `pairContent` not null)
  - **Radio button group** for available classes/tags (fetched from API response) — rendered dynamically based on response labels
- Navigation (matching forms.md UC7):
  - `<< Précédent` (Previous) — goes to previous page, disabled on first page
  - `Valider` — **saves the current annotation and loads the next pair**. Calls `POST /api/annotator/tasks/{taskId}/annotate` with `{ textItemId, label }`. On success → auto-advance to next page + show success toast
  - `Suivant >>` — skips to next pair **without saving**. Clears any selected class
  - Disable Suivant/Valider on last page
- Save behavior:
  - `Valider` button calls `POST /api/annotator/tasks/{taskId}/annotate` with `{ textItemId: currentPair.id, label: selectedClass }`
  - Show success toast on save, error alert on failure
  - If no class selected when Valider is clicked, show validation message "Veuillez sélectionner une classe"
- Edge cases:
  - If task is completed (all pairs annotated), show a completion message with "Retour aux tâches" link
  - If API returns an error, show error alert with retry option
  - If annotator already annotated this pair (re-opening workspace), pre-select the saved class

## State Management (per component)
```jsx
// AnnotationWorkspace state shape
const [currentPage, setCurrentPage] = useState(0);    // synced with URL ?page=
const [selectedClass, setSelectedClass] = useState(null);
const [currentPair, setCurrentPair] = useState(null);  // current TextPairResponse
const [totalPages, setTotalPages] = useState(0);
const [saving, setSaving] = useState(false);
const { loading, error, execute: fetchPair } = useApi(...);
```

## Styling
- All styling via Tailwind utility classes
- Clean, minimal design focused on the annotation task
- Large readable text for the content being annotated
- Radio buttons styled with Tailwind (larger touch targets)
- Responsive: works on tablet and desktop
- Progress bar using Tailwind `bg-primary` + `w-[xx%]`

## Data Fetching Pattern
```jsx
const fetchPair = async (page) => {
  const res = await annotatorApi.getPair(taskId, page);
  setPairs(prev => ({ ...prev, [page]: res.data }));
  setTotalPages(res.data.totalPages);
};
```

### AnnotatorStats.jsx
- Route: `/annotator/stats`
- Calls `GET /api/annotator/stats` on mount
- Expected response shape:
```json
{
  "totalAnnotated": 45,
  "avgTimePerAnnotation": 12.5,
  "classDistribution": { "positif": 20, "negatif": 15, "neutre": 10 }
}
```
- Displays:
  - **Stat cards** (Tailwind grid of cards):
    - Total annotated texts (large number)
    - Average annotation time (seconds)
  - **Class distribution** section:
    - List of class names with colored Tailwind badges showing counts
    - Or a simple horizontal bar chart using Tailwind width utilities (e.g., `<div class="bg-indigo-500" style="width: 60%"></div>`)
- "Retour aux tâches" link to go back to `/annotator/tasks`

## How to verify
1. Annotator logs in and sees their assigned tasks
2. Click "Travailler" opens the annotation workspace
3. Navigate through pairs with Previous/Next
4. Select a class and save successfully
5. Progress bar updates correctly
6. Completed task shows completion message
7. Click "Mes Statistiques" → see stats cards and class distribution
8. All API calls include JWT Bearer token (check Network tab)

See `architecture.md` in this folder for architectural decisions.
