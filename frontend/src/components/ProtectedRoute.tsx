import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

export function ProtectedRoute() {
  const isAuthenticated = useAuthStore(s => s.isAuthenticated());
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
}

export function AdminRoute() {
  const isAdmin = useAuthStore(s => s.isAdmin());
  return isAdmin ? <Outlet /> : <Navigate to="/dashboard" replace />;
}
