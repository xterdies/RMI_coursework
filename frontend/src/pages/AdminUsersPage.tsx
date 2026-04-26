import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '../api/services';
import { Button, Card, PageHeader, Badge, Spinner } from '../components/ui';
import { useUsersTableStore } from '../store/tableStore';
import { Trash2 } from 'lucide-react';

type UserDto = {
  id: number;
  email: string;
  fullName: string;
  role: string;
  enabled: boolean;
  createdAt: string;
};

type PagedResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
};

export default function AdminUsersPage() {
  const qc = useQueryClient();
  const { page, size, filterText, sortField, sortOrder, setPage, setFilterText, setSort } = useUsersTableStore();

  const { data, isLoading } = useQuery({
    queryKey: ['admin-users', page, size, filterText, sortField, sortOrder],
    queryFn: () => adminApi.listUsers(page, size, {
      filter: filterText ? `email:like:${filterText}` : undefined,
      sort: `${sortField},${sortOrder}`,
    }) as Promise<PagedResponse<UserDto>>,
  });

  const toggleMutation = useMutation({
    mutationFn: adminApi.toggleUser,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-users'] }),
  });

  const deleteMutation = useMutation({
    mutationFn: adminApi.deleteUser,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-users'] }),
  });

  if (isLoading) return <div className="flex justify-center p-12"><Spinner /></div>;

  return (
    <div>
      <PageHeader title="User Management" subtitle={`${data?.totalElements ?? 0} users`} />
      <Card>
        <div className="flex flex-col sm:flex-row gap-3 mb-4">
          <input
            className="input w-full sm:w-72"
            placeholder="Filter by email…"
            value={filterText}
            onChange={(e) => setFilterText(e.target.value)}
          />
          <select className="input sm:w-56" value={`${sortField},${sortOrder}`}
            onChange={(e) => {
              const [f, o] = e.target.value.split(',');
              setSort(f, (o as any) ?? 'desc');
            }}>
            <option value="createdAt,desc">Newest</option>
            <option value="createdAt,asc">Oldest</option>
            <option value="email,asc">Email (A→Z)</option>
            <option value="email,desc">Email (Z→A)</option>
          </select>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b text-left text-gray-500">
                <th className="pb-3 font-medium">Email</th>
                <th className="pb-3 font-medium">Name</th>
                <th className="pb-3 font-medium">Role</th>
                <th className="pb-3 font-medium">Status</th>
                <th className="pb-3 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody>
              {data?.content.map((u) => (
                <tr key={u.id} className="border-b last:border-0 hover:bg-gray-50">
                  <td className="py-3 font-medium">{u.email}</td>
                  <td className="py-3 text-gray-500">{u.fullName}</td>
                  <td className="py-3"><Badge label={u.role} color={u.role === 'ADMIN' ? 'yellow' : 'blue'} /></td>
                  <td className="py-3">
                    <Badge label={u.enabled ? 'Enabled' : 'Disabled'} color={u.enabled ? 'green' : 'red'} />
                  </td>
                  <td className="py-3 flex gap-2">
                    <Button
                      size="sm"
                      variant="secondary"
                      onClick={() => toggleMutation.mutate(u.id)}
                      loading={toggleMutation.isPending}
                    >
                      Toggle
                    </Button>
                    <Button
                      size="sm"
                      variant="danger"
                      onClick={() => deleteMutation.mutate(u.id)}
                      loading={deleteMutation.isPending}
                    >
                      <Trash2 className="h-3 w-3" />
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {data && data.totalPages > 1 && (
          <div className="flex justify-between items-center mt-4 pt-4 border-t">
            <Button variant="secondary" size="sm" disabled={page === 0} onClick={() => setPage(page - 1)}>
              Previous
            </Button>
            <span className="text-sm text-gray-500">Page {page + 1} of {data.totalPages}</span>
            <Button variant="secondary" size="sm" disabled={data.last} onClick={() => setPage(page + 1)}>
              Next
            </Button>
          </div>
        )}
      </Card>
    </div>
  );
}

