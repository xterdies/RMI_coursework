# Regional Clustering Platform

Platform for clustering regions by economic indicators, analyzing time-series trends, and exporting analytical results.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 21, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Frontend | React 18, TypeScript, Vite, Tailwind CSS, TanStack Query, Zustand |
| Database | PostgreSQL 15, Liquibase migrations |
| Cache | Redis |
| Auth | JWT + OAuth2 (Google) |
| Export | Apache POI (Excel), iText 8 (PDF) |
| DevOps | Docker, Docker Compose, Nginx |

## Project Structure

```text
backend/src/main/java/com/platform/
- api/
  - controller/        # REST controllers
  - dto/               # Request / response DTO records
  - mapper/            # Entity -> DTO mapping
- domain/
  - entity/            # JPA entities
  - repository/        # Spring Data repositories
- infrastructure/
  - config/            # Security, cache, OpenAPI, WebClient config
  - external/          # World Bank synchronization
  - query/             # Filtering and sorting helpers
  - security/          # JWT filter and OAuth2 handlers
- service/             # Business logic, clustering, export, audit

frontend/src/
- api/                   # Axios client and typed service calls
- components/            # Layout, protected routes, shared UI
- pages/                 # Application screens
- store/                 # Zustand stores
- types/                 # Shared TypeScript DTOs
```

## Core Features

- user registration and JWT authentication
- OAuth2 login with Google
- region catalog management
- economic indicator catalog and time-series values
- K-Means clustering of regions by selected indicators
- linear trend forecasting for region indicators
- PDF and Excel export of clustering results
- weekly World Bank synchronization
- admin user management and audit logs

## Main Domain Objects

- `Role`
- `User`
- `RefreshToken`
- `SystemLog`
- `Region`
- `EconomicIndicator`
- `IndicatorValue`
- `ClusteringRun`
- `ClusteringAssignment`
- `TrendModel`

## Database Overview

```text
roles --< users --< refresh_tokens
              \--< system_logs

regions --< indicator_values >-- economic_indicators
regions --< clustering_assignments >-- clustering_runs
regions --< trend_models >-- economic_indicators
```

## Graphic Materials

The coursework graphic materials are stored in [docs/graphics](D:/RMI_coursework/rmi_coursework_updates/docs/graphics):

1. [01-bpmn-domain-processes.svg](D:/RMI_coursework/rmi_coursework_updates/docs/graphics/01-bpmn-domain-processes.svg) - BPMN-style model of domain processes
2. [02-software-architecture.svg](D:/RMI_coursework/rmi_coursework_updates/docs/graphics/02-software-architecture.svg) - software architecture poster
3. [03-database-model.svg](D:/RMI_coursework/rmi_coursework_updates/docs/graphics/03-database-model.svg) - database schema
4. [04-presentation-models.svg](D:/RMI_coursework/rmi_coursework_updates/docs/graphics/04-presentation-models.svg) - presentation/view models
5. [05-core-business-logic-algorithm.svg](D:/RMI_coursework/rmi_coursework_updates/docs/graphics/05-core-business-logic-algorithm.svg) - flowchart of the main business logic

All files are vector `SVG` posters prepared for A4 landscape placement.

## Implemented Use Cases

| # | Use Case | Role |
|---|----------|------|
| UC1 | User registration | Public |
| UC2 | JWT login | Public |
| UC3 | OAuth2 Google login | Public |
| UC4 | Token refresh and logout | User |
| UC5 | CRUD regions | Admin |
| UC6 | CRUD economic indicators | Admin |
| UC7 | Add and delete indicator values | Admin |
| UC8 | View time-series data | User |
| UC9 | Run K-Means clustering | User |
| UC10 | View clustering results and charts | User |
| UC11 | Compute linear trend forecast | User |
| UC12 | Export clustering to PDF | User |
| UC13 | Export clustering to Excel | User |
| UC14 | Scheduled World Bank synchronization | System |
| UC15 | Admin user management and audit logs | Admin |

## Quick Start

### Prerequisites

- Docker and Docker Compose
- Google OAuth2 credentials for OAuth login

### Run with Docker Compose

```bash
cp .env.example .env
docker compose --env-file .env.example up --build
```

Available services:

- Frontend: [http://localhost](http://localhost)
- Backend API: [http://localhost:8080](http://localhost:8080)
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Actuator health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### Run Locally

Backend:

```bash
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Frontend dev server is available at [http://localhost:5173](http://localhost:5173).

## Default Admin Credentials

- Email: `admin@platform.com`
- Password: `Admin1234!`

## API Overview

Key endpoint groups:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `GET /api/v1/regions`
- `POST /api/v1/regions`
- `GET /api/v1/indicators`
- `POST /api/v1/indicators/values`
- `POST /api/v1/clustering/run`
- `GET /api/v1/clustering/{id}`
- `POST /api/v1/trends/compute`
- `GET /api/v1/export/clustering/{id}/pdf`
- `GET /api/v1/export/clustering/{id}/excel`
- `GET /api/v1/admin/users`
- `GET /api/v1/admin/logs`

## Clustering Logic

`ClusteringService` implements the main analytical workflow:

1. load all regions
2. build the region x indicator feature matrix for the selected year
3. normalize features with z-score normalization
4. execute `KMeansAlgorithm`
5. compute inertia and distances to centroids
6. save `ClusteringRun` and `ClusteringAssignment`
7. return a DTO for charts and exports

`KMeansAlgorithm` uses:

- seeded centroid initialization
- iterative relabeling and centroid recomputation
- convergence threshold `1e-6`
- maximum `300` iterations

## Trend Analysis

`TrendService` + `LinearRegressionTrendStrategy` implement linear forecasting:

- Ordinary Least Squares on historical indicator values
- year normalization by `baseYear`
- computation of slope, intercept and `R^2`
- forecast generation for a target year
- persistence of parameters in `JSONB`

## World Bank Synchronization

`WorldBankSyncService` runs by scheduler and:

- finds indicators with `worldBankCode`
- requests data from the World Bank API through `WebClient`
- inserts only missing yearly values
- keeps synchronization idempotent

## Testing

```bash
cd backend
mvn test
mvn verify
mvn jacoco:report
```

## Notes

- Redis is used for cacheable lists and detail endpoints.
- Liquibase is the source of truth for schema evolution and seed data.
- The frontend includes analytical charts for clustering results.
