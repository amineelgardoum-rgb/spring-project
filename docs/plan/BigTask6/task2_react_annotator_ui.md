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
/annotator/tasks         → AnnotatorDashboard.jsx
/annotator/tasks/:taskId → AnnotationWorkspace.jsx
/annotator/stats         → AnnotatorStats.jsx
```

## Components

### AnnotatorDashboard.jsx
- Calls `GET /api/annotator/tasks` on mount
- Displays a table with columns:
  - Dataset Name
  - Progress (e.g., "45/100" + progress bar)
  - Deadline (if applicable)
  - Action: "Travailler" button → navigates to `/annotator/tasks/{id}`
- At bottom of page: "Mes Statistiques" button/link → navigates to `/annotator/stats`
- Empty state: "Aucune tâche assignée" message
- Loading state: skeleton rows using Tailwind `animate-pulse`

### AnnotationWorkspace.jsx
- Fetches current pair: `GET /api/annotator/tasks/{taskId}/pairs?page={currentPage}`
- Displays:
  - Progress indicator: "Question 5 / 100"
  - Text content (Text1, and Text2 if pair-based task)
  - Radio button group for available classes/tags (fetched from API response)
- Navigation:
  - "Précédent" (Previous) and "Suivant" (Next) buttons at bottom
  - Current page index tracked in React state (`useState`)
  - Disable Previous on first page, Next on last page
- Save behavior:
  - "Sauvegarder" button calls `POST /api/annotator/tasks/{taskId}/annotate` with `{ textPairId, classId }`
  - Auto-save option: save on every "Next" click
  - Show success toast on save, error alert on failure
- Edge cases:
  - If task is completed (all pairs annotated), show a completion message with "Retour aux tâches" link
  - If API returns an error, show error alert with retry option

## State Management (per component)
```jsx
// AnnotationWorkspace state shape
const [currentPage, setCurrentPage] = useState(0);
const [selectedClass, setSelectedClass] = useState(null);
const [pairs, setPairs] = useState([]);       // cached pairs for this session
const [totalPages, setTotalPages] = useState(0);
const { loading, error } = useApi(...);
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
