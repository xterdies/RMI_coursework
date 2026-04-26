import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import AppLayout from './components/AppLayout';
import { ProtectedRoute, AdminRoute } from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import OAuth2CallbackPage from './pages/OAuth2CallbackPage';
import DashboardPage from './pages/DashboardPage';
import RegionsPage from './pages/RegionsPage';
import IndicatorsPage from './pages/IndicatorsPage';
import ClusteringPage from './pages/ClusteringPage';
import TrendsPage from './pages/TrendsPage';

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, staleTime: 30_000 } },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />

          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/regions" element={<RegionsPage />} />
              <Route path="/indicators" element={<IndicatorsPage />} />
              <Route path="/clustering" element={<ClusteringPage />} />
              <Route path="/trends" element={<TrendsPage />} />

              <Route element={<AdminRoute />}>
                <Route path="/admin/users" element={<div className="card"><h1 className="text-xl font-bold">User Management</h1><p className="text-gray-500 mt-2">Admin user management panel</p></div>} />
                <Route path="/admin/logs" element={<div className="card"><h1 className="text-xl font-bold">Audit Logs</h1><p className="text-gray-500 mt-2">System audit log viewer</p></div>} />
              </Route>
            </Route>
          </Route>

          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
}
