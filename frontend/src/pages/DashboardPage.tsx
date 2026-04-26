import { useQuery } from '@tanstack/react-query';
import { regionsApi, indicatorsApi, clusteringApi } from '../api/services';
import { Card, PageHeader } from '../components/ui';
import { MapPin, BarChart2, GitBranch, TrendingUp } from 'lucide-react';

export default function DashboardPage() {
  const { data: regions } = useQuery({ queryKey: ['regions'], queryFn: () => regionsApi.list(0, 1) });
  const { data: indicators } = useQuery({ queryKey: ['indicators'], queryFn: indicatorsApi.list });
  const { data: runs } = useQuery({ queryKey: ['clustering'], queryFn: () => clusteringApi.list(0, 1) });

  const stats = [
    { label: 'Regions', value: regions?.totalElements ?? '—', icon: MapPin, color: 'text-blue-600 bg-blue-50' },
    { label: 'Indicators', value: indicators?.length ?? '—', icon: BarChart2, color: 'text-green-600 bg-green-50' },
    { label: 'Clustering Runs', value: runs?.totalElements ?? '—', icon: GitBranch, color: 'text-purple-600 bg-purple-50' },
    { label: 'Trend Models', value: '—', icon: TrendingUp, color: 'text-orange-600 bg-orange-50' },
  ];

  return (
    <div>
      <PageHeader title="Dashboard" subtitle="Overview of the Regional Clustering Platform" />
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {stats.map(({ label, value, icon: Icon, color }) => (
          <Card key={label}>
            <div className="flex items-center gap-4">
              <div className={`p-3 rounded-lg ${color}`}>
                <Icon className="h-6 w-6" />
              </div>
              <div>
                <p className="text-sm text-gray-500">{label}</p>
                <p className="text-2xl font-bold text-gray-900">{value}</p>
              </div>
            </div>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <h2 className="text-lg font-semibold mb-4">Recent Clustering Runs</h2>
          {runs?.content.length === 0 && (
            <p className="text-gray-500 text-sm">No clustering runs yet. Start by running K-Means analysis.</p>
          )}
          {runs?.content.map(run => (
            <div key={run.id} className="flex items-center justify-between py-2 border-b last:border-0">
              <div>
                <p className="font-medium text-sm">{run.name}</p>
                <p className="text-xs text-gray-500">Year: {run.year} | K={run.kClusters}</p>
              </div>
              <span className="text-xs text-gray-400">{new Date(run.createdAt).toLocaleDateString()}</span>
            </div>
          ))}
        </Card>

        <Card>
          <h2 className="text-lg font-semibold mb-4">Quick Actions</h2>
          <div className="space-y-3">
            {[
              { label: 'Run K-Means Clustering', href: '/clustering/new', color: 'bg-blue-50 text-blue-700' },
              { label: 'Compute Trend Forecast', href: '/trends', color: 'bg-green-50 text-green-700' },
              { label: 'Manage Regions', href: '/regions', color: 'bg-purple-50 text-purple-700' },
              { label: 'View Indicators', href: '/indicators', color: 'bg-orange-50 text-orange-700' },
            ].map(({ label, href, color }) => (
              <a key={href} href={href}
                className={`block p-3 rounded-lg text-sm font-medium ${color} hover:opacity-80 transition-opacity`}>
                {label}
              </a>
            ))}
          </div>
        </Card>
      </div>
    </div>
  );
}
