import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import AdminLayout from './components/AdminLayout';
import AnnotatorLayout from './components/AnnotatorLayout';
import Login from './pages/Login';
import NotFound from './pages/NotFound';
import AdminDashboard from './pages/admin/AdminDashboard';
import AnnotatorManagement from './pages/admin/AnnotatorManagement';
import DatasetsList from './pages/admin/DatasetsList';
import DatasetUpload from './pages/admin/DatasetUpload';
import DatasetDetail from './pages/admin/DatasetDetail';
import NlpDashboard from './pages/admin/NlpDashboard';
import AnnotatorDashboard from './pages/annotator/AnnotatorDashboard';
import AnnotationWorkspace from './pages/annotator/AnnotationWorkspace';
import AnnotatorStats from './pages/annotator/AnnotatorStats';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/admin" element={<ProtectedRoute allowedRole="ADMIN"><AdminLayout /></ProtectedRoute>}>
            <Route index element={<AdminDashboard />} />
            <Route path="users" element={<AnnotatorManagement />} />
            <Route path="datasets" element={<DatasetsList />} />
            <Route path="datasets/new" element={<DatasetUpload />} />
            <Route path="datasets/:id" element={<DatasetDetail />} />
            <Route path="nlp" element={<NlpDashboard />} />
          </Route>
          <Route path="/annotator" element={<ProtectedRoute allowedRole="ANNOTATOR"><AnnotatorLayout /></ProtectedRoute>}>
            <Route index element={<AnnotatorDashboard />} />
            <Route path="tasks/:taskId" element={<AnnotationWorkspace />} />
            <Route path="stats" element={<AnnotatorStats />} />
          </Route>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
