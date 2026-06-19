import { useState, useEffect, useRef } from 'react';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';
import { trainModel, testModel, getNlpLogs, downloadModel } from '../../api/adminApi';
import { useToast } from '../../components/Toast';
import Spinner from '../../components/Spinner';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler);

function FileUpload({ accept, label, onSelect, selectedFile }) {
  const inputRef = useRef(null);

  return (
    <div
      onClick={() => inputRef.current?.click()}
      className="border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg p-4 text-center cursor-pointer hover:border-green-400 dark:hover:border-green-500 transition-colors bg-gray-50 dark:bg-gray-700/50"
    >
      <input
        ref={inputRef}
        type="file"
        accept={accept}
        onChange={(e) => onSelect(e.target.files[0])}
        className="hidden"
      />
      {selectedFile ? (
        <div className="flex items-center justify-center gap-2 text-sm">
          <span className="text-green-600 dark:text-green-400">📄</span>
          <span className="font-medium text-gray-800 dark:text-gray-200 truncate max-w-[200px]">
            {selectedFile.name}
          </span>
          <button
            onClick={(e) => { e.stopPropagation(); onSelect(null); }}
            className="ml-1 text-gray-400 hover:text-red-500 dark:hover:text-red-400 text-lg leading-none"
          >
            &times;
          </button>
        </div>
      ) : (
        <p className="text-sm text-gray-500 dark:text-gray-400">
          {label}
        </p>
      )}
    </div>
  );
}

function TrainingChart({ metrics }) {
  if (!metrics || metrics.length === 0) return null;

  const hasLoss = metrics.some((m) => m.loss != null && m.loss !== '');
  const hasAccuracy = metrics.some((m) => m.accuracy != null && m.accuracy !== '');

  const datasets = [];
  if (hasLoss) {
    datasets.push({
      label: 'Loss',
      data: metrics.map((m) => m.loss),
      borderColor: '#ef4444',
      backgroundColor: 'rgba(239, 68, 68, 0.1)',
      yAxisID: 'y-loss',
      fill: true,
      tension: 0.3,
      pointRadius: 4,
    });
  }
  if (hasAccuracy) {
    datasets.push({
      label: 'Accuracy',
      data: metrics.map((m) => m.accuracy),
      borderColor: '#22c55e',
      backgroundColor: 'rgba(34, 197, 94, 0.1)',
      yAxisID: 'y-acc',
      fill: true,
      tension: 0.3,
      pointRadius: 4,
    });
  }
  if (datasets.length === 0) return null;

  const data = {
    labels: metrics.map((m) => `Epoch ${m.epoch}`),
    datasets,
  };

  const options = {
    responsive: true,
    interaction: { mode: 'index', intersect: false },
    animation: { duration: 300 },
    plugins: {
      legend: { position: 'bottom', labels: { color: '#9ca3af' } },
      tooltip: {
        callbacks: {
          label: (ctx) => {
            const val = ctx.parsed.y;
            return ctx.dataset.label === 'Accuracy'
              ? `${(val * 100).toFixed(2)}%`
              : val.toFixed(4);
          },
        },
      },
    },
    scales: {
      'y-loss': {
        type: 'linear',
        display: hasLoss,
        position: 'left',
        title: { display: true, text: 'Loss', color: '#9ca3af' },
        grid: { color: 'rgba(156, 163, 175, 0.1)' },
        ticks: { color: '#9ca3af' },
      },
      'y-acc': {
        type: 'linear',
        display: hasAccuracy,
        position: 'right',
        title: { display: true, text: 'Accuracy', color: '#9ca3af' },
        grid: { drawOnChartArea: false },
        ticks: { color: '#9ca3af', callback: (v) => `${(v * 100).toFixed(0)}%` },
        min: 0,
        max: 1,
      },
      x: {
        ticks: { color: '#9ca3af' },
        grid: { color: 'rgba(156, 163, 175, 0.1)' },
      },
    },
  };

  return (
    <div className="mt-4 p-3 bg-gray-50 dark:bg-gray-700/30 rounded-lg">
      <Line data={data} options={options} />
    </div>
  );
}

