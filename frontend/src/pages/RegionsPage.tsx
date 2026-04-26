import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { regionsApi } from '../api/services';
import { Button, Card, PageHeader, Badge, Spinner } from '../components/ui';
import { useAuthStore } from '../store/authStore';
import { Trash2, Plus } from 'lucide-react';
import type { RegionDto } from '../types';

export default function RegionsPage() {
  const [page, setPage] = useState(0);
  const isAdmin = useAuthStore(s => s.isAdmin());
  const qc = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ['regions', page],
    queryFn: () => regionsApi.list(page, 20),
  });

  const deleteMutation = useMutation({
    mutationFn: regionsApi.delete,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['regions'] }),
  });

  if (isLoading) return <div className="flex justify-center p-12"><Spinner /></div>;

  return (
    <div>
      <PageHeader
        title="Regions"
        subtitle={`${data?.totalElements ?? 0} regions total`}
        action={isAdmin ? <Button size="sm"><Plus className="h-4 w-4 mr-1" />Add Region</Button> : undefined}
      />
      <Card>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b text-left text-gray-500">
                <th className="pb-3 font-medium">Code</th>
                <th className="pb-3 font-medium">Name</th>
                <th className="pb-3 font-medium">Country</th>
                <th className="pb-3 font-medium">Population</th>
                {isAdmin && <th className="pb-3 font-medium">Actions</th>}
              </tr>
            </thead>
            <tbody>
              {data?.content.map((region: RegionDto) => (
                <tr key={region.id} className="border-b last:border-0 hover:bg-gray-50">
                  <td className="py-3"><Badge label={region.code} color="blue" /></td>
                  <td className="py-3 font-medium">{region.name}</td>
                  <td className="py-3 text-gray-500">{region.countryCode}</td>
                  <td className="py-3 text-gray-500">
                    {region.population ? region.population.toLocaleString() : '—'}
                  </td>
                  {isAdmin && (
                    <td className="py-3">
                      <Button
                        variant="danger" size="sm"
                        onClick={() => deleteMutation.mutate(region.id)}
                        loading={deleteMutation.isPending}
                      >
                        <Trash2 className="h-3 w-3" />
                      </Button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {data && data.totalPages > 1 && (
          <div className="flex justify-between items-center mt-4 pt-4 border-t">
            <Button variant="secondary" size="sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>
              Previous
            </Button>
            <span className="text-sm text-gray-500">Page {page + 1} of {data.totalPages}</span>
            <Button variant="secondary" size="sm" disabled={data.last} onClick={() => setPage(p => p + 1)}>
              Next
            </Button>
          </div>
        )}
      </Card>
    </div>
  );
}
