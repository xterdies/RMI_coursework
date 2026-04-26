import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { trendsApi, regionsApi, indicatorsApi } from '../api/services';
import { Button, Card, PageHeader } from '../components/ui';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import type { TrendModelDto } from '../types';

export default function TrendsPage() {
  const [regionId, setRegionId] = useState<number | null>(null);
  const [indicatorId, setIndicatorId] = useState<number | null>(null);
  const [forecastYear, setForecastYear] = useState(2025);

  const { data: regions } = useQuery({ queryKey: ['regions-all'], queryFn: () => regionsApi.list(0, 100) });
  const { data: indicators } = useQuery({ queryKey: ['indicators'], queryFn: indicatorsApi.list });
  const { data: trends, refetch } = useQuery({
    queryKey: ['trends', regionId],
    queryFn: () => trendsApi.getByRegion(regionId!),
    enabled: !!regionId,
  });

  const computeMutation = useMutation({
    mutationFn: trendsApi.compute,
    onSuccess: () => refetch(),
  });

  const chartData = trends?.map((t: TrendModelDto) => ({
    year: t.forecastYear,
    forecast: t.forecastValue,
    r2: t.rSquared,
  })) ?? [];

  return (
    <div>
      <PageHeader title="Trend Analysis" subtitle="Linear regression forecasting for economic indicators" />
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card>
          <h2 className="text-lg font-semibold mb-4">Compute Forecast</h2>
          <div className="space-y-4">
            <div className="space-y-1">
              <label className="block text-sm font-medium text-gray-700">Region</label>
              <select className="input" onChange={e => setRegionId(Number(e.target.value))}>
                <option value="">Select region...</option>
                {regions?.content.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
              </select>
            </div>
            <div className="space-y-1">
              <label className="block text-sm font-medium text-gray-700">Indicator</label>
              <select className="input" onChange={e => setIndicatorId(Number(e.target.value))}>
                <option value="">Select indicator...</option>
                {indicators?.map(i => <option key={i.id} value={i.id}>{i.name}</option>)}
              </select>
            </div>
            <div className="space-y-1">
              <label className="block text-sm font-medium text-gray-700">Forecast Year</label>
              <input className="input" type="number" value={forecastYear}
                onChange={e => setForecastYear(Number(e.target.value))} min={2024} max={2050} />
            </div>
            <Button
              className="w-full"
              disabled={!regionId || !indicatorId}
              loading={computeMutation.isPending}
              onClick={() => computeMutation.mutate({ regionId: regionId!, indicatorId: indicatorId!, forecastYear })}
            >
              Compute Trend
            </Button>
          </div>
        </Card>

        <div className="lg:col-span-2 space-y-4">
          {trends && trends.length > 0 && (
            <Card>
              <h3 className="font-semibold mb-4">Forecast Results</h3>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="year" />
                  <YAxis />
                  <Tooltip />
                  <Line type="monotone" dataKey="forecast" stroke="#3b82f6" strokeWidth={2} dot />
                </LineChart>
              </ResponsiveContainer>
            </Card>
          )}

          {trends && (
            <Card>
              <h3 className="font-semibold mb-3">Trend Models</h3>
              <div className="space-y-3">
                {trends.map((t: TrendModelDto) => (
                  <div key={t.id} className="p-3 rounded-lg border">
                    <div className="flex justify-between items-start">
                      <div>
                        <p className="font-medium text-sm">{t.indicatorName}</p>
                        <p className="text-xs text-gray-500">
                          Forecast {t.forecastYear}: <strong>{t.forecastValue?.toFixed(2)}</strong>
                        </p>
                      </div>
                      <div className="text-right text-xs text-gray-500">
                        <p>R² = {t.rSquared?.toFixed(4)}</p>
                        <p>Slope: {t.slope?.toFixed(4)}</p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}
