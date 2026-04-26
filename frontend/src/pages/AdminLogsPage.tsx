import { useQuery } from '@tanstack/react-query';
import { adminApi } from '../api/services';
import { Button, Card, PageHeader, Spinner } from '../components/ui';
import { useLogsTableStore } from '../store/tableStore';

type PagedResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
};

export default function AdminLogsPage() {
  const { page, size, setPage } = useLogsTableStore();

  const { data, isLoading } = useQuery({
    queryKey: ['admin-logs', page, size],
    queryFn: () => adminApi.getLogs(page, size) as Promise<PagedResponse<any>>,
  });

  if (isLoading) return <div className="flex justify-center p-12"><Spinner /></div>;

  return (
    <div>
      <PageHeader title="Audit Logs" subtitle={`${data?.totalElements ?? 0} events`} />
      <Card>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b text-left text-gray-500">
                <th className="pb-3 font-medium">Time</th>
                <th className="pb-3 font-medium">Action</th>
                <th className="pb-3 font-medium">Entity</th>
                <th className="pb-3 font-medium">Status</th>
              </tr>
            </thead>
            <tbody>
              {data?.content.map((l: any) => (
                <tr key={l.id} className="border-b last:border-0 hover:bg-gray-50">
                  <td className="py-3 text-gray-500">{l.createdAt ? new Date(l.createdAt).toLocaleString() : '—'}</td>
                  <td className="py-3 font-medium">{l.action}</td>
                  <td className="py-3 text-gray-500">{l.entityType}#{l.entityId ?? '—'}</td>
                  <td className="py-3 text-gray-500">{l.status}</td>
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

