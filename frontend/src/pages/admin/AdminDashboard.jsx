import { useEffect, useState } from 'react';
import api from '../../api/axios';

export default function AdminDashboard() {
  const [status, setStatus] = useState('checking...');

  useEffect(() => {
    api.get('/api/admin/status')
      .then(() => setStatus('connected'))
      .catch(() => setStatus('disconnected'));
  }, []);

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Admin Dashboard</h1>
      <p>Welcome to the admin panel. Use the sidebar to manage annotators, datasets, and NLP training.</p>
      <p className="mt-4">API status: <span className={status === 'connected' ? 'text-green-600' : 'text-red-600'}>{status}</span></p>
    </div>
  );
}
