import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { indicatorsApi } from '../api/services';
import { Button, Card, PageHeader, Badge, Spinner } from '../components/ui';
import { useAuthStore } from '../store/authStore';
import { Trash2 } from 'lucide-react';

export default function IndicatorsPage() {
  const isAdmin = useAuthStore(s => s.isAdmin());
  const qc = useQueryClient();

  const { data: indicators, isLoading } = useQuery({
    queryKey: ['indicators'],
    queryFn: indicatorsApi.list,
  });

  const deleteMutation = useMutation({
    mutationFn: indicatorsApi.delete,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['indicators'] }),
  });

  if (isLoading) return <div className="flex justify-center p-12"><Spinner /></div>;

  return (
    <div>
      <PageHeader title="Economic Indicators" subtitle={`${indicators?.length ?? 0} indicators`} />
      <Card>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b text-left text-gray-500">
                <th className="pb-3 font-medium">Code</th>
                <th className="pb-3 font-medium">Name</th>
                <th className="pb-3 font-medium">Unit</th>
                <th className="pb-3 font-medium">Source</th>
                <th className="pb-3 font-medium">World Bank Code</th>
                {isAdmin && <th className="pb-3 font-medium">Actions</th>}
              </tr>
            </thead>
            <tbody>
              {indicators?.map(ind => (
                <tr key={ind.id} className="border-b last:border-0 hover:bg-gray-50">
                  <td className="py-3"><Badge label={ind.code} color="green" /></td>
                  <td className="py-3 font-medium">{ind.name}</td>
                  <td className="py-3 text-gray-500">{ind.unit ?? '—'}</td>
                  <td className="py-3 text-gray-500">{ind.source ?? '—'}</td>
                  <td className="py-3 text-gray-400 font-mono text-xs">{ind.worldBankCode ?? '—'}</td>
                  {isAdmin && (
                    <td className="py-3">
                      <Button variant="danger" size="sm"
                        onClick={() => deleteMutation.mutate(ind.id)}
                        loading={deleteMutation.isPending}>
                        <Trash2 className="h-3 w-3" />
                      </Button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
}
