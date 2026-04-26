import { apiClient } from './client';
import type {
  AuthResponse, RegionDto, IndicatorDto, IndicatorValueDto,
  ClusteringRunDto, TrendModelDto, PagedResponse
} from '../types';

// Auth
export const authApi = {
  register: (data: { email: string; password: string; fullName: string }) =>
    apiClient.post<AuthResponse>('/auth/register', data).then(r => r.data),
  login: (data: { email: string; password: string }) =>
    apiClient.post<AuthResponse>('/auth/login', data).then(r => r.data),
  refresh: (refreshToken: string) =>
    apiClient.post<AuthResponse>('/auth/refresh', { refreshToken }).then(r => r.data),
  logout: () => apiClient.post('/auth/logout'),
};

// Regions
export const regionsApi = {
  list: (page = 0, size = 20) =>
    apiClient.get<PagedResponse<RegionDto>>(`/regions?page=${page}&size=${size}`).then(r => r.data),
  getById: (id: number) => apiClient.get<RegionDto>(`/regions/${id}`).then(r => r.data),
  getByCountry: (code: string) => apiClient.get<RegionDto[]>(`/regions/country/${code}`).then(r => r.data),
  create: (data: Omit<RegionDto, 'id' | 'createdAt'>) =>
    apiClient.post<RegionDto>('/regions', data).then(r => r.data),
  update: (id: number, data: Partial<RegionDto>) =>
    apiClient.patch<RegionDto>(`/regions/${id}`, data).then(r => r.data),
  delete: (id: number) => apiClient.delete(`/regions/${id}`),
};

// Indicators
export const indicatorsApi = {
  list: () => apiClient.get<IndicatorDto[]>('/indicators').then(r => r.data),
  getById: (id: number) => apiClient.get<IndicatorDto>(`/indicators/${id}`).then(r => r.data),
  create: (data: Omit<IndicatorDto, 'id' | 'createdAt'>) =>
    apiClient.post<IndicatorDto>('/indicators', data).then(r => r.data),
  delete: (id: number) => apiClient.delete(`/indicators/${id}`),
  getValues: (regionId: number, indicatorId: number) =>
    apiClient.get<IndicatorValueDto[]>(`/indicators/values/region/${regionId}/indicator/${indicatorId}`)
      .then(r => r.data),
  createValue: (data: { regionId: number; indicatorId: number; year: number; value: number }) =>
    apiClient.post<IndicatorValueDto>('/indicators/values', data).then(r => r.data),
  deleteValue: (id: number) => apiClient.delete(`/indicators/values/${id}`),
};

// Clustering
export const clusteringApi = {
  run: (data: { name: string; kClusters: number; year: number; indicatorIds: number[] }) =>
    apiClient.post<ClusteringRunDto>('/clustering/run', data).then(r => r.data),
  list: (page = 0, size = 10) =>
    apiClient.get<PagedResponse<ClusteringRunDto>>(`/clustering?page=${page}&size=${size}`).then(r => r.data),
  getById: (id: number) => apiClient.get<ClusteringRunDto>(`/clustering/${id}`).then(r => r.data),
  delete: (id: number) => apiClient.delete(`/clustering/${id}`),
  exportPdf: (id: number) =>
    apiClient.get(`/export/clustering/${id}/pdf`, { responseType: 'blob' }).then(r => r.data),
  exportExcel: (id: number) =>
    apiClient.get(`/export/clustering/${id}/excel`, { responseType: 'blob' }).then(r => r.data),
};

// Trends
export const trendsApi = {
  compute: (data: { regionId: number; indicatorId: number; forecastYear: number }) =>
    apiClient.post<TrendModelDto>('/trends/compute', data).then(r => r.data),
  getByRegion: (regionId: number) =>
    apiClient.get<TrendModelDto[]>(`/trends/region/${regionId}`).then(r => r.data),
};

// Admin
export const adminApi = {
  listUsers: (page = 0, size = 20) =>
    apiClient.get(`/admin/users?page=${page}&size=${size}`).then(r => r.data),
  updateRole: (id: number, role: string) =>
    apiClient.patch(`/admin/users/${id}/role?role=${role}`).then(r => r.data),
  toggleUser: (id: number) => apiClient.patch(`/admin/users/${id}/toggle`).then(r => r.data),
  deleteUser: (id: number) => apiClient.delete(`/admin/users/${id}`),
  getLogs: (page = 0, size = 50) =>
    apiClient.get(`/admin/logs?page=${page}&size=${size}`).then(r => r.data),
};
