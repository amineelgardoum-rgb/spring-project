import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="flex h-screen">
      <aside className="w-64 bg-gray-900 text-white p-4 flex flex-col">
        <h1 className="text-xl font-bold mb-6">Admin Panel</h1>
        <nav className="flex flex-col gap-2 flex-1">
          <Link to="/admin" className="px-3 py-2 rounded hover:bg-gray-700">Dashboard</Link>
          <Link to="/admin/users" className="px-3 py-2 rounded hover:bg-gray-700">Annotators</Link>
          <Link to="/admin/datasets" className="px-3 py-2 rounded hover:bg-gray-700">Datasets</Link>
          <Link to="/admin/nlp" className="px-3 py-2 rounded hover:bg-gray-700">NLP Training</Link>
        </nav>
        <div className="border-t border-gray-700 pt-4">
          <p className="text-sm text-gray-400">{user}</p>
          <button onClick={handleLogout} className="text-sm text-red-400 hover:text-red-300 mt-1">Logout</button>
        </div>
      </aside>
      <main className="flex-1 p-6 bg-gray-100 overflow-auto">
        <Outlet />
      </main>
    </div>
  );
}
