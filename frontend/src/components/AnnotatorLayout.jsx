import { Outlet, useNavigate, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import DarkModeToggle from './DarkModeToggle';
import ErrorBoundary from './ErrorBoundary';

export default function AnnotatorLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-dark-surface">
      <header className="bg-white dark:bg-dark-surface-alt border-b border-gray-200 dark:border-dark-border sticky top-0 z-30">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center gap-6">
              <Link to="/annotator" className="text-lg font-bold text-primary">NLP Platform</Link>
              <nav className="hidden sm:flex items-center gap-1">
                <Link
                  to="/annotator"
                  className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                    location.pathname === '/annotator'
                      ? 'bg-primary-50 text-primary dark:bg-primary-900/30 dark:text-primary-300'
                      : 'text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-dark-surface-hover'
                  }`}
                >
                  Mes Tâches
                </Link>
                <Link
                  to="/annotator/stats"
                  className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                    location.pathname === '/annotator/stats'
                      ? 'bg-primary-50 text-primary dark:bg-primary-900/30 dark:text-primary-300'
                      : 'text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-dark-surface-hover'
                  }`}
                >
                  Mes Statistiques
                </Link>
              </nav>
            </div>

            <div className="flex items-center gap-3">
              <DarkModeToggle />
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-full bg-primary-100 dark:bg-primary-900/50 flex items-center justify-center text-primary text-sm font-semibold">
                  {user?.charAt(0).toUpperCase()}
                </div>
                <span className="hidden sm:block text-sm font-medium text-gray-700 dark:text-gray-300">{user}</span>
              </div>
              <button
                onClick={handleLogout}
                className="text-sm text-red-600 hover:text-red-700 dark:text-red-400 dark:hover:text-red-300 font-medium transition-colors"
              >
                Déconnexion
              </button>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 lg:py-8">
        <ErrorBoundary>
          <Outlet />
        </ErrorBoundary>
      </main>
    </div>
  );
}
