export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserDto;
}

export interface UserDto {
  id: number;
  email: string;
  fullName: string;
  role: string;
  enabled: boolean;
  createdAt: string;
}

export interface RegionDto {
  id: number;
  code: string;
  name: string;
  countryCode: string;
  latitude?: number;
  longitude?: number;
  population?: number;
  areaKm2?: number;
  createdAt: string;
}

export interface IndicatorDto {
  id: number;
  code: string;
  name: string;
  description?: string;
  unit?: string;
  source?: string;
  worldBankCode?: string;
  createdAt: string;
}

export interface IndicatorValueDto {
  id: number;
  regionId: number;
  regionName: string;
  indicatorId: number;
  indicatorName: string;
  year: number;
  value: number;
  sourceUrl?: string;
}

export interface ClusteringRunDto {
  id: number;
  name: string;
  kClusters: number;
  year: number;
  algorithm: string;
  iterations?: number;
  inertia?: number;
  metadata?: Record<string, unknown>;
  createdAt: string;
  assignments: AssignmentDto[];
}

export interface AssignmentDto {
  regionId: number;
  regionName: string;
  regionCode: string;
  clusterLabel: number;
  distanceToCentroid?: number;
}

export interface TrendModelDto {
  id: number;
  regionId: number;
  regionName: string;
  indicatorId: number;
  indicatorName: string;
  modelType: string;
  slope?: number;
  intercept?: number;
  rSquared?: number;
  forecastYear: number;
  forecastValue?: number;
  createdAt: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  path: string;
  timestamp: string;
}
