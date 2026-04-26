import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { clusteringApi, indicatorsApi } from '../api/services';
import { Button, Card, PageHeader, Badge, Spinner } from '../components/ui';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Download, Play } from 'lucide-react';
import type { ClusteringRunDto } from '../types';

const CLUSTER_COLORS = ['#3b82f6','#10b981','#f59e0b','#ef4444','#8b5cf6','#06b6d4','#f97316','#84cc16'];

const schema = z.object({
  name: z.string().min(1, 'Name required'),
  kClusters: z.coerce.number().min(2).max(20),
  year: z.coerce.number().min(2000).max(2030),
  indicatorIds: z.array(z.number()).min(1, 'Select at least one indicator'),
});
type FormData = z.infer<typeof schema>;

export default function ClusteringPage() {
  const [selectedRun, setSelectedRun] = useState<ClusteringRunDto | null>(null);
  const qc = useQueryClient();

  const { data: runs, isLoading } = useQuery({
    queryKey: ['clustering'],
    queryFn: () => clusteringApi.list(0, 20),
  });
  const { data: indicators } = useQuery({ queryKey: ['indicators'], queryFn: indicatorsApi.list });

  const { register, handleSubmit, setValue, watch, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { kClusters: 3, year: 2022, indicatorIds: [] },
  });

  const runMutation = useMutation({
    mutationFn: clusteringApi.run,
    onSuccess: (data) => {
      qc.invalidateQueries({ queryKey: ['clustering'] });
      setSelectedRun(data);
    },
  });

  const selectedIds = watch('indicatorIds');

  const toggleIndicator = (id: number) => {
    const current = selectedIds ?? [];
    setValue('indicatorIds', current.includes(id) ? current.filter(i => i !== id) : [...current, id]);
  };

  const downloadFile = async (type: 'pdf' | 'excel', id: number) => {
    const blob = type === 'pdf' ? await clusteringApi.exportPdf(id) : await clusteringApi.exportExcel(id);
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `clustering-${id}.${type === 'pdf' ? 'pdf' : 'xlsx'}`;
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div>
      <PageHeader title="K-Means Clustering" subtitle="Cluster regions by economic indicators" />
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Run Form */}
        <Card className="lg:col-span-1">
          <h2 className="text-lg font-semibold mb-4">New Clustering Run</h2>
          <form onSubmit={handleSubmit((d) => runMutation.mutate(d as Required<FormData>))} className="space-y-4">
            <div className="space-y-1">
              <label className="block text-sm font-medium text-gray-700">Run Name</label>
              <input className="input" {...register('name')} placeholder="e.g. GDP Analysis 2022" />
              {errors.name && <p className="text-xs text-red-600">{errors.name.message}</p>}
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <label className="block text-sm font-medium text-gray-700">K Clusters</label>
                <input className="input" type="number" {...register('kClusters')} />
              </div>
              <div className="space-y-1">
                <label className="block text-sm font-medium text-gray-700">Year</label>
                <input className="input" type="number" {...register('year')} />
              </div>
            </div>
            <div className="space-y-2">
              <label className="block text-sm font-medium text-gray-700">Indicators</label>
              {indicators?.map(ind => (
                <label key={ind.id} className="flex items-center gap-2 cursor-pointer">
                  <input type="checkbox" checked={selectedIds?.includes(ind.id)}
                    onChange={() => toggleIndicator(ind.id)} className="rounded" />
                  <span className="text-sm">{ind.name}</span>
                </label>
              ))}
              {errors.indicatorIds && <p className="text-xs text-red-600">{errors.indicatorIds.message}</p>}
            </div>
            <Button type="submit" className="w-full" loading={isSubmitting || runMutation.isPending}>
              <Play className="h-4 w-4 mr-2" /> Run Clustering
            </Button>
          </form>
        </Card>

        {/* Results */}
        <div className="lg:col-span-2 space-y-4">
          {selectedRun && (
            <Card>
              <div className="flex items-center justify-between mb-4">
                <div>
                  <h3 className="font-semibold">{selectedRun.name}</h3>
                  <p className="text-sm text-gray-500">
                    {selectedRun.kClusters} clusters | Year {selectedRun.year} | Inertia: {selectedRun.inertia?.toFixed(2)}
                  </p>
                </div>
                <div className="flex gap-2">
                  <Button variant="secondary" size="sm" onClick={() => downloadFile('pdf', selectedRun.id)}>
                    <Download className="h-3 w-3 mr-1" /> PDF
                  </Button>
                  <Button variant="secondary" size="sm" onClick={() => downloadFile('excel', selectedRun.id)}>
                    <Download className="h-3 w-3 mr-1" /> Excel
                  </Button>
                </div>
              </div>
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b text-left text-gray-500">
                      <th className="pb-2 font-medium">Region</th>
                      <th className="pb-2 font-medium">Code</th>
                      <th className="pb-2 font-medium">Cluster</th>
                      <th className="pb-2 font-medium">Distance</th>
                    </tr>
                  </thead>
                  <tbody>
                    {selectedRun.assignments.map(a => (
                      <tr key={a.regionId} className="border-b last:border-0">
                        <td className="py-2">{a.regionName}</td>
                        <td className="py-2 text-gray-500">{a.regionCode}</td>
                        <td className="py-2">
                          <span className="inline-flex items-center gap-1">
                            <span className="w-3 h-3 rounded-full inline-block"
                              style={{ backgroundColor: CLUSTER_COLORS[a.clusterLabel % CLUSTER_COLORS.length] }} />
                            Cluster {a.clusterLabel + 1}
                          </span>
                        </td>
                        <td className="py-2 text-gray-500">{a.distanceToCentroid?.toFixed(4) ?? '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </Card>
          )}

          <Card>
            <h3 className="font-semibold mb-3">Previous Runs</h3>
            {isLoading ? <Spinner /> : (
              <div className="space-y-2">
                {runs?.content.map((run: ClusteringRunDto) => (
                  <div key={run.id}
                    className="flex items-center justify-between p-3 rounded-lg border hover:bg-gray-50 cursor-pointer"
                    onClick={() => clusteringApi.getById(run.id).then(setSelectedRun)}>
                    <div>
                      <p className="font-medium text-sm">{run.name}</p>
                      <p className="text-xs text-gray-500">K={run.kClusters} | {run.year}</p>
                    </div>
                    <Badge label={`${run.kClusters} clusters`} color="blue" />
                  </div>
                ))}
              </div>
            )}
          </Card>
        </div>
      </div>
    </div>
  );
}
