import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { apiClient } from '../api/client';
import { Spinner } from '../components/ui';

export default function OAuth2CallbackPage() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const setAuth = useAuthStore(s => s.setAuth);

  useEffect(() => {
    const token = params.get('token');
    if (!token) { navigate('/login'); return; }

    // Decode user from JWT payload
    try {
      apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      // Fetch current user info
      apiClient.get('/auth/me').then(res => {
        setAuth(res.data, token, '');
        navigate('/dashboard');
      }).catch(() => navigate('/login'));
    } catch {
      navigate('/login');
    }
  }, []);

  return (
    <div className="min-h-screen flex items-center justify-center">
      <Spinner className="h-8 w-8 text-primary-600" />
    </div>
  );
}
