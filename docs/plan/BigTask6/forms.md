# Front-End UI Specifications: Text Annotation Platform

## Overview
This document outlines the user interface components, views, and business logic for a text dataset annotation platform (specifically for NLP tasks like NLI or Text Similarity). The system has two primary roles: **Admin** (`Administrateur`) and **Annotator** (`Annotateur`).

---

## 1. Admin Interfaces & Use Cases

### UC1: Admin Dashboard (`Tableau de bord`)
The landing page after admin login, showing global platform statistics.
* **API:** `GET /api/admin/dashboard/stats`
* **Summary cards:** Total datasets, total texts, total annotators, total annotations, total assignments, overall annotation %
* **Global class distribution:** Badges or horizontal bar chart showing count per label across all datasets
* **Annotator progress table:** List of annotators with annotated count, avg time, class distribution
* **Spammer alerts:** Red banner listing annotator names (from `spammers[].firstName + lastName`) flagged as spammers (95%+ single label)
* **Quick links:** Navigate to Datasets, NLP Dashboard

### UC2: Create a Dataset (`Création d'un dataset`)
A form interface for the admin to upload and configure a new dataset.
* **API:** `POST /api/admin/datasets/upload` (multipart/form-data with params `file` and `tags`)
* **Fields:**
  * `Fichier`: File upload input with an "upload" button.
  * `Nom`: Text input for the dataset name (e.g., "NLI-AR").
  * `Classes`: Text input for the allowed annotation labels, separated by semicolons (e.g., "N; E; C" or "Similar; Not Similar").
  * `Description`: Textarea for dataset instructions.
* **Actions:**
  * `Annuler` (Cancel) button.
  * `Créer` (Create) button.

### UC3: Dataset Management (`Liste des datasets`)
A main dashboard view displaying all datasets.
* **API:** `GET /api/admin/datasets`
* **Header action:** `+ Créer dataset` button → Routes to UC2
* **Data Table columns:** `Nom`, `Nb Annotations` (field: `totalAnnotations`), `% Avancement` (field: `completionPercentage`), `Actions`.
* **Actions per row:** `Détails` → Routes to UC3-1. `Ajouter Annotateurs` → Routes to UC3.3.

### UC3-1: Dataset Details (`Afficher détails dataset`)
A detailed view of a specific dataset.
* **API:** `GET /api/admin/datasets/{id}`
* **Header Info:** Shows `Taille` (Total size/pairs), `Nom dataset`, `% avancement`, `Description`, and `Classes`.
* **Sub-Table 1: List of Text Couples (`Liste des couples`)**
  * Columns: `Id`, `texte 1`, `texte 2`.
  * Features: Pagination controls (`<< précédent`, `suivant >>`) showing ~100 couples per page.
  * Click a couple → loads its annotations below via `GET /api/admin/annotations?textItemId=X`
  * Admin can correct an annotation with a dropdown + `Corriger` button → `PUT /api/admin/annotations/{id}`
* **Sub-Table 2: Assigned Annotators (`Annotateurs affectés`)**
  * Columns: `Id`, `nom`, `Prénom`, `Action`.
  * Actions: `Supprimer` → `DELETE /api/admin/datasets/{id}/annotators/{userId}` (with confirmation dialog)
* **Export:** Download as CSV/JSON via `GET /api/admin/datasets/{id}/export?format=csv|json`

### UC3.3: Assign Annotators (`Ajouter/Affecter annotateurs`)
A modal or page to assign multiple users to a dataset.
* **API:** `POST /api/admin/datasets/{id}/assign` with body `{ annotatorIds: [...] }`
* **Data Table columns:** `Annotateurs` (Full name), `Sélection` (Checkboxes).
* **Actions:** `Valider` (Submit selection) button.

### UC4: Annotator Management (`Gestion annotateurs`)
A CRUD dashboard for managing annotator accounts.
* **API:** `GET /api/admin/users`, `PUT /api/admin/users/{id}`, `DELETE /api/admin/users/{id}`
* **Header Action:** `+ Ajouter` button (Routes to UC4-4).
* **Data Table columns:** `Nom`, `Prénom`, `Actions`.
* **Actions per row:**
  * `Modifier` (Edit user) → opens pre-filled modal, calls `PUT /api/admin/users/{id}`
  * `Supprimer` (Delete user) → confirmation dialog → calls `DELETE /api/admin/users/{id}`
* **⚠️ CRITICAL BUSINESS LOGIC:** The deletion of an annotator must be a **Logical Deletion** (soft delete, `deleted = true`), NOT a physical database deletion, to preserve annotation history.