export default function NlpDashboard() {
  const { addToast } = useToast();

  const [hyperparamsOpen, setHyperparamsOpen] = useState(false);
  const [learningRate, setLearningRate] = useState(0.001);
  const [epochs, setEpochs] = useState(5);
  const [batchSize, setBatchSize] = useState(16);
  const [trainingStatus, setTrainingStatus] = useState('idle');
  const [trainFile, setTrainFile] = useState(null);
  const [configFile, setConfigFile] = useState(null);

  const [liveMetrics, setLiveMetrics] = useState([]);

  const [testStatus, setTestStatus] = useState('idle');
  const [testResults, setTestResults] = useState(null);
  const [testFile, setTestFile] = useState(null);
  const [modelFile, setModelFile] = useState(null);

  const [logs, setLogs] = useState([]);
  const [logsLoading, setLogsLoading] = useState(true);

  useEffect(() => {
    getNlpLogs()
      .then((res) => setLogs(res.data))
      .catch(() => addToast('Failed to load NLP logs', 'error'))
      .finally(() => setLogsLoading(false));
  }, []);

  useEffect(() => {
    const eventSource = new EventSource('/api/admin/nlp/metrics/stream');
    eventSource.addEventListener('metric', (e) => {
      try {
        const data = JSON.parse(e.data);
        setLiveMetrics((prev) => {
          const exists = prev.some((m) => m.epoch === data.epoch);
          return exists ? prev : [...prev, data];
        });
      } catch {}
    });
    eventSource.onerror = () => eventSource.close();
    return () => eventSource.close();
  }, []);

  const handleTrain = async () => {
    setTrainingStatus('training');
    try {
      const formData = new FormData();
      if (trainFile) formData.append('file', trainFile);
      if (configFile) formData.append('config', configFile);
      formData.append('learningRate', String(learningRate));
      formData.append('epochs', String(epochs));
      formData.append('batchSize', String(batchSize));
      const res = await trainModel(formData);
      setTrainingStatus('done');
      addToast('Training started successfully');
      const logsRes = await getNlpLogs();
      setLogs(logsRes.data);
    } catch {
      setTrainingStatus('error');
      addToast('Training failed', 'error');
    }
  };

  const handleDownloadModel = async (logId, filename) => {
    try {
      const res = await downloadModel(logId);
      const url = URL.createObjectURL(new Blob([res.data]));
      const a = document.createElement('a');
      a.href = url;
      a.download = filename || `model_${logId}.pt`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
      addToast('Modèle téléchargé');
    } catch {
      addToast('Échec du téléchargement du modèle', 'error');
    }
  };

  const handleTest = async () => {
    setTestStatus('testing');
    setTestResults(null);
    try {
      const formData = new FormData();
      if (testFile) formData.append('file', testFile);
      if (modelFile) formData.append('model', modelFile);
      const res = await testModel(formData);
      setTestResults(res.data);
      setTestStatus('done');
      addToast('Testing completed');
    } catch {
      setTestStatus('error');
      addToast('Testing failed', 'error');
    }
  };

  const formatDate = (d) => {
    if (!d) return '—';
    const date = new Date(d);
    if (isNaN(date.getTime())) return '—';
    return date.toLocaleDateString('fr-FR', {
      year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit',
    });
  };

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6 dark:text-white">NLP Dashboard</h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-5">
          <h2 className="text-lg font-semibold mb-4 dark:text-white">Train</h2>

          <div className="space-y-3 mb-4">
            <FileUpload
              accept=".py"
              label="Cliquez pour sélectionner train.py"
              onSelect={setTrainFile}
              selectedFile={trainFile}
            />
            <FileUpload
              accept=".txt,.json,.yml,.yaml,.cfg"
              label="(Optionnel) requirements.txt / config"
              onSelect={setConfigFile}
              selectedFile={configFile}
            />
          </div>

          <button
            onClick={() => setHyperparamsOpen(!hyperparamsOpen)}
            className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-300 mb-3 hover:text-gray-900 dark:hover:text-white"
          >
            <span className={`transition-transform ${hyperparamsOpen ? 'rotate-90' : ''}`}>&#9654;</span>
            Hyperparamètres
          </button>

          {hyperparamsOpen && (
            <div className="space-y-3 mb-4 pl-4 border-l-2 border-green-200 dark:border-green-700">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Learning Rate</label>
                <input
                  type="number"
                  step="0.0001"
                  value={learningRate}
                  onChange={(e) => setLearningRate(Number(e.target.value))}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-sm bg-white dark:bg-gray-700 dark:text-white focus:outline-none focus:ring-2 focus:ring-green-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Epochs</label>
                <input
                  type="number"
                  min="1"
                  value={epochs}
                  onChange={(e) => setEpochs(Number(e.target.value))}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-sm bg-white dark:bg-gray-700 dark:text-white focus:outline-none focus:ring-2 focus:ring-green-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Batch Size</label>
                <input
                  type="number"
                  min="1"
                  value={batchSize}
                  onChange={(e) => setBatchSize(Number(e.target.value))}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-sm bg-white dark:bg-gray-700 dark:text-white focus:outline-none focus:ring-2 focus:ring-green-500"
                />
              </div>
            </div>
          )}

          <button
            onClick={handleTrain}
            disabled={trainingStatus === 'training'}
            className="w-full px-4 py-2 bg-green-600 text-white rounded-lg text-sm font-medium hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {trainingStatus === 'training' ? 'Training...' : 'Train Model'}
          </button>

          {trainingStatus === 'training' && (
            <div className="mt-3 flex items-center gap-2 text-sm text-green-600 dark:text-green-400">
              <Spinner size="sm" /> Training in progress...
            </div>
          )}

          {trainingStatus === 'done' && (
            <p className="mt-3 text-sm text-green-600 dark:text-green-400">Training completed successfully.</p>
          )}

          {trainingStatus === 'error' && (
            <p className="mt-3 text-sm text-red-600 dark:text-red-400">Training failed. Check logs for details.</p>
          )}

          {liveMetrics.length > 0 && (
            <TrainingChart metrics={liveMetrics} />
          )}
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-5">
          <h2 className="text-lg font-semibold mb-4 dark:text-white">Test</h2>

          <div className="space-y-3 mb-4">
            <FileUpload
              accept=".py"
              label="Cliquez pour sélectionner test.py"
              onSelect={setTestFile}
              selectedFile={testFile}
            />
            <FileUpload
              accept=".pt,.pkl,.bin,.onnx,.h5"
              label="(Optionnel) Modèle pré-entraîné"
              onSelect={setModelFile}
              selectedFile={modelFile}
            />
          </div>

          <button
            onClick={handleTest}
            disabled={testStatus === 'testing'}
            className="w-full px-4 py-2 bg-green-600 text-white rounded-lg text-sm font-medium hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {testStatus === 'testing' ? 'Testing...' : 'Test Model'}
          </button>

          {testStatus === 'testing' && (
            <div className="mt-3 flex items-center gap-2 text-sm text-green-600 dark:text-green-400">
              <Spinner size="sm" /> Running evaluation...
            </div>
          )}

          {testResults && (
            <div className="mt-4 space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div className="bg-gray-50 dark:bg-gray-700 rounded-lg p-3 text-center">
                  <p className="text-xs text-gray-500 dark:text-gray-400 uppercase tracking-wide">Accuracy</p>
                  <p className="text-xl font-bold text-gray-900 dark:text-white">
                    {testResults.accuracy != null ? `${(testResults.accuracy * 100).toFixed(2)}%` : '-'}
                  </p>
                </div>
                <div className="bg-gray-50 dark:bg-gray-700 rounded-lg p-3 text-center">
                  <p className="text-xs text-gray-500 dark:text-gray-400 uppercase tracking-wide">F1-Score</p>
                  <p className="text-xl font-bold text-gray-900 dark:text-white">
                    {testResults.f1Score != null ? `${(testResults.f1Score * 100).toFixed(2)}%` : '-'}
                  </p>
                </div>
              </div>

              {testResults.confusionMatrix && (
                <div>
                  <p className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Confusion Matrix</p>
                  <div className="inline-block border border-gray-300 dark:border-gray-600 rounded-lg overflow-hidden">
                    {testResults.confusionMatrix.map((row, i) => (
                      <div key={i} className="flex">
                        {row.map((cell, j) => (
                          <div
                            key={j}
                            className="w-16 h-12 flex items-center justify-center text-sm font-semibold text-gray-900 dark:text-white border-r border-b border-gray-300 dark:border-gray-600 last:border-r-0 bg-white dark:bg-gray-800"
                          >
                            {cell}
                          </div>
                        ))}
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {testStatus === 'error' && (
            <p className="mt-3 text-sm text-red-600 dark:text-red-400">Testing failed.</p>
          )}
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-5">
          <h2 className="text-lg font-semibold mb-4 dark:text-white">History</h2>

          {logsLoading ? (
            <Spinner size="sm" />
          ) : logs.length === 0 ? (
            <p className="text-sm text-gray-500 dark:text-gray-400">No training history yet.</p>
          ) : (
            <>
              <div className="overflow-x-auto -mx-5">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-gray-200 dark:border-gray-700">
                      <th className="text-left px-4 py-2 font-medium text-gray-500 dark:text-gray-400">Date</th>
                      <th className="text-left px-4 py-2 font-medium text-gray-500 dark:text-gray-400">Status</th>
                      <th className="text-right px-4 py-2 font-medium text-gray-500 dark:text-gray-400">Acc.</th>
                      <th className="text-right px-4 py-2 font-medium text-gray-500 dark:text-gray-400">F1</th>
                      <th className="text-right px-4 py-2 font-medium text-gray-500 dark:text-gray-400">Modèle</th>
                    </tr>
                  </thead>
                  <tbody>
                    {logs.map((log) => (
                      <tr key={log.id} className="border-b border-gray-100 dark:border-gray-700 last:border-0">
                        <td className="px-4 py-2 text-gray-700 dark:text-gray-300 whitespace-nowrap">{formatDate(log.completedAt || log.startedAt)}</td>
                        <td className="px-4 py-2">
                          <span className={`inline-block px-2 py-0.5 rounded text-xs font-medium ${
                            log.status === 'SUCCESS' ? 'bg-green-100 text-green-700 dark:bg-green-900 dark:text-green-300' :
                            log.status === 'FAILED' ? 'bg-red-100 text-red-700 dark:bg-red-900 dark:text-red-300' :
                            'bg-yellow-100 text-yellow-700 dark:bg-yellow-900 dark:text-yellow-300'
                          }`}>
                            {log.status}
                          </span>
                        </td>
                        <td className="px-4 py-2 text-right text-gray-700 dark:text-gray-300">{log.accuracy != null ? `${(log.accuracy * 100).toFixed(1)}%` : '-'}</td>
                        <td className="px-4 py-2 text-right text-gray-700 dark:text-gray-300">{log.f1Score != null ? `${(log.f1Score * 100).toFixed(1)}%` : '-'}</td>
                        <td className="px-4 py-2 text-right">
                          {log.status === 'SUCCESS' && (
                            <button
                              onClick={() => handleDownloadModel(log.id, `model_${log.id}.pt`)}
                              className="text-xs text-green-600 hover:text-green-800 dark:text-green-400 dark:hover:text-green-300 underline"
                            >
                              Télécharger
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="mt-4">
                {logs.filter((l) => l.accuracy != null).length > 0 && (
                  <TrainingChart
                    metrics={logs
                      .filter((l) => l.accuracy != null)
                      .map((l, i) => ({ epoch: i + 1, loss: l.loss, accuracy: l.accuracy }))}
                  />
                )}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
