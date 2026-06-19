import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getStats } from '../../api/annotatorApi';
import Spinner from '../../components/Spinner';

export default function AnnotatorStats() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getStats()
      .then((res) => setStats(res.data))
      .catch(() => setStats(null))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[60vh]">
        <Spinner size="lg" />
      </div>
    );
  }

  if (!stats) {
    return (
      <div className="flex justify-center items-center min-h-[60vh]">
        <p className="text-gray-500 dark:text-gray-400">Impossible de charger les statistiques</p>
      </div>
    );
  }

  const { totalAnnotated, avgTimePerAnnotation, classDistribution } = stats;

  const maxCount = Math.max(...Object.values(classDistribution || {}), 1);
  const colors = ['bg-blue-500', 'bg-green-500', 'bg-purple-500', 'bg-orange-500', 'bg-pink-500', 'bg-teal-500'];

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6 dark:text-white">Mes Statistiques</h1>

      <div className="grid grid-cols-2 gap-4 mb-8">
        <div className="rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400 mb-1">Textes annotés</p>
              <p className="text-3xl font-bold dark:text-white">{totalAnnotated}</p>
            </div>
            <svg className="h-8 w-8 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
        </div>

        <div className="rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400 mb-1">Temps moyen / annotation</p>
              <p className="text-3xl font-bold dark:text-white">{avgTimePerAnnotation?.toFixed(1)}s</p>
            </div>
            <svg className="h-8 w-8 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
        </div>
      </div>

      {classDistribution && Object.keys(classDistribution).length > 0 && (
        <div className="rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-6 shadow-sm mb-8">
          <h2 className="text-lg font-semibold mb-4 dark:text-white">Répartition par classe</h2>
          <div className="space-y-3">
            {Object.entries(classDistribution).map(([label, count], i) => (
              <div key={label}>
                <div className="flex justify-between items-center mb-1">
                  <span className="text-sm font-medium dark:text-gray-200 capitalize">{label}</span>
                  <span className="text-sm font-bold dark:text-gray-300">{count}</span>
                </div>
                <div className="h-3 bg-gray-100 dark:bg-gray-700 rounded-full overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all duration-300 ${colors[i % colors.length]}`}
                    style={{ width: `${(count / maxCount) * 100}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <Link
        to="/annotator"
        className="inline-block px-4 py-2 text-sm font-medium text-primary border border-primary rounded hover:bg-primary hover:text-white transition-colors"
      >
        Retour aux tâches
      </Link>
    </div>
  );
}
