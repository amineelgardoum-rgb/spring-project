import { useState, useEffect } from 'react';
import { useParams, useSearchParams, useNavigate, useLocation } from 'react-router-dom';
import { getTaskDetail, submitAnnotation } from '../../api/annotatorApi';
import Spinner from '../../components/Spinner';
import { useToast } from '../../components/Toast';

export default function AnnotationWorkspace() {
  const { taskId } = useParams();
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { addToast } = useToast();

  const resumePage = location.state?.resumePage ?? 0;
  const totalAvailable = location.state?.totalItems ?? 0;
  const pageFromParams = parseInt(searchParams.get('page'), 10) || 0;
  const initialPage = searchParams.has('page') ? pageFromParams : (resumePage || pageFromParams);
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [selectedClass, setSelectedClass] = useState(null);
  const [currentPair, setCurrentPair] = useState(null);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [finished, setFinished] = useState(false);
  const [error, setError] = useState(false);
  const [startTime, setStartTime] = useState(Date.now());

  useEffect(() => {
    if (resumePage > 0 && !searchParams.has('page')) {
      setSearchParams({ page: resumePage }, { replace: true });
    }
    if (totalAvailable > 0 && resumePage >= totalAvailable) {
      setFinished(true);
    }
  }, []);

  useEffect(() => {
    const fetchPair = async () => {
      setLoading(true);
      setSelectedClass(null);
      try {
        const res = await getTaskDetail(taskId, currentPage);
        const data = res.data;
        if (data.content?.length > 0) {
          const pair = data.content[0];
          setCurrentPair(pair);
          setStartTime(Date.now());
          if (pair.currentLabel) {
            setSelectedClass(pair.currentLabel);
          }
        } else {
          setCurrentPair(null);
        }
        setTotalPages(data.totalPages || 0);
      } catch {
        addToast('Erreur lors du chargement', 'error');
        setCurrentPair(null);
        setError(true);
      } finally {
        setLoading(false);
      }
    };
    fetchPair();
  }, [taskId, currentPage, addToast]);

  const goToPage = (page) => {
    const next = Math.max(0, Math.min(page, totalPages - 1));
    setCurrentPage(next);
    setSearchParams({ page: next });
  };

  const handleValidate = async () => {
    if (!selectedClass) {
      addToast('Veuillez sélectionner une classe', 'error');
      return;
    }
    if (!currentPair) return;
    setSaving(true);
    const duration = (Date.now() - startTime) / 1000;
    try {
      await submitAnnotation(taskId, currentPair.textItemId, selectedClass, duration);
      addToast('Annotation enregistrée', 'success');
      if (currentPage < totalPages - 1) {
        goToPage(currentPage + 1);
      } else {
        setCurrentPair(null);
        setFinished(true);
      }
    } catch {
      addToast("Erreur lors de l'enregistrement", 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleSkip = () => {
    if (currentPage < totalPages - 1) {
      setSelectedClass(null);
      goToPage(currentPage + 1);
    }
  };

  if (loading) {
    return <Spinner size="lg" className="mt-20" />;
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] text-center">
        <p className="text-lg mb-4 text-red-600 dark:text-red-400">Erreur de chargement</p>
        <p className="text-sm text-gray-500 dark:text-gray-400 mb-6">Impossible de charger les données de cette tâche.</p>
        <button
          onClick={() => navigate('/annotator')}
          className="text-primary hover:underline cursor-pointer"
        >
          Retour aux tâches
        </button>
      </div>
    );
  }

  if (finished || (!currentPair && totalPages === 0)) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] text-center">
        <p className="text-lg mb-4">Tâche terminée!</p>
        <button
          onClick={() => navigate('/annotator')}
          className="text-primary hover:underline cursor-pointer"
        >
          Retour aux tâches
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto p-4">
      <div className="mb-4">
        <h1 className="text-xl font-bold">Id couple: {currentPair?.textItemId}</h1>
      </div>

      <div className="mb-4">
        <p className="text-sm mb-1">
          Question {currentPage + 1} / {totalPages}
        </p>
        <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
          <div
            className="bg-primary h-2 rounded-full transition-all duration-300"
            style={{ width: `${((currentPage + 1) / totalPages) * 100}%` }}
          />
        </div>
      </div>

      <div className="grid gap-4 mb-4">
        <div className="border rounded-lg p-4 bg-white dark:bg-gray-800">
          <p className="text-xs font-semibold uppercase text-gray-500 dark:text-gray-400 mb-2">Texte 1</p>
          <p>{currentPair?.content}</p>
        </div>
        {currentPair?.pairContent && (
          <div className="border rounded-lg p-4 bg-white dark:bg-gray-800">
            <p className="text-xs font-semibold uppercase text-gray-500 dark:text-gray-400 mb-2">Texte 2</p>
            <p>{currentPair.pairContent}</p>
          </div>
        )}
      </div>

      <div className="mb-6">
        <p className="text-sm font-semibold mb-2">Classe</p>
        <div className="flex gap-4">
          {currentPair?.availableLabels?.map((label) => (
            <label key={label} className="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                name="class"
                value={label}
                checked={selectedClass === label}
                onChange={() => setSelectedClass(label)}
                className="accent-primary"
              />
              {label}
            </label>
          ))}
        </div>
      </div>

      <div className="flex justify-between items-center border-t pt-4">
        <button
          onClick={() => goToPage(currentPage - 1)}
          disabled={currentPage === 0}
          className="px-4 py-2 rounded border disabled:opacity-40 hover:bg-gray-100 dark:hover:bg-gray-700 dark:border-gray-600 cursor-pointer disabled:cursor-default"
        >
          &lt;&lt; Précédent
        </button>

        <div className="flex gap-2">
          <button
            onClick={handleValidate}
            disabled={saving}
            className="px-4 py-2 rounded bg-primary text-white hover:opacity-90 disabled:opacity-50 cursor-pointer disabled:cursor-default"
          >
            {saving ? '...' : 'Valider'}
          </button>
          <button
            onClick={handleSkip}
            disabled={currentPage >= totalPages - 1}
            className="px-4 py-2 rounded border hover:bg-gray-100 dark:hover:bg-gray-700 dark:border-gray-600 cursor-pointer disabled:opacity-40 disabled:cursor-default"
          >
            Suivant &gt;&gt;
          </button>
        </div>
      </div>
    </div>
  );
}
