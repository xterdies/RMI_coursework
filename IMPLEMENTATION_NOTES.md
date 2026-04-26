# Implementation Notes

This document summarizes the added compliance items (Redis caching, design patterns, reflection/AOP, filtering/sorting, integration tests, CI).

## Redis caching

- **Backend deps**: `spring-boot-starter-data-redis` added, cache type switched to Redis in `backend/src/main/resources/application.yml`.
- **Redis configuration**:
  - `spring.data.redis.host`, `spring.data.redis.port`, optional `spring.data.redis.password`
  - `app.cache.ttl.default` (default **10m**) used by `RedisCacheManager` in `backend/src/main/java/com/platform/infrastructure/config/CacheConfig.java`.
- **docker-compose**: Redis service added in `docker-compose.yml` and backend wired with `REDIS_HOST/REDIS_PORT`.
- **Cache usage**:
  - Regions list: cached at controller level (`RegionController.list`) to avoid caching `Page` serialization; evicted by `RegionService` mutations.
  - Indicators list/time-series: cached in `IndicatorService`.
  - Clustering run lookup/list: cached by id in `ClusteringService` and list at controller level; evicted on run/delete.

## Design patterns added

- **Strategy**: trend computation is now delegated via `TrendComputationStrategy` (default `LinearRegressionTrendStrategy`) used by `TrendService`.
  - Files: `backend/src/main/java/com/platform/service/trend/*`
  - Marker comment: `// Design Pattern: Strategy`

- **Factory**: export generation is selected by format via `ClusteringRunExporterFactory`.
  - Files: `backend/src/main/java/com/platform/service/export/*`
  - Marker comment: `// Design Pattern: Factory`

- **Observer/Event**: on successful clustering run save, an event is published for decoupled side effects.
  - File: `backend/src/main/java/com/platform/service/ClusteringService.java`
  - Event: `backend/src/main/java/com/platform/service/event/ClusteringRunCompletedEvent.java`
  - Marker comment: `// Design Pattern: Observer/Event`

## Custom annotation + reflection (AOP)

- Annotation: `@MeasureExecutionTime` (`backend/src/main/java/com/platform/infrastructure/aop/MeasureExecutionTime.java`)
- Aspect: `MeasureExecutionTimeAspect` uses **reflection** to read annotation parameters at runtime and records timings via `ExecutionTimeRecorder`.
  - Files: `backend/src/main/java/com/platform/infrastructure/aop/*`
  - Applied to: `ClusteringService.runClustering`
  - Marker comment: `// Design Pattern: Decorator`
- Unit test: `backend/src/test/java/com/platform/infrastructure/aop/MeasureExecutionTimeAspectTest.java`

## Filtering / sorting (query parameters)

### Backend

Controllers accept optional parameters:

- `GET /api/v1/regions?filter=...&sort=...`
- `GET /api/v1/clustering?filter=...&sort=...`
- `GET /api/v1/admin/users?filter=...&sort=...`

Supported syntax:

- **filter**: `field:op:value` (multiple criteria separated by `;`)
  - ops: `eq`, `like`
  - examples:
    - `name:like:eu`
    - `countryCode:eq:UA`
    - `role.name:eq:ADMIN`
- **sort**: `field,asc|desc`
  - example: `createdAt,desc`

Implementation:

- Parser: `backend/src/main/java/com/platform/infrastructure/query/QueryParamParsers.java`
- Specs builder: `backend/src/main/java/com/platform/infrastructure/query/Specifications.java`

### Frontend

- Updated API clients to pass `filter`/`sort` parameters.
- Added table state stores (`frontend/src/store/tableStore.ts`) and basic filter/sort UI:
  - `RegionsPage`
  - `ClusteringPage`
  - `AdminUsersPage`

## Integration tests (Testcontainers)

- Base: `backend/src/test/java/com/platform/it/IntegrationTestBase.java`
  - Starts **PostgreSQL 15** and **Redis 7**
  - Uses `@DynamicPropertySource` to wire Spring properties
- Tests:
  - `AuthFlowIT`: login as seeded admin + access protected endpoint
  - `RedisCachingIT`: verifies a Redis cache key is created for regions list
  - `RestFlowIT`: create region → run clustering → fetch clustering run details

Run locally:

```bash
mvn -f backend/pom.xml test
```

## CI (GitHub Actions)

- Workflow: `.github/workflows/ci.yml`
  - Backend: JDK 21, `mvn test`, build JAR
  - Frontend: Node 20, `npm ci`, `npm run lint`, `npm run build`

## CORS tightening

- `SecurityConfig` now reads a comma-separated list from `app.cors.allowed-origins`.
- Default values (in `application.yml`) include:
  - `http://localhost`
  - `http://localhost:80`
  - `http://localhost:5173` (Vite)

## Required environment variables

- `JWT_SECRET` is **required** (no hard-coded default in `application.yml` / `docker-compose.yml`). Use `.env.example` as a dev template.

