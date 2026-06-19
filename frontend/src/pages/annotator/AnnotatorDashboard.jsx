import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getTasks } from '../../api/annotatorApi';
import Spinner from '../../components/Spinner';

export default function AnnotatorDashboard() {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getTasks()
      .then((res) => setTasks(res.data.content ?? res.data ?? []))
      .catch(() => setTasks([]))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[60vh]">
        <Spinner size="lg" />
      </div>
    );
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6 dark:text-white">Mes Tâches</h1>

      {tasks.length === 0 ? (
        <p className="text-gray-500 dark:text-gray-400">Aucune tâche assignée</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-gray-100 dark:bg-gray-800 text-left">
                <th className="p-3 font-semibold text-sm dark:text-gray-200">Id</th>
                <th className="p-3 font-semibold text-sm dark:text-gray-200">Nom dataset</th>
                <th className="p-3 font-semibold text-sm dark:text-gray-200">Date limite</th>
                <th className="p-3 font-semibold text-sm dark:text-gray-200">% Avancement</th>
                <th className="p-3 font-semibold text-sm dark:text-gray-200">Taille</th>
                <th className="p-3 font-semibold text-sm dark:text-gray-200">Actions</th>
              </tr>
            </thead>
            <tbody>
              {tasks.map((task) => (
                <tr key={task.id} className="border-b border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800/50">
                  <td className="p-3 text-sm dark:text-gray-300">{task.id}</td>
                  <td className="p-3 text-sm dark:text-gray-300">{task.datasetName}</td>
                  <td className="p-3 text-sm dark:text-gray-300">{task.assignedAt ? new Date(task.assignedAt).toLocaleDateString() : '-'}</td>
                  <td className="p-3">
                    <div className="flex items-center gap-2">
                      <div className="flex-1 h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                        <div
                          className="h-full bg-primary rounded-full transition-all duration-300"
                          style={{ width: `${task.completionPercentage}%` }}
                        />
                      </div>
                      <span className="text-sm font-medium dark:text-gray-300">{task.completionPercentage}%</span>
                    </div>
                  </td>
                  <td className="p-3 text-sm dark:text-gray-300">{task.totalItems}</td>
                  <td className="p-3">
                    <Link
                      to={`/annotator/tasks/${task.id}`}
                      state={{ resumePage: task.annotatedItems, totalItems: task.totalItems }}
                      className="inline-block px-4 py-1.5 text-sm font-medium text-white bg-primary rounded hover:bg-primary/90 transition-colors"
                    >
                      Travailler
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div className="mt-6">
        <Link
          to="/annotator/stats"
          className="inline-block px-4 py-2 text-sm font-medium text-primary border border-primary rounded hover:bg-primary hover:text-white transition-colors"
        >
          Mes Statistiques
        </Link>
      </div>
    </div>
  );
}
