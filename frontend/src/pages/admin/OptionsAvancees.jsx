import { useEffect, useState } from 'react';
import { getDatasets, getDatasetMetrics } from '../../api/adminApi';
import Spinner from '../../components/Spinner';
import Modal from '../../components/Modal';

const barColors = ['bg-blue-500', 'bg-green-500', 'bg-purple-500', 'bg-orange-500', 'bg-pink-500', 'bg-teal-500'];

function MetricsModal({ open, onClose, datasetName, metrics }) {
  if (!metrics) return null;

  const {
    totalItems,
    totalAnnotations,
    assignedAnnotators,
    fleissKappa,
    overallClassDistribution,
    annotationsPerAnnotator,
  } = metrics;

  const entries = Object.entries(overallClassDistribution || {});
  const maxCount = Math.max(...entries.map(([, c]) => c), 1);
  const annotatorEntries = Object.entries(annotationsPerAnnotator || {});
  const maxAnnot = Math.max(...annotatorEntries.map(([, c]) => c), 1);

  return (
    <Modal open={open} onClose={onClose} title={`Métriques — ${datasetName}`}>
      <div className="space-y-5">
        <div className="grid grid-cols-3 gap-3">
          <div className="rounded-lg border border-gray-200 dark:border-gray-700 p-3 text-center">
            <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Items</p>
            <p className="text-xl font-bold dark:text-white">{totalItems}</p>
          </div>
          <div className="rounded-lg border border-gray-200 dark:border-gray-700 p-3 text-center">
            <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Annotations</p>
            <p className="text-xl font-bold dark:text-white">{totalAnnotations}</p>
          </div>
          <div className="rounded-lg border border-gray-200 dark:border-gray-700 p-3 text-center">
            <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">Annotateurs</p>
            <p className="text-xl font-bold dark:text-white">{assignedAnnotators}</p>
          </div>
        </div>

        <div>
          <p className="text-sm font-semibold mb-2 dark:text-gray-200">Fleiss' Kappa</p>
          {fleissKappa != null ? (
            <div className="flex items-center gap-2">
              <span className="text-lg font-bold dark:text-white">{fleissKappa.toFixed(4)}</span>
              <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${
                fleissKappa >= 0.6 ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400' :
                fleissKappa >= 0.3 ? 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400' :
                'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'
              }`}>
                {fleissKappa >= 0.6 ? 'Bon accord' : fleissKappa >= 0.3 ? 'Accord modéré' : 'Accord faible'}
              </span>
            </div>
          ) : (
            <p className="text-sm text-gray-500 dark:text-gray-400 italic">
              Pas assez de données pour le calcul
            </p>
          )}
        </div>

        {entries.length > 0 && (
          <div>
            <p className="text-sm font-semibold mb-2 dark:text-gray-200">Distribution des classes</p>
            <div className="space-y-2">
              {entries.map(([label, count], i) => (
                <div key={label}>
                  <div className="flex justify-between text-xs mb-0.5">
                    <span className="dark:text-gray-300 capitalize">{label}</span>
                    <span className="font-medium dark:text-gray-300">{count}</span>
                  </div>
                  <div className="h-3 bg-gray-100 dark:bg-gray-700 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all ${barColors[i % barColors.length]}`}
                      style={{ width: `${(count / maxCount) * 100}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {annotatorEntries.length > 0 && (
          <div>
            <p className="text-sm font-semibold mb-2 dark:text-gray-200">Annotations par annotateur</p>
            <div className="space-y-2">
              {annotatorEntries.map(([annotatorId, count]) => (
                <div key={annotatorId}>
                  <div className="flex justify-between text-xs mb-0.5">
                    <span className="dark:text-gray-300">Annotateur #{annotatorId}</span>
                    <span className="font-medium dark:text-gray-300">{count}</span>
                  </div>
                  <div className="h-3 bg-gray-100 dark:bg-gray-700 rounded-full overflow-hidden">
                    <div
                      className="h-full rounded-full transition-all bg-primary"
                      style={{ width: `${(count / maxAnnot) * 100}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </Modal>
  );
}

export default function OptionsAvancees() {
  const [datasets, setDatasets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [metricsData, setMetricsData] = useState(null);
  const [metricsDatasetName, setMetricsDatasetName] = useState('');
  const [metricsOpen, setMetricsOpen] = useState(false);

  useEffect(() => {
    getDatasets()
      .then((res) => setDatasets(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handleAfficherMetrique = (datasetId, datasetName) => {
    getDatasetMetrics(datasetId)
      .then((res) => {
        setMetricsData(res.data);
        setMetricsDatasetName(datasetName);
        setMetricsOpen(true);
      })
      .catch(() => {
        setMetricsData(null);
        setMetricsDatasetName(datasetName);
        setMetricsOpen(true);
      });
  };

  const handleSpammeurs = (datasetId, datasetName) => {
    alert(`Détection des spammeurs pour "${datasetName}" (ID: ${datasetId}) — fonctionnalité à venir`);
  };

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6 dark:text-white">Options avancées</h1>

      {loading ? (
        <Spinner size="lg" className="mt-20" />
      ) : datasets.length === 0 ? (
        <p className="mt-20 text-center text-gray-500 dark:text-gray-400">Aucun dataset trouvé.</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-gray-700 dark:text-gray-300">
            <thead>
              <tr className="border-b border-gray-200 dark:border-gray-700">
                <th className="pb-3 pr-4 font-semibold">Id</th>
                <th className="pb-3 pr-4 font-semibold">nom dataset</th>
                <th className="pb-3 font-semibold">Actions</th>
              </tr>
            </thead>
            <tbody>
              {datasets.map((ds) => (
                <tr key={ds.id} className="border-b border-gray-100 dark:border-gray-800">
                  <td className="py-3 pr-4">{ds.id}</td>
                  <td className="py-3 pr-4">{ds.name}</td>
                  <td className="py-3">
                    <div className="flex gap-2">
                      <button
                        onClick={() => handleAfficherMetrique(ds.id, ds.name)}
                        className="rounded bg-green-600 px-3 py-1 text-xs font-medium text-white hover:bg-green-700 dark:bg-green-500 dark:hover:bg-green-600 cursor-pointer"
                      >
                        afficher métrique
                      </button>
                      <button
                        onClick={() => handleSpammeurs(ds.id, ds.name)}
                        className="rounded bg-red-600 px-3 py-1 text-xs font-medium text-white hover:bg-red-700 dark:bg-red-500 dark:hover:bg-red-600 cursor-pointer"
                      >
                        Spammeurs
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <MetricsModal
        open={metricsOpen}
        onClose={() => setMetricsOpen(false)}
        datasetName={metricsDatasetName}
        metrics={metricsData}
      />
    </div>
  );
}
