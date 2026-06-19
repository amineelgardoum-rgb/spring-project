import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getDatasets } from '../../api/adminApi';
import Spinner from '../../components/Spinner';

export default function DatasetsList() {
  const [datasets, setDatasets] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getDatasets()
      .then((res) => setDatasets(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Datasets</h1>
        <Link
          to="/admin/datasets/new"
          className="rounded bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 dark:bg-green-500 dark:hover:bg-green-600"
        >
          Créer dataset
        </Link>
      </div>

      {loading ? (
        <Spinner size="lg" className="mt-20" />
      ) : datasets.length === 0 ? (
        <p className="mt-20 text-center text-gray-500 dark:text-gray-400">Aucun dataset trouvé.</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-gray-700 dark:text-gray-300">
            <thead>
              <tr className="border-b border-gray-200 dark:border-gray-700">
                <th className="pb-3 pr-4 font-semibold">Nom</th>
                <th className="pb-3 pr-4 font-semibold">Nb Annotations</th>
                <th className="pb-3 pr-4 font-semibold">% Avancement</th>
                <th className="pb-3 font-semibold">Actions</th>
              </tr>
            </thead>
            <tbody>
              {datasets.map((ds) => (
                <tr key={ds.id} className="border-b border-gray-100 dark:border-gray-800">
                  <td className="py-3 pr-4">{ds.name}</td>
                  <td className="py-3 pr-4">{ds.totalAnnotations}</td>
                  <td className="py-3 pr-4">
                    <div className="flex items-center gap-2">
                      <div className="h-2 w-24 rounded-full bg-gray-200 dark:bg-gray-700">
                        <div
                          className="h-2 rounded-full bg-gradient-to-r from-green-400 to-green-500"
                          style={{ width: `${ds.completionPercentage}%` }}
                        />
                      </div>
                      <span className="text-xs">{ds.completionPercentage}%</span>
                    </div>
                  </td>
                  <td className="py-3">
                    <div className="flex gap-2">
                      <Link
                        to={`/admin/datasets/${ds.id}`}
                        className="rounded bg-blue-600 px-3 py-1 text-xs font-medium text-white hover:bg-blue-700 dark:bg-blue-500 dark:hover:bg-blue-600"
                      >
                        Détails
                      </Link>
                      <Link
                        to={`/admin/datasets/${ds.id}?assign=true`}
                        className="rounded bg-green-600 px-3 py-1 text-xs font-medium text-white hover:bg-green-700 dark:bg-green-500 dark:hover:bg-green-600"
                      >
                        Ajouter Annotateurs
                      </Link>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
