import { useState, useEffect } from 'react';
import { getDashboardStats } from '../../api/adminApi';
import Spinner from '../../components/Spinner';
import { formatPercent, formatDecimal, formatTime } from '../../utils/formatters';

export default function AdminDashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getDashboardStats()
      .then((res) => {
        setData(res.data);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to load dashboard stats');
        setLoading(false);
      });
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Spinner size="lg" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-6 text-center">
          <p className="text-red-700 dark:text-red-400 font-medium">{error}</p>
          <button
            onClick={() => {
              setLoading(true);
              setError(null);
              getDashboardStats()
                .then((res) => {
                  setData(res.data);
                  setLoading(false);
                })
                .catch((err) => {
                  setError(err.response?.data?.message || 'Failed to load dashboard stats');
                  setLoading(false);
                });
            }}
            className="mt-3 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors text-sm"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  if (!data) return null;

  const {
    totalDatasets = 0,
    totalTexts = 0,
    totalAnnotators = 0,
    totalAnnotations = 0,
    overallAnnotationPercent = 0,
    globalClassDistribution = {},
    annotatorProgress = [],
    spammers = [],
  } = data;

  const classColors = [
    'bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-300',
    'bg-green-100 text-green-800 dark:bg-green-900/40 dark:text-green-300',
    'bg-purple-100 text-purple-800 dark:bg-purple-900/40 dark:text-purple-300',
    'bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300',
    'bg-pink-100 text-pink-800 dark:bg-pink-900/40 dark:text-pink-300',
    'bg-cyan-100 text-cyan-800 dark:bg-cyan-900/40 dark:text-cyan-300',
  ];

  const classEntries = Object.entries(globalClassDistribution);
  const totalClassCount = classEntries.reduce((sum, [, count]) => sum + count, 0);

  return (
    <div className="p-4 md:p-6 space-y-6">
      {spammers.length > 0 && (
        <div className="bg-red-50 dark:bg-red-900/30 border border-red-300 dark:border-red-700 rounded-lg p-4 flex items-start gap-3">
          <svg className="w-5 h-5 text-red-600 dark:text-red-400 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4.5c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
          <div>
            <p className="font-semibold text-red-700 dark:text-red-400">
              Spammer Alert
            </p>
            <p className="text-sm text-red-600 dark:text-red-300">
              The following annotators have been flagged as potential spammers:{' '}
              <span className="font-medium">
                {spammers.map((s) => `${s.firstName} ${s.lastName}`).join(', ')}
              </span>
            </p>
          </div>
        </div>
      )}

      <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Admin Dashboard</h1>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          icon={
            <svg className="w-6 h-6 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4" />
            </svg>
          }
          label="Total Datasets"
          value={totalDatasets}
          bgColor="bg-green-50 dark:bg-green-900/20"
        />
        <StatCard
          icon={
            <svg className="w-6 h-6 text-indigo-600 dark:text-indigo-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          }
          label="Total Texts"
          value={totalTexts}
          bgColor="bg-indigo-50 dark:bg-indigo-900/20"
        />
        <StatCard
          icon={
            <svg className="w-6 h-6 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
            </svg>
          }
          label="Total Annotators"
          value={totalAnnotators}
          bgColor="bg-green-50 dark:bg-green-900/20"
        />
        <StatCard
          icon={
            <svg className="w-6 h-6 text-amber-600 dark:text-amber-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
            </svg>
          }
          label="Total Annotations"
          value={totalAnnotations}
          bgColor="bg-amber-50 dark:bg-amber-900/20"
        />
        <StatCard
          icon={
            <svg className="w-6 h-6 text-purple-600 dark:text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
          }
          label="Overall Annotation %"
          value={formatPercent(overallAnnotationPercent)}
          bgColor="bg-purple-50 dark:bg-purple-900/20"
        />
      </div>

      <div className="bg-white dark:bg-dark-surface-alt border border-gray-200 dark:border-dark-border rounded-lg p-5">
        <h2 className="text-lg font-semibold text-gray-800 dark:text-gray-200 mb-4">
          Global Class Distribution
        </h2>
        {classEntries.length === 0 ? (
          <p className="text-gray-500 dark:text-gray-400 text-sm">No distribution data available.</p>
        ) : (
          <div className="space-y-3">
            {classEntries.map(([className, count], idx) => {
              const percentage = totalClassCount > 0 ? (count / totalClassCount) * 100 : 0;
              return (
                <div key={className}>
                  <div className="flex items-center justify-between mb-1">
                    <span className={`px-2 py-0.5 rounded text-xs font-medium ${classColors[idx % classColors.length]}`}>
                      {className}
                    </span>
                    <span className="text-sm text-gray-600 dark:text-gray-400">
                      {count} ({formatDecimal(percentage, 1)}%)
                    </span>
                  </div>
                  <div className="w-full bg-gray-100 dark:bg-dark-surface-hover rounded-full h-2.5">
                    <div
                      className="h-2.5 rounded-full bg-primary dark:bg-primary-400 transition-all"
                      style={{ width: `${percentage}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      <div className="bg-white dark:bg-dark-surface-alt border border-gray-200 dark:border-dark-border rounded-lg overflow-hidden">
        <div className="p-5 pb-3">
          <h2 className="text-lg font-semibold text-gray-800 dark:text-gray-200">
            Annotator Progress
          </h2>
        </div>
        {annotatorProgress.length === 0 ? (
          <div className="px-5 pb-5">
            <p className="text-gray-500 dark:text-gray-400 text-sm">No annotator data available.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-t border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-surface">
                  <th className="text-left px-4 py-3 font-medium text-gray-600 dark:text-gray-400">Annotator</th>
                  <th className="text-left px-4 py-3 font-medium text-gray-600 dark:text-gray-400">Annotated</th>
                  <th className="text-left px-4 py-3 font-medium text-gray-600 dark:text-gray-400">Avg Time (s)</th>
                  <th className="text-left px-4 py-3 font-medium text-gray-600 dark:text-gray-400">Class Distribution</th>
                </tr>
              </thead>
              <tbody>
                {annotatorProgress.map((annotator) => {
                  const distEntries = Object.entries(annotator.classDistribution || {});
                  return (
                    <tr key={annotator.annotatorId} className="border-t border-gray-200 dark:border-dark-border hover:bg-gray-50 dark:hover:bg-dark-surface-hover transition-colors">
                      <td className="px-4 py-3 font-medium text-gray-800 dark:text-gray-200">
                        {annotator.firstName} {annotator.lastName}
                      </td>
                      <td className="px-4 py-3 text-gray-600 dark:text-gray-400">{annotator.annotatedCount}</td>
                      <td className="px-4 py-3 text-gray-600 dark:text-gray-400">
                        {formatTime(annotator.avgTimePerAnnotation)}
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex flex-wrap gap-1">
                          {distEntries.map(([cls, cnt], i) => (
                            <span
                              key={cls}
                              className={`px-2 py-0.5 rounded text-xs font-medium ${classColors[i % classColors.length]}`}
                            >
                              {cls}: {cnt}
                            </span>
                          ))}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

function StatCard({ icon, label, value, bgColor }) {
  return (
    <div className="bg-white dark:bg-dark-surface-alt border border-gray-200 dark:border-dark-border rounded-lg p-4 flex items-center gap-4 min-w-0">
      <div className={`${bgColor} p-3 rounded-lg flex-shrink-0`}>
        {icon}
      </div>
      <div className="min-w-0">
        <p className="text-sm text-gray-500 dark:text-gray-400">{label}</p>
        <p className="text-2xl font-bold text-gray-900 dark:text-gray-100 truncate">{value}</p>
      </div>
    </div>
  );
}
