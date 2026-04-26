import { useMemo, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { isAxiosError } from 'axios';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Trash2, Plus, X } from 'lucide-react';
import { regionsApi } from '../api/services';
import { Button, Card, Input, PageHeader, Badge, Spinner } from '../components/ui';
import { useAuthStore } from '../store/authStore';
import { useRegionsTableStore } from '../store/tableStore';
import type { RegionDto } from '../types';

const createRegionSchema = z.object({
  code: z.string().trim().min(2, 'Code is required').max(10, 'Max 10 characters'),
  name: z.string().trim().min(2, 'Name is required').max(255, 'Max 255 characters'),
  countryCode: z.string().trim().min(2, 'Use 2-3 letter code').max(3, 'Use 2-3 letter code'),
  population: z.number().int().positive('Population must be positive').optional(),
  latitude: z.number().min(-90, 'Min -90').max(90, 'Max 90').optional(),
  longitude: z.number().min(-180, 'Min -180').max(180, 'Max 180').optional(),
  areaKm2: z.number().positive('Area must be positive').optional(),
});

type CreateRegionForm = z.infer<typeof createRegionSchema>;

function toOptionalNumber(value: unknown) {
  if (value === '' || value === null || value === undefined) {
    return undefined;
  }

  const parsed = Number(value);
  return Number.isNaN(parsed) ? undefined : parsed;
}

function getErrorMessage(error: unknown) {
  if (isAxiosError(error)) {
    const message = (error.response?.data as { message?: string } | undefined)?.message;
    return message ?? error.message;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return 'Something went wrong while processing the request.';
}

export default function RegionsPage() {
  const isAdmin = useAuthStore((s) => s.isAdmin());
  const qc = useQueryClient();
  const [showCreateForm, setShowCreateForm] = useState(false);
  const {
    page,
    size,
    filterText,
    sortField,
    sortOrder,
    setPage,
    setFilterText,
    setSort,
  } = useRegionsTableStore();

  const { data, isLoading } = useQuery({
    queryKey: ['regions', page, size, filterText, sortField, sortOrder],
    queryFn: () => regionsApi.list(page, size, {
      filter: filterText ? `name:like:${filterText}` : undefined,
      sort: `${sortField},${sortOrder}`,
    }),
  });

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<CreateRegionForm>({
    resolver: zodResolver(createRegionSchema),
    defaultValues: {
      code: '',
      name: '',
      countryCode: '',
    },
  });

  const createMutation = useMutation({
    mutationFn: regionsApi.create,
    onSuccess: async () => {
      setPage(0);
      await qc.invalidateQueries({ queryKey: ['regions'] });
      reset();
      setShowCreateForm(false);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: regionsApi.delete,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['regions'] }),
  });

  const createError = useMemo(
    () => (createMutation.isError ? getErrorMessage(createMutation.error) : null),
    [createMutation.error, createMutation.isError]
  );

  if (isLoading) {
    return <div className="flex justify-center p-12"><Spinner /></div>;
  }

  return (
    <div>
      <PageHeader
        title="Regions"
        subtitle={`${data?.totalElements ?? 0} regions total`}
        action={isAdmin ? (
          <Button
            size="sm"
            type="button"
            onClick={() => {
              reset();
              setShowCreateForm((current) => !current);
            }}
          >
            {showCreateForm ? <X className="mr-1 h-4 w-4" /> : <Plus className="mr-1 h-4 w-4" />}
            {showCreateForm ? 'Close Form' : 'Add Region'}
          </Button>
        ) : undefined}
      />

      {isAdmin && showCreateForm && (
        <Card className="mb-6">
          <div className="mb-4 flex items-start justify-between gap-4">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">Create Region</h2>
              <p className="mt-1 text-sm text-gray-500">Add a region with optional geographic and population fields.</p>
            </div>
          </div>

          <form
            className="space-y-4"
            onSubmit={handleSubmit((values) => createMutation.mutate({
              ...values,
              code: values.code.trim().toUpperCase(),
              name: values.name.trim(),
              countryCode: values.countryCode.trim().toUpperCase(),
            }))}
          >
            <div className="grid gap-4 md:grid-cols-3">
              <Input label="Code" placeholder="DE-BY" error={errors.code?.message} {...register('code')} />
              <Input label="Name" placeholder="Bavaria" error={errors.name?.message} {...register('name')} />
              <Input label="Country Code" placeholder="DE" error={errors.countryCode?.message} {...register('countryCode')} />
            </div>

            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
              <Input
                label="Population"
                type="number"
                placeholder="13150000"
                error={errors.population?.message}
                {...register('population', { setValueAs: toOptionalNumber })}
              />
              <Input
                label="Latitude"
                type="number"
                step="0.000001"
                placeholder="48.7904"
                error={errors.latitude?.message}
                {...register('latitude', { setValueAs: toOptionalNumber })}
              />
              <Input
                label="Longitude"
                type="number"
                step="0.000001"
                placeholder="11.4979"
                error={errors.longitude?.message}
                {...register('longitude', { setValueAs: toOptionalNumber })}
              />
              <Input
                label="Area (km2)"
                type="number"
                step="0.01"
                placeholder="70550"
                error={errors.areaKm2?.message}
                {...register('areaKm2', { setValueAs: toOptionalNumber })}
              />
            </div>

            {createError && (
              <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                {createError}
              </div>
            )}

            <div className="flex flex-wrap gap-3">
              <Button type="submit" loading={isSubmitting || createMutation.isPending}>
                <Plus className="mr-2 h-4 w-4" />
                Save Region
              </Button>
              <Button
                type="button"
                variant="secondary"
                onClick={() => {
                  reset();
                  setShowCreateForm(false);
                }}
              >
                Cancel
              </Button>
            </div>
          </form>
        </Card>
      )}

      <Card>
        <div className="mb-4 flex flex-col gap-3 sm:flex-row">
          <input
            className="input w-full sm:w-72"
            placeholder="Filter by name..."
            value={filterText}
            onChange={(e) => setFilterText(e.target.value)}
          />
          <select
            className="input sm:w-56"
            value={`${sortField},${sortOrder}`}
            onChange={(e) => {
              const [field, order] = e.target.value.split(',');
              setSort(field, (order as 'asc' | 'desc') ?? 'asc');
            }}
          >
            <option value="name,asc">Name (A-Z)</option>
            <option value="name,desc">Name (Z-A)</option>
            <option value="createdAt,desc">Newest</option>
            <option value="createdAt,asc">Oldest</option>
          </select>
        </div>

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
              {data?.content.length ? data.content.map((region: RegionDto) => (
                <tr key={region.id} className="border-b last:border-0 hover:bg-gray-50">
                  <td className="py-3"><Badge label={region.code} color="blue" /></td>
                  <td className="py-3 font-medium">{region.name}</td>
                  <td className="py-3 text-gray-500">{region.countryCode}</td>
                  <td className="py-3 text-gray-500">
                    {region.population ? region.population.toLocaleString() : '-'}
                  </td>
                  {isAdmin && (
                    <td className="py-3">
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={() => deleteMutation.mutate(region.id)}
                        loading={deleteMutation.isPending}
                      >
                        <Trash2 className="h-3 w-3" />
                      </Button>
                    </td>
                  )}
                </tr>
              )) : (
                <tr>
                  <td colSpan={isAdmin ? 5 : 4} className="py-10 text-center text-sm text-gray-500">
                    No regions match the current filter.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {data && data.totalPages > 1 && (
          <div className="mt-4 flex items-center justify-between border-t pt-4">
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
