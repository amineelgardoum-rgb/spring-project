import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export default function AnnotatorLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="flex h-screen">
      <aside className="w-64 bg-blue-900 text-white p-4 flex flex-col">
        <h1 className="text-xl font-bold mb-6">Annotation Platform</h1>
        <nav className="flex flex-col gap-2 flex-1">
          <Link to="/annotator" className="px-3 py-2 rounded hover:bg-blue-700">My Tasks</Link>
          <Link to="/annotator/stats" className="px-3 py-2 rounded hover:bg-blue-700">My Stats</Link>
        </nav>
        <div className="border-t border-blue-700 pt-4">
          <p className="text-sm text-blue-300">{user}</p>
          <button onClick={handleLogout} className="text-sm text-red-400 hover:text-red-300 mt-1">Logout</button>
        </div>
      </aside>
      <main className="flex-1 p-6 bg-gray-100 overflow-auto">
        <Outlet />
      </main>
    </div>
  );
}
