import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { authApi } from '../api/services';
import { LayoutDashboard, MapPin, BarChart2, GitBranch, TrendingUp, Users, LogOut, FileText } from 'lucide-react';
import { clsx } from 'clsx';

const navItems = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/regions', label: 'Regions', icon: MapPin },
  { to: '/indicators', label: 'Indicators', icon: BarChart2 },
  { to: '/clustering', label: 'Clustering', icon: GitBranch },
  { to: '/trends', label: 'Trends', icon: TrendingUp },
];

const adminItems = [
  { to: '/admin/users', label: 'Users', icon: Users },
  { to: '/admin/logs', label: 'Audit Logs', icon: FileText },
];

export default function AppLayout() {
  const { user, clearAuth, isAdmin } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await authApi.logout().catch(() => {});
    clearAuth();
    navigate('/login');
  };

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Sidebar */}
      <aside className="w-64 bg-white border-r border-gray-200 flex flex-col">
        <div className="p-6 border-b">
          <h1 className="font-bold text-gray-900 text-sm leading-tight">Regional Clustering Platform</h1>
        </div>

        <nav className="flex-1 p-4 space-y-1">
          {navItems.map(({ to, label, icon: Icon }) => (
            <NavLink key={to} to={to}
              className={({ isActive }) => clsx(
                'flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors',
                isActive ? 'bg-primary-50 text-primary-700 font-medium' : 'text-gray-600 hover:bg-gray-100'
              )}>
              <Icon className="h-4 w-4" />
              {label}
            </NavLink>
          ))}

          {isAdmin() && (
            <>
              <div className="pt-4 pb-2">
                <p className="text-xs font-medium text-gray-400 uppercase tracking-wider px-3">Admin</p>
              </div>
              {adminItems.map(({ to, label, icon: Icon }) => (
                <NavLink key={to} to={to}
                  className={({ isActive }) => clsx(
                    'flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors',
                    isActive ? 'bg-primary-50 text-primary-700 font-medium' : 'text-gray-600 hover:bg-gray-100'
                  )}>
                  <Icon className="h-4 w-4" />
                  {label}
                </NavLink>
              ))}
            </>
          )}
        </nav>

        <div className="p-4 border-t">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-8 h-8 rounded-full bg-primary-100 flex items-center justify-center text-primary-700 font-medium text-sm">
              {user?.fullName?.[0]?.toUpperCase()}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-gray-900 truncate">{user?.fullName}</p>
              <p className="text-xs text-gray-500 truncate">{user?.role}</p>
            </div>
          </div>
          <button onClick={handleLogout}
            className="flex items-center gap-2 text-sm text-gray-500 hover:text-gray-700 w-full">
            <LogOut className="h-4 w-4" /> Sign out
          </button>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 overflow-auto">
        <div className="p-8">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
