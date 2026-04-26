import { useMemo, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { isAxiosError } from 'axios';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  BarChart3,
  Download,
  Play,
  Sparkles,
} from 'lucide-react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { clusteringApi, indicatorsApi } from '../api/services';
import { Button, Card, PageHeader, Badge, Spinner } from '../components/ui';
import { useClusteringTableStore } from '../store/tableStore';
import type { ClusteringRunDto, IndicatorDto } from '../types';

const CLUSTER_COLORS = ['#2563eb', '#059669', '#d97706', '#dc2626', '#7c3aed', '#0891b2', '#ea580c', '#65a30d'];

const schema = z.object({
  name: z.string().min(1, 'Name required'),
  kClusters: z.coerce.number().min(2).max(20),
  year: z.coerce.number().min(2000).max(2030),
  indicatorIds: z.array(z.number()).min(1, 'Select at least one indicator'),
});

type FormData = z.infer<typeof schema>;

function getErrorMessage(error: unknown) {
  if (isAxiosError(error)) {
    const message = (error.response?.data as { message?: string } | undefined)?.message;
    return message ?? error.message;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return 'Unable to complete the clustering request.';
}

function readNumberArray(value: unknown) {
  if (!Array.isArray(value)) {
    return [];
  }

  return value
    .map((entry) => Number(entry))
    .filter((entry) => Number.isFinite(entry));
}

function readMatrix(value: unknown) {
  if (!Array.isArray(value)) {
    return [];
  }

  return value
    .map((row) => Array.isArray(row)
      ? row.map((entry) => Number(entry)).filter((entry) => Number.isFinite(entry))
      : [])
    .filter((row) => row.length > 0);
}

function formatMetric(value: number | undefined, digits = 2) {
  if (value === undefined || Number.isNaN(value)) {
    return '-';
  }

  return value.toFixed(digits);
}

function buildCentroidSeries(selectedRun: ClusteringRunDto | null, indicators: IndicatorDto[] | undefined) {
  if (!selectedRun?.metadata) {
    return [];
  }

  const indicatorIds = readNumberArray(selectedRun.metadata.indicatorIds);
  const centroids = readMatrix(selectedRun.metadata.centroids);
  if (!indicatorIds.length || !centroids.length) {
    return [];
  }

  const indicatorNames = new Map((indicators ?? []).map((indicator) => [indicator.id, indicator.name]));

  return indicatorIds.map((indicatorId, index) => {
    const row: Record<string, number | string> = {
      indicator: indicatorNames.get(indicatorId) ?? `Indicator ${indicatorId}`,
    };

    centroids.forEach((cluster, clusterIndex) => {
      row[`Cluster ${clusterIndex + 1}`] = cluster[index] ?? 0;
    });

    return row;
  });
}

export default function ClusteringPage() {
  const [selectedRun, setSelectedRun] = useState<ClusteringRunDto | null>(null);
  const qc = useQueryClient();
  const { page, size, filterText, sortField, sortOrder, setPage, setFilterText, setSort } = useClusteringTableStore();

  const { data: runs, isLoading } = useQuery({
    queryKey: ['clustering', page, size, filterText, sortField, sortOrder],
    queryFn: () => clusteringApi.list(page, size, {
      filter: filterText ? `name:like:${filterText}` : undefined,
      sort: `${sortField},${sortOrder}`,
    }),
  });

  const { data: indicators } = useQuery({
    queryKey: ['indicators'],
    queryFn: indicatorsApi.list,
  });

  const { register, handleSubmit, setValue, watch, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { name: '', kClusters: 3, year: 2022, indicatorIds: [] },
  });

  const runMutation = useMutation({
    mutationFn: clusteringApi.run,
    onSuccess: (data) => {
      qc.invalidateQueries({ queryKey: ['clustering'] });
      setSelectedRun(data);
      reset({ name: '', kClusters: data.kClusters, year: data.year, indicatorIds: [] });
    },
  });

  const selectedIds = watch('indicatorIds') ?? [];
  const selectedIndicators = useMemo(
    () => (indicators ?? []).filter((indicator) => selectedIds.includes(indicator.id)),
    [indicators, selectedIds]
  );

  const runError = useMemo(
    () => (runMutation.isError ? getErrorMessage(runMutation.error) : null),
    [runMutation.error, runMutation.isError]
  );

  const clusterBreakdown = useMemo(() => {
    if (!selectedRun) {
      return [];
    }

    const grouped = new Map<number, { label: string; regions: number; averageDistance: number; totalDistance: number }>();

    selectedRun.assignments.forEach((assignment) => {
      const current = grouped.get(assignment.clusterLabel) ?? {
        label: `Cluster ${assignment.clusterLabel + 1}`,
        regions: 0,
        averageDistance: 0,
        totalDistance: 0,
      };

      current.regions += 1;
      current.totalDistance += assignment.distanceToCentroid ?? 0;
      current.averageDistance = current.totalDistance / current.regions;
      grouped.set(assignment.clusterLabel, current);
    });

    return Array.from(grouped.entries())
      .sort(([a], [b]) => a - b)
      .map(([clusterLabel, stats]) => ({
        clusterLabel,
        ...stats,
      }));
  }, [selectedRun]);

  const distanceChartData = useMemo(() => {
    if (!selectedRun) {
      return [];
    }

    return selectedRun.assignments.map((assignment) => ({
      region: assignment.regionCode,
      clusterLabel: assignment.clusterLabel,
      distance: assignment.distanceToCentroid ?? 0,
      fill: CLUSTER_COLORS[assignment.clusterLabel % CLUSTER_COLORS.length],
    }));
  }, [selectedRun]);

  const centroidSeries = useMemo(
    () => buildCentroidSeries(selectedRun, indicators),
    [indicators, selectedRun]
  );

  const summary = useMemo(() => {
    if (!selectedRun) {
      return [];
    }

    const distances = selectedRun.assignments.map((assignment) => assignment.distanceToCentroid ?? 0);
    const avgDistance = distances.length
      ? distances.reduce((total, value) => total + value, 0) / distances.length
      : 0;
    const largestCluster = clusterBreakdown.reduce(
      (largest, cluster) => (cluster.regions > largest.regions ? cluster : largest),
      clusterBreakdown[0] ?? { label: '-', regions: 0 }
    );

    return [
      { label: 'Regions', value: String(selectedRun.assignments.length) },
      { label: 'Clusters', value: String(selectedRun.kClusters) },
      { label: 'Iterations', value: String(selectedRun.iterations ?? '-') },
      { label: 'Avg Distance', value: formatMetric(avgDistance, 4) },
      { label: 'Inertia', value: formatMetric(selectedRun.inertia, 4) },
      { label: 'Largest Cluster', value: `${largestCluster.label} (${largestCluster.regions})` },
    ];
  }, [clusterBreakdown, selectedRun]);

  const toggleIndicator = (id: number) => {
    setValue(
      'indicatorIds',
      selectedIds.includes(id)
        ? selectedIds.filter((currentId) => currentId !== id)
        : [...selectedIds, id],
      { shouldValidate: true }
    );
  };

  const downloadFile = async (type: 'pdf' | 'excel', id: number) => {
    const blob = type === 'pdf' ? await clusteringApi.exportPdf(id) : await clusteringApi.exportExcel(id);
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = `clustering-${id}.${type === 'pdf' ? 'pdf' : 'xlsx'}`;
    anchor.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div>
      <PageHeader title="K-Means Clustering" subtitle="Cluster regions by economic indicators and inspect centroid behavior." />
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-1">
          <h2 className="mb-4 text-lg font-semibold">New Clustering Run</h2>
          <form
            onSubmit={handleSubmit((data: FormData) => runMutation.mutate(data as Required<FormData>))}
            className="space-y-4"
          >
            <div className="space-y-1">
              <label className="block text-sm font-medium text-gray-700">Run Name</label>
              <input className="input" {...register('name')} placeholder="GDP resilience 2022" />
              {errors.name && <p className="text-xs text-red-600">{errors.name.message}</p>}
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <label className="block text-sm font-medium text-gray-700">K Clusters</label>
                <input className="input" type="number" {...register('kClusters')} />
                {errors.kClusters && <p className="text-xs text-red-600">{errors.kClusters.message}</p>}
              </div>
              <div className="space-y-1">
                <label className="block text-sm font-medium text-gray-700">Year</label>
                <input className="input" type="number" {...register('year')} />
                {errors.year && <p className="text-xs text-red-600">{errors.year.message}</p>}
              </div>
            </div>

            <div className="space-y-2">
              <label className="block text-sm font-medium text-gray-700">Indicators</label>
              <div className="space-y-2 rounded-lg border border-gray-200 bg-gray-50 p-3">
                {indicators?.map((indicator) => (
                  <label key={indicator.id} className="flex cursor-pointer items-start gap-2">
                    <input
                      type="checkbox"
                      checked={selectedIds.includes(indicator.id)}
                      onChange={() => toggleIndicator(indicator.id)}
                      className="mt-1 rounded"
                    />
                    <span className="text-sm text-gray-700">{indicator.name}</span>
                  </label>
                ))}
              </div>
              {errors.indicatorIds && <p className="text-xs text-red-600">{errors.indicatorIds.message}</p>}
            </div>

            {selectedIndicators.length > 0 && (
              <div className="rounded-lg border border-blue-100 bg-blue-50 px-3 py-2 text-sm text-blue-700">
                Using {selectedIndicators.length} indicator{selectedIndicators.length > 1 ? 's' : ''}:{' '}
                {selectedIndicators.map((indicator) => indicator.code).join(', ')}
              </div>
            )}

            {runError && (
              <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                {runError}
              </div>
            )}

            <Button type="submit" className="w-full" loading={isSubmitting || runMutation.isPending}>
              <Play className="mr-2 h-4 w-4" />
              Run Clustering
            </Button>
          </form>
        </Card>

        <div className="space-y-4 lg:col-span-2">
          <Card>
            <div className="mb-3 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h3 className="font-semibold">Previous Runs</h3>
                <p className="text-sm text-gray-500">{runs?.totalElements ?? 0} stored clustering run(s)</p>
              </div>
              <div className="flex w-full gap-2 sm:w-auto">
                <input
                  className="input w-full sm:w-64"
                  placeholder="Filter by name..."
                  value={filterText}
                  onChange={(e) => setFilterText(e.target.value)}
                />
                <select
                  className="input sm:w-56"
                  value={`${sortField},${sortOrder}`}
                  onChange={(e) => {
                    const [field, order] = e.target.value.split(',');
                    setSort(field, (order as 'asc' | 'desc') ?? 'desc');
                  }}
                >
                  <option value="createdAt,desc">Newest</option>
                  <option value="createdAt,asc">Oldest</option>
                  <option value="year,desc">Year (desc)</option>
                  <option value="year,asc">Year (asc)</option>
                  <option value="kClusters,desc">K (desc)</option>
                  <option value="kClusters,asc">K (asc)</option>
                </select>
              </div>
            </div>

            {isLoading ? (
              <Spinner />
            ) : (
              <div className="space-y-2">
                {runs?.content.length ? runs.content.map((run: ClusteringRunDto) => (
                  <button
                    key={run.id}
                    type="button"
                    className="flex w-full items-center justify-between rounded-lg border p-3 text-left transition-colors hover:bg-gray-50"
                    onClick={() => clusteringApi.getById(run.id).then(setSelectedRun)}
                  >
                    <div>
                      <p className="text-sm font-medium">{run.name}</p>
                      <p className="text-xs text-gray-500">K={run.kClusters} | {run.year}</p>
                    </div>
                    <Badge label={`${run.kClusters} clusters`} color="blue" />
                  </button>
                )) : (
                  <div className="rounded-lg border border-dashed border-gray-300 px-4 py-8 text-center text-sm text-gray-500">
                    No clustering runs yet. Start one from the form on the left.
                  </div>
                )}
              </div>
            )}

            {runs && runs.totalPages > 1 && (
              <div className="mt-4 flex items-center justify-between border-t pt-4">
                <Button variant="secondary" size="sm" disabled={page === 0} onClick={() => setPage(page - 1)}>
                  Previous
                </Button>
                <span className="text-sm text-gray-500">Page {page + 1} of {runs.totalPages}</span>
                <Button variant="secondary" size="sm" disabled={runs.last} onClick={() => setPage(page + 1)}>
                  Next
                </Button>
              </div>
            )}
          </Card>

          {selectedRun ? (
            <Card>
              <div className="mb-4 flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
                <div>
                  <div className="mb-2 flex items-center gap-2">
                    <Sparkles className="h-4 w-4 text-primary-600" />
                    <h3 className="font-semibold">{selectedRun.name}</h3>
                  </div>
                  <p className="text-sm text-gray-500">
                    {selectedRun.kClusters} clusters | Year {selectedRun.year} | Inertia {formatMetric(selectedRun.inertia, 4)}
                  </p>
                </div>

                <div className="flex flex-wrap gap-2">
                  <Button variant="secondary" size="sm" onClick={() => downloadFile('pdf', selectedRun.id)}>
                    <Download className="mr-1 h-3 w-3" />
                    PDF
                  </Button>
                  <Button variant="secondary" size="sm" onClick={() => downloadFile('excel', selectedRun.id)}>
                    <Download className="mr-1 h-3 w-3" />
                    Excel
                  </Button>
                </div>
              </div>

              <div className="mb-6 grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
                {summary.map((item) => (
                  <div key={item.label} className="rounded-lg border border-gray-200 bg-gray-50 px-4 py-3">
                    <p className="text-xs font-medium uppercase tracking-wide text-gray-500">{item.label}</p>
                    <p className="mt-1 text-lg font-semibold text-gray-900">{item.value}</p>
                  </div>
                ))}
              </div>

              <div className="grid gap-6 xl:grid-cols-2">
                <div>
                  <div className="mb-3 flex items-center gap-2">
                    <BarChart3 className="h-4 w-4 text-primary-600" />
                    <h4 className="font-medium text-gray-900">Cluster Size</h4>
                  </div>
                  <div className="h-64">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={clusterBreakdown}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="label" />
                        <YAxis allowDecimals={false} />
                        <Tooltip />
                        <Bar dataKey="regions" radius={[6, 6, 0, 0]}>
                          {clusterBreakdown.map((entry) => (
                            <Cell key={entry.label} fill={CLUSTER_COLORS[entry.clusterLabel % CLUSTER_COLORS.length]} />
                          ))}
                        </Bar>
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </div>

                <div>
                  <div className="mb-3 flex items-center gap-2">
                    <BarChart3 className="h-4 w-4 text-primary-600" />
                    <h4 className="font-medium text-gray-900">Distance by Region</h4>
                  </div>
                  <div className="h-64">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={distanceChartData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="region" />
                        <YAxis />
                        <Tooltip />
                        <Bar dataKey="distance" radius={[6, 6, 0, 0]}>
                          {distanceChartData.map((entry) => (
                            <Cell key={entry.region} fill={entry.fill} />
                          ))}
                        </Bar>
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              </div>

              {centroidSeries.length > 0 && (
                <div className="mt-6">
                  <div className="mb-3 flex items-center gap-2">
                    <BarChart3 className="h-4 w-4 text-primary-600" />
                    <h4 className="font-medium text-gray-900">Centroid Profiles (normalized)</h4>
                  </div>
                  <div className="h-72">
                    <ResponsiveContainer width="100%" height="100%">
                      <LineChart data={centroidSeries}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="indicator" />
                        <YAxis />
                        <Tooltip />
                        <Legend />
                        {clusterBreakdown.map((cluster) => (
                          <Line
                            key={cluster.label}
                            type="monotone"
                            dataKey={cluster.label}
                            stroke={CLUSTER_COLORS[cluster.clusterLabel % CLUSTER_COLORS.length]}
                            strokeWidth={2}
                            dot
                          />
                        ))}
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              )}

              <div className="mt-6 overflow-x-auto">
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
                    {selectedRun.assignments.map((assignment) => (
                      <tr key={assignment.regionId} className="border-b last:border-0">
                        <td className="py-2">{assignment.regionName}</td>
                        <td className="py-2 text-gray-500">{assignment.regionCode}</td>
                        <td className="py-2">
                          <span className="inline-flex items-center gap-2">
                            <span
                              className="inline-block h-3 w-3 rounded-full"
                              style={{ backgroundColor: CLUSTER_COLORS[assignment.clusterLabel % CLUSTER_COLORS.length] }}
                            />
                            Cluster {assignment.clusterLabel + 1}
                          </span>
                        </td>
                        <td className="py-2 text-gray-500">{formatMetric(assignment.distanceToCentroid, 4)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </Card>
          ) : (
            <Card>
              <div className="rounded-lg border border-dashed border-gray-300 px-6 py-10 text-center text-sm text-gray-500">
                Select a run to inspect assignments, centroid charts, and cluster balance.
              </div>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}