### UC4-4: Add Annotator (`Ajouter un annotateur`)
Form to create a new annotator profile.
* **API:** `POST /api/admin/users` with body `{ firstName, lastName, username }`
* **Fields:** * `Nom` (Last Name): Text input.
  * `Prénom` (First Name): Text input.
  * `Login` (Username/Email): Text input.
* **Actions:** `Valider` (Submit) button.
* **⚠️ CRITICAL BUSINESS LOGIC:** There is no password input field on this form. The password **must be generated automatically** by the system backend.

### UC5: Advanced Options (`Options avancés`)
A dashboard for quality assurance (integrated into NlpDashboard).
* **Data Table columns:** `Id`, `Nom dataset`, `Actions`.
* **Actions per row:**
  * `Afficher métrique` (UC5-1): Calls `GET /api/admin/datasets/{id}/metrics` → displays Fleiss' Kappa, per-annotator counts, class distribution
      * `Spammeurs` (UC5-2): Uses `GET /api/admin/dashboard/stats` `spammers` list (with `firstName`, `lastName`) → highlights flagged annotators
* **NLP Training:** `POST /api/admin/nlp/train` with hyperparams `{ learningRate, epochs, batchSize }` → returns job ID. Poll `GET /api/jobs/{id}` for status updates (PENDING → RUNNING → COMPLETED/FAILED)
* **NLP Testing:** `POST /api/admin/nlp/test` → returns job ID. Poll `GET /api/jobs/{id}` for completion
* **NLP History:** `GET /api/admin/nlp/logs` — table of past runs with accuracy, F1-score, confusion matrix

---

## 2. Annotator Interfaces & Use Cases

### UC6: Task List (`Listes tâches`)
The main dashboard for an Annotator when they log in.
* **API:** `GET /api/annotator/tasks`
* **Data Table columns:** * `Id`
  * `Nom dataset`
  * `Date limite` (Deadline)
  * `% Avancement` (The annotator's *personal* progress on this specific task).
  * `Taille` (Size/Number of pairs assigned to them).
  * `Actions`.
* **Actions per row:** `Travailler` (Start/Resume annotating -> Routes to UC7).

### UC7: Annotation Workspace (`Travailler une tâche`)
The core UI where the actual labeling happens.
* **API:** `GET /api/annotator/tasks/{taskId}/pairs?page={n}` to fetch current pair, `POST /api/annotator/tasks/{taskId}/annotate` with `{ textItemId, label }` to save
* **Header Info:** `Id couple` and a `Description` link/tooltip.
* **Data Display:**
  * `Texte 1`: Read-only text box/card (always present, from `content` field).
  * `Texte 2`: Read-only text box/card (only if `pairContent` is not null).
* **Inputs:**
  * Radio buttons for the classes (e.g., `not similar` [ ], `Similar` [x]). These should render dynamically based on the dataset's defined labels from the API response.
* **Navigation/Actions:**
  * `<< Précédent` — Go to previous pair (disabled on first page).
  * `Valider` — **Save current annotation and load next pair**. Calls `POST /api/annotator/tasks/{taskId}/annotate`. Shows validation error if no class selected.
  * `Suivant >>` — Skip to next pair **without saving** (clears selected class).
* **Edge cases:**
  * If all pairs are annotated, show completion message with "Retour aux tâches" link.
  * If annotator already annotated a pair, pre-select their saved class.
  * Disable Valider/Suivant on the last page.

### UC8: Annotator Stats (`Mes Statistiques`)
The annotator's personal statistics page.
* **API:** `GET /api/annotator/stats`
* **Response shape:**
  ```json
  {
    "totalAnnotated": 45,
    "avgTimePerAnnotation": 12.5,
    "classDistribution": { "positif": 20, "negatif": 15, "neutre": 10 }
  }
  ```
* **Stat cards:** Total annotated texts (large number), Average annotation time (seconds)
* **Class distribution:** List of class names with colored badges showing counts
* **Navigation:** "Retour aux tâches" link → back to UC6

---

## 3. Core Data Entities (For AI Type/Interface Generation)
Based on the UML diagram, the AI should construct the following primary objects/interfaces:

* **Utilisateur (Base):** `Id`, `nom`, `prénom`, `login`, `password` (hashed). Roles: `Administrateur`, `Annotateur`.
* **Dataset:** `Id`, `nomDataset`, `description`.
* **Tâche (Task):** Links Dataset to Annotator. Contains `Id`, `dateLimite`.
* **CoupleTexte:** `Id`, `texte1`, `texte2`. Linked to Tasks.
* **ClassePossible:** `Id`, `textClasse` (e.g., Similar, Neutral). Linked to Dataset.
* **Annotation:** The resulting entity linking an `Annotateur`, a `CoupleTexte`, and the `classeChoisie`.