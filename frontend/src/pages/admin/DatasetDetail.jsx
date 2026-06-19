import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  getDatasetDetail,
  getUsers,
  assignAnnotators,
  removeAnnotator,
  getAnnotations,
  updateAnnotation,
  exportDataset,
} from '../../api/adminApi';
import Spinner from '../../components/Spinner';
import Modal from '../../components/Modal';
import { useToast } from '../../components/Toast';

export default function DatasetDetail() {
  const { id } = useParams();
  const { addToast } = useToast();

  const [dataset, setDataset] = useState(null);
  const [loading, setLoading] = useState(true);
  const [users, setUsers] = useState([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedIds, setSelectedIds] = useState([]);
  const [page, setPage] = useState(1);
  const [selectedTextItem, setSelectedTextItem] = useState(null);
  const [textAnnotations, setTextAnnotations] = useState([]);
  const [annotationsLoading, setAnnotationsLoading] = useState(false);
  const perPage = 5;

   const fetchDataset = () => {
     setLoading(true);
     getDatasetDetail(id)
       .then((res) => {
         console.log('Dataset detail response:', res.data);
         setDataset(res.data);
         setPage(1);
         setSelectedTextItem(null);
         setTextAnnotations([]);
       })
       .catch(() => addToast('Erreur lors du chargement du dataset', 'error'))
       .finally(() => setLoading(false));
   };

  useEffect(() => {
    fetchDataset();
    getUsers()
      .then((res) => setUsers(res.data.content ?? res.data ?? []))
      .catch(() => {});
  }, [id]);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    if (params.get('assign') === 'true') {
      window.history.replaceState({}, '', window.location.pathname);
      setSelectedIds([]);
      setModalOpen(true);
    }
  }, []);

  const handleAssign = () => {
    assignAnnotators(id, selectedIds)
      .then(() => {
        addToast('Annotateurs assignés avec succès', 'success');
        setModalOpen(false);
        setSelectedIds([]);
        fetchDataset();
      })
      .catch(() => addToast("Erreur lors de l'assignation", 'error'));
  };

  const handleRemoveAnnotator = (userId) => {
    if (!window.confirm('Supprimer cet annotateur du dataset ?')) return;
    removeAnnotator(id, userId)
      .then(() => {
        addToast('Annotateur retiré', 'success');
        fetchDataset();
      })
      .catch(() => addToast('Erreur lors de la suppression', 'error'));
  };

  const handleSelectAnnotator = (userId) => {
    setSelectedIds((prev) =>
      prev.includes(userId) ? prev.filter((u) => u !== userId) : [...prev, userId]
    );
  };

  const handleTextItemClick = (item) => {
    setSelectedTextItem(item);
    setAnnotationsLoading(true);
    getAnnotations(item.id)
      .then((res) => setTextAnnotations(res.data))
      .catch(() => addToast('Erreur lors du chargement des annotations', 'error'))
      .finally(() => setAnnotationsLoading(false));
  };

  const handleCorrection = (annotationId, label, comment) => {
    updateAnnotation(annotationId, { label, comment })
      .then(() => {
        addToast('Annotation corrigée', 'success');
        if (selectedTextItem) {
          getAnnotations(selectedTextItem.id)
            .then((res) => setTextAnnotations(res.data))
            .catch(() => {});
        }
      })
      .catch(() => addToast('Erreur lors de la correction', 'error'));
  };

  const handleExport = (format) => {
    exportDataset(id, format)
      .then((res) => {
        const url = window.URL.createObjectURL(new Blob([res.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `dataset-${id}.${format}`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        addToast('Export lancé', 'success');
      })
      .catch(() => addToast("Erreur lors de l'export", 'error'));
  };

  const assignedIds = dataset?.annotators?.map((a) => a.id) || [];
  const availableUsers = users.filter((u) => !assignedIds.includes(u.id));

  const textItems = dataset?.textItems || [];
  const totalPages = Math.ceil(textItems.length / perPage);
  const paginatedItems = textItems.slice((page - 1) * perPage, page * perPage);

  if (loading) return <Spinner size="lg" className="mt-20" />;

  if (!dataset) {
    return (
      <p className="text-center text-gray-500 dark:text-gray-400 mt-20">
        Dataset introuvable.
      </p>
    );
  }

  return (
    <div className="space-y-8">
      {/* Header info card */}
      <div className="bg-white dark:bg-dark-surface rounded-xl shadow p-6 space-y-3">
        <div className="flex items-start justify-between flex-wrap gap-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
              {dataset.name}
            </h1>
            {dataset.description && (
              <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                {dataset.description}
              </p>
            )}
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => handleExport('json')}
              className="px-4 py-2 text-sm rounded-lg bg-primary text-white hover:bg-primary-dark transition"
            >
              Exporter JSON
            </button>
            <button
              onClick={() => handleExport('csv')}
              className="px-4 py-2 text-sm rounded-lg bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-gray-200 hover:bg-gray-300 dark:hover:bg-gray-600 transition"
            >
              Exporter CSV
            </button>
          </div>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-sm">
          <div>
            <span className="text-gray-500 dark:text-gray-400">Taille :</span>{' '}
            <span className="font-semibold text-gray-800 dark:text-gray-200">
              {dataset.numRecords}
            </span>
          </div>
          <div>
            <span className="text-gray-500 dark:text-gray-400">Avancement :</span>{' '}
            <span className="font-semibold text-gray-800 dark:text-gray-200">
              {dataset.progress != null ? `${dataset.progress}%` : '—'}
            </span>
          </div>
        </div>
        {dataset.labels?.length > 0 && (
          <div className="flex flex-wrap gap-2">
            {dataset.labels.map((label, idx) => (
              <span
                key={idx}
                className="px-3 py-1 rounded-full text-xs font-medium bg-green-100 text-green-700 dark:bg-green-900/40 dark:text-green-300"
              >
                {label}
              </span>
            ))}
          </div>
        )}
      </div>

      {/* Section 1: Assign annotators */}
      <section className="bg-white dark:bg-dark-surface rounded-xl shadow p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
            Annotateurs
          </h2>
          <button
            onClick={() => {
              setSelectedIds([]);
              setModalOpen(true);
            }}
            className="px-4 py-2 text-sm rounded-lg bg-primary text-white hover:bg-primary-dark transition"
          >
            Assigner des annotateurs
          </button>
        </div>

        {dataset.annotators?.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead>
                <tr className="border-b border-gray-200 dark:border-dark-border text-gray-500 dark:text-gray-400">
                  <th className="pb-2 font-medium">Utilisateur</th>
                  <th className="pb-2 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {dataset.annotators.map((ann) => (
                  <tr key={ann.id} className="border-b border-gray-100 dark:border-dark-border/50">
                    <td className="py-2 text-gray-800 dark:text-gray-200">{ann.username || ann.name}</td>
                    <td className="py-2">
                      <button
                        onClick={() => handleRemoveAnnotator(ann.id)}
                        className="text-red-600 hover:text-red-800 dark:text-red-400 dark:hover:text-red-300 text-sm"
                      >
                        Supprimer
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Aucun annotateur assigné.
          </p>
        )}
      </section>

      {/* Section 2: Text items table */}
      {textItems.length > 0 && (
        <section className="bg-white dark:bg-dark-surface rounded-xl shadow p-6">
          <h2 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
            Textes
          </h2>
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead>
                <tr className="border-b border-gray-200 dark:border-dark-border text-gray-500 dark:text-gray-400">
                  <th className="pb-2 font-medium">ID</th>
                  <th className="pb-2 font-medium">Source</th>
                  <th className="pb-2 font-medium">Target</th>
                  <th className="pb-2 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {paginatedItems.map((item) => (
                  <tr
                    key={item.id}
                    className={`border-b border-gray-100 dark:border-dark-border/50 cursor-pointer transition ${
                      selectedTextItem?.id === item.id
                        ? 'bg-primary/5 dark:bg-primary/10'
                        : 'hover:bg-gray-50 dark:hover:bg-dark-border/20'
                    }`}
                    onClick={() => handleTextItemClick(item)}
                  >
                    <td className="py-2 text-gray-800 dark:text-gray-200">{item.id}</td>
                    <td className="py-2 text-gray-600 dark:text-gray-400 max-w-xs truncate">
                      {item.sourceText || item.source}
                    </td>
                    <td className="py-2 text-gray-600 dark:text-gray-400 max-w-xs truncate">
                      {item.targetText || item.target}
                    </td>
                    <td className="py-2">
                      <span className="text-primary text-xs">
                        {selectedTextItem?.id === item.id ? 'Sélectionné' : 'Voir annotations'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {totalPages > 1 && (
            <div className="flex items-center justify-between mt-4 text-sm">
              <button
                disabled={page <= 1}
                onClick={() => setPage((p) => Math.max(1, p - 1))}
                className="px-3 py-1 rounded border border-gray-300 dark:border-dark-border disabled:opacity-40 text-gray-700 dark:text-gray-300"
              >
                Précédent
              </button>
              <span className="text-gray-500 dark:text-gray-400">
                Page {page} / {totalPages}
              </span>
              <button
                disabled={page >= totalPages}
                onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                className="px-3 py-1 rounded border border-gray-300 dark:border-dark-border disabled:opacity-40 text-gray-700 dark:text-gray-300"
              >
                Suivant
              </button>
            </div>
          )}
        </section>
      )}

      {/* Section 4: Annotations for selected text item */}
      {selectedTextItem && (
        <section className="bg-white dark:bg-dark-surface rounded-xl shadow p-6">
          <h3 className="text-md font-semibold text-gray-900 dark:text-gray-100 mb-3">
            Annotations — Texte #{selectedTextItem.id}
          </h3>
          {annotationsLoading ? (
            <Spinner size="sm" />
          ) : textAnnotations.length === 0 ? (
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Aucune annotation pour ce texte.
            </p>
          ) : (
            <div className="space-y-4">
              {textAnnotations.map((ann) => (
                <AnnotationRow
                  key={ann.id}
                  annotation={ann}
                  classes={dataset.labels}
                  onCorrect={(label, comment) => handleCorrection(ann.id, label, comment)}
                />
              ))}
            </div>
          )}
        </section>
      )}

      {/* Modal for assigning annotators */}
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Assigner des annotateurs">
        {availableUsers.length === 0 ? (
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Tous les utilisateurs sont déjà assignés.
          </p>
        ) : (
          <div className="space-y-3 max-h-64 overflow-y-auto">
            {availableUsers.map((user) => (
              <label
                key={user.id}
                className="flex items-center gap-3 cursor-pointer text-sm text-gray-800 dark:text-gray-200"
              >
                <input
                  type="checkbox"
                  checked={selectedIds.includes(user.id)}
                  onChange={() => handleSelectAnnotator(user.id)}
                  className="accent-primary"
                />
                <span>{user.username || user.name}</span>
                <span className="text-gray-400 dark:text-gray-500">({user.email})</span>
              </label>
            ))}
          </div>
        )}
        <div className="flex justify-end gap-2 mt-4">
          <button
            onClick={() => setModalOpen(false)}
            className="px-4 py-2 text-sm rounded-lg border border-gray-300 dark:border-dark-border text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-dark-border/30 transition"
          >
            Annuler
          </button>
          <button
            onClick={handleAssign}
            disabled={selectedIds.length === 0}
            className="px-4 py-2 text-sm rounded-lg bg-primary text-white hover:bg-primary-dark transition disabled:opacity-50"
          >
            Valider
          </button>
        </div>
      </Modal>
    </div>
  );
}

function AnnotationRow({ annotation, classes, onCorrect }) {
  const [label, setLabel] = useState(annotation.label || '');
  const [comment, setComment] = useState(annotation.comment || '');
  const [editing, setEditing] = useState(false);

  useEffect(() => {
    setLabel(annotation.label || '');
    setComment(annotation.comment || '');
  }, [annotation]);

  return (
    <div className="border border-gray-200 dark:border-dark-border rounded-lg p-4 space-y-2 text-sm">
      <div className="flex items-start justify-between">
        <div className="space-y-1">
          <p className="text-gray-500 dark:text-gray-400">
            Annotateur :{' '}
            <span className="text-gray-800 dark:text-gray-200 font-medium">
              {annotation.annotatorName || annotation.annotator?.username || `#${annotation.annotatorId}`}
            </span>
          </p>
          {editing ? (
            <div className="flex flex-wrap items-center gap-2 mt-1">
              <select
                value={label}
                onChange={(e) => setLabel(e.target.value)}
                className="border border-gray-300 dark:border-dark-border rounded px-2 py-1 text-sm bg-white dark:bg-dark-surface text-gray-800 dark:text-gray-200"
              >
                <option value="">Sélectionner</option>
                {classes?.map((label, idx) => (
                  <option key={idx} value={label}>
                    {label}
                  </option>
                ))}
              </select>
              <input
                type="text"
                placeholder="Commentaire"
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                className="border border-gray-300 dark:border-dark-border rounded px-2 py-1 text-sm bg-white dark:bg-dark-surface text-gray-800 dark:text-gray-200 flex-1 min-w-[120px]"
              />
              <button
                onClick={() => {
                  onCorrect(label, comment);
                  setEditing(false);
                }}
                className="px-3 py-1 rounded bg-green-600 text-white text-xs hover:bg-green-700 transition"
              >
                Corriger
              </button>
              <button
                onClick={() => setEditing(false)}
                className="px-3 py-1 rounded border border-gray-300 dark:border-dark-border text-gray-600 dark:text-gray-400 text-xs hover:bg-gray-50 dark:hover:bg-dark-border/30 transition"
              >
                Annuler
              </button>
            </div>
          ) : (
            <div className="flex flex-wrap items-center gap-2 mt-1">
              {annotation.label && (
                <span className="px-2 py-0.5 rounded text-xs font-medium bg-purple-100 text-purple-700 dark:bg-purple-900/40 dark:text-purple-300">
                  {annotation.label}
                </span>
              )}
              {annotation.comment && (
                <span className="text-gray-500 dark:text-gray-400 italic">
                  "{annotation.comment}"
                </span>
              )}
              <button
                onClick={() => setEditing(true)}
                className="text-primary hover:text-primary-dark text-xs ml-2"
              >
                Modifier
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
