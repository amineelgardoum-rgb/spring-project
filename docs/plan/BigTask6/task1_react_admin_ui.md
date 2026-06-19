# Task 1: React Admin Components

## Description
Implement the frontend screens for the Admin role using React + Tailwind CSS.

## Prerequisites
- React project initialized (see BigTask1/task2_react_setup.md)
- Axios instance with JWT interceptor configured
- AuthContext and ProtectedRoute in place
- AdminLayout component with sidebar navigation

## Route setup
All admin pages are rendered inside `<AdminLayout>` which provides:
- Left sidebar with navigation links: Dashboard, Datasets, Upload Dataset, Annotators, NLP Dashboard
- Top header bar with user info and logout button

```
/admin                  → AdminDashboard.jsx   (index route)
/admin/annotators       → AnnotatorManagement.jsx
/admin/datasets         → DatasetsList.jsx
/admin/datasets/new     → DatasetUpload.jsx
/admin/datasets/:id     → DatasetDetail.jsx
/admin/nlp              → NlpDashboard.jsx
```

## Components

### AdminDashboard.jsx
- Route: `/admin` (index route, default after login)
- Calls `GET /api/admin/dashboard/stats` on mount
- Displays summary cards:
  - Total datasets, total texts, total annotators, overall annotation %
- **Global class distribution**: horizontal bar chart or colored badges showing how many texts were labeled per class across all datasets
- **Annotator progress table**: list of annotators with their personal stats (annotated count, avg time, class distribution)
- **Spammer alerts**: if any annotators are flagged as spammers (`spammerIds` from metrics), show a red alert banner with their names
- Quick links to Datasets, NLP Dashboard

### DatasetsList.jsx
- Calls `GET /api/admin/datasets` on mount
- Displays a table with columns: Nom, % Avancement, Actions
- Actions per row: `Détails` → navigates to `/admin/datasets/{id}`, `Ajouter Annotateurs` → opens assign modal
- Pagination controls at bottom
- Clicking a row navigates to `/admin/datasets/{id}` for detail view

### DatasetDetail.jsx
- Calls `GET /api/admin/datasets/{id}` on mount
- **Header info**: Taille (numRecords), Nom dataset, % avancement, Description, Classes (labels)
- Four sections:
  - **Assignment section**: "Assigner des annotateurs" button opens a modal with a multi-select list of available annotators → calls `POST /api/admin/datasets/{id}/assign` with selected `annotatorIds`. System automatically distributes pairs with redundancy of 3
  - **Text pairs table**: paginated list of `(Text1, Text2)` pairs (100 per page). Click a pair → loads its annotations below
  - **Annotators card**: list of assigned annotators with their progress (annotated/total count). Each has a "Supprimer" button → calls `DELETE /api/admin/datasets/{id}/annotators/{userId}` with confirmation dialog
- When a text pair is selected, fetch `GET /api/admin/annotations?textItemId=X` and display each annotation (annotator name, class chosen, date). Admin can change the class via a dropdown + "Corriger" button → calls `PUT /api/admin/annotations/{id}`
- Export link: `GET /api/admin/datasets/{id}/export?format=csv|json`

### DatasetUpload.jsx
- Form with (matching forms.md UC2):
  - **Fichier**: File input (accepts `.csv`, `.json`) with upload button
  - **Nom**: Text input for dataset name
  - **Classes**: Text input for classes separated by semicolons (e.g. `N;E;C` or `Similar;Not Similar`)
  - **Description**: Textarea for dataset description
- Buttons: `Annuler` (Cancel) → navigate back, `Créer` (Create) → submit
- Calls `POST /api/admin/datasets/upload` with `multipart/form-data` (params: `file`, `tags`)
- Shows upload progress and success/error toast

### AnnotatorManagement.jsx
- Calls `GET /api/admin/users` on mount
- Header action: `+ Ajouter` button → opens add modal (UC4-4)
- Displays table columns (matching forms.md UC4): Nom, Prénom, Actions
- **Add modal (UC4-4)**:
  - Fields: `Nom` (lastName), `Prénom` (firstName), `Login` (username)
  - ⚠️ No password field — backend auto-generates password; display it to admin once in a success alert
  - Button: `Valider` → calls `POST /api/admin/users`
- **Edit action**: button `Modifier` opens pre-filled modal (firstName, lastName, username) → calls `PUT /api/admin/users/{id}`
- **Delete action**: button `Supprimer` → confirmation dialog → calls `DELETE /api/admin/users/{id}` (soft delete — backend sets `deleted = true`)
- Uses Tailwind modal component for add/edit

### NlpDashboard.jsx
- **Train section**:
  - Collapsible "Hyperparamètres" form with inputs: Learning Rate, Epochs, Batch Size
  - "Train Model" button → calls `POST /api/admin/nlp/train` with hyperparameters body `{ learningRate, epochs, batchSize }`
  - Shows loading spinner, then displays job status (Job entity with status field)
- **Test section**:
  - "Test Model" button → calls `POST /api/admin/nlp/test`
  - Displays results card: accuracy, F1-score
  - Renders confusion matrix as a styled Tailwind grid/table (e.g., a 2×2 or N×N matrix with color-coded cells)
- **History section**: fetches `GET /api/admin/nlp/logs`
  - Table: Date, Status, Accuracy, F1-Score, Hyperparameters, Actions (view logs)
  - **Performance chart**: include a simple line chart (using a lightweight chart library like Chart.js or Recharts) showing accuracy/F1-score over time across multiple training runs
  - Uses Tailwind table styling
- **Metrics section**: "Voir métriques dataset" button → `GET /api/admin/datasets/{id}/metrics` (Fleiss' Kappa, class distribution per dataset)

## Data Fetching Pattern
```jsx
// Custom hook pattern for all API calls
const { data, loading, error } = useApi(() => adminApi.getDatasets(page));
if (loading) return <Spinner />;
if (error) return <ErrorAlert message={error} />;
return <Table data={data} />;
```

## Styling
- All styling via Tailwind utility classes
- Shared color tokens (indigo primary, green success, red danger)
- Responsive: sidebar collapses on mobile
- Loading states: use Tailwind `animate-spin` for spinners
- Use Tailwind cards for metrics/NLP results

## How to verify
1. Navigate to each admin route and confirm it renders
2. **Dashboard** shows global stats, class distribution, annotator progress, spammer alerts
3. Upload a CSV/JSON dataset successfully; check validation rejects rows without `id`/`texte`
4. Create, list, edit, and soft-delete an annotator
5. Click a dataset row → see detail page with text pairs, annotators, and assignment modal
6. Assign annotators via the modal; check distribution is automatic
7. De-assign an annotator from the dataset detail page
8. View annotations for a text pair and correct one
9. Export a dataset as CSV and JSON; verify columns `id, texte, classe, annotateur, date_annotation`
10. Trigger NLP train with custom hyperparameters
11. Trigger NLP test and see accuracy, F1-score, and confusion matrix
12. Check the performance chart in the NLP history section
13. All API calls include JWT Bearer token (check Network tab)

See `architecture.md` in this folder for architectural decisions.
