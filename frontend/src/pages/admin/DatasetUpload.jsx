import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { uploadDataset } from '../../api/adminApi';
import { useToast } from '../../components/Toast';
import Spinner from '../../components/Spinner';

export default function DatasetUpload() {
  const [file, setFile] = useState(null);
  const [name, setName] = useState('');
  const [classes, setClasses] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { addToast } = useToast();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const formData = new FormData();
      formData.append('file', file);
      const tags = classes.split(';').map((s) => s.trim()).filter(Boolean);
      formData.append('tags', String(tags));
      formData.append('name', name);
      formData.append('description', description);
      await uploadDataset(formData);
      addToast('Dataset uploaded successfully', 'success');
      navigate('/admin/datasets');
    } catch (err) {
      const msg = err.response?.data?.error || err.message || 'Upload failed';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-dark-text">Upload Dataset</h1>
      </div>

      {error && (
        <div className="mb-4 p-3 rounded-lg bg-red-100 dark:bg-red-900/30 border border-red-300 dark:border-red-700 text-red-700 dark:text-red-300 text-sm">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="bg-white dark:bg-dark-surface-alt rounded-lg shadow p-6 space-y-5 max-w-2xl">
        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Fichier
          </label>
          <input
            type="file"
            accept=".csv,.json"
            onChange={(e) => setFile(e.target.files[0])}
            className="w-full text-sm text-gray-600 dark:text-gray-400 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-medium file:bg-primary-50 file:text-primary dark:file:bg-primary-900/30 dark:file:text-primary-300 hover:file:bg-primary-100 dark:hover:file:bg-primary-900/50 cursor-pointer"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Nom
          </label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full border border-gray-300 dark:border-dark-border rounded px-3 py-2 bg-white dark:bg-dark-surface text-gray-900 dark:text-dark-text focus:outline-none focus:ring-2 focus:ring-primary"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Classes
          </label>
          <input
            type="text"
            value={classes}
            onChange={(e) => setClasses(e.target.value)}
            placeholder="N;E;C or Similar;Not Similar"
            className="w-full border border-gray-300 dark:border-dark-border rounded px-3 py-2 bg-white dark:bg-dark-surface text-gray-900 dark:text-dark-text focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Description
          </label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={4}
            className="w-full border border-gray-300 dark:border-dark-border rounded px-3 py-2 bg-white dark:bg-dark-surface text-gray-900 dark:text-dark-text focus:outline-none focus:ring-2 focus:ring-primary resize-y"
          />
        </div>

        <div className="flex items-center gap-3 pt-2">
          <button
            type="submit"
            disabled={loading || !file || !name}
            className="px-5 py-2.5 bg-primary text-white rounded-lg hover:bg-primary-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-sm font-medium flex items-center gap-2"
          >
            {loading && <Spinner size="sm" />}
            Créer
          </button>
          <Link
            to="/admin/datasets"
            className="px-5 py-2.5 text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200 border border-gray-300 dark:border-dark-border rounded-lg hover:bg-gray-50 dark:hover:bg-dark-surface-hover transition-colors text-sm font-medium"
          >
            Annuler
          </Link>
        </div>
      </form>
    </div>
  );
}
