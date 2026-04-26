# Regional Clustering Platform

Platform for clustering regions by economic indicators with a trend development detection module.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Frontend | React 18, TypeScript, Vite, Tailwind CSS, TanStack Query |
| Database | PostgreSQL 15, Liquibase migrations |
| Auth | JWT + OAuth2 (Google) |
| Export | Apache POI (Excel), iText 8 (PDF) |
| DevOps | Docker, Docker Compose |

## Architecture

```
backend/src/main/java/com/platform/
├── api/
│   ├── controller/     # REST controllers (UC1–UC15)
│   ├── dto/            # Request/Response records
│   └── mapper/         # MapStruct entity↔DTO mappers
├── service/            # Business logic layer
│   └── exception/      # Domain exceptions
├── domain/
│   ├── entity/         # JPA entities (8 tables, 3NF)
│   └── repository/     # Spring Data repositories
└── infrastructure/
    ├── config/         # Security, OpenAPI, WebClient
    ├── external/       # World Bank API sync
    └── security/       # JWT filter, OAuth2 handler
```

## Database Schema (3NF, 8+ tables)

```
roles ──< users ──< refresh_tokens
                 └─< system_logs
regions ──< indicator_values >── economic_indicators
        └─< clustering_assignments >── clustering_runs
        └─< trend_models >── economic_indicators
```

## Use Cases Implemented

| # | Use Case | Role |
|---|----------|------|
| UC1 | User Registration | Public |
| UC2 | JWT Login | Public |
| UC3 | OAuth2 Google Login | Public |
| UC4 | Token Refresh / Logout | User |
| UC5 | CRUD Regions | Admin |
| UC6 | CRUD Economic Indicators | Admin |
| UC7 | Add/Delete Indicator Values | Admin |
| UC8 | View Time-Series Data | User |
| UC9 | Run K-Means Clustering | User |
| UC10 | View Clustering Results | User |
| UC11 | Compute Linear Trend Forecast | User |
| UC12 | Export Clustering to PDF | User |
| UC13 | Export Clustering to Excel | User |
| UC14 | World Bank API Auto-Sync | System |
| UC15 | Admin User Management & Audit Logs | Admin |

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Google OAuth2 credentials (optional for local dev)

### Run with Docker Compose

```bash
cp .env.example .env
# Edit .env with your Google OAuth2 credentials
docker-compose up --build
```

- Frontend: http://localhost:80
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator Health: http://localhost:8080/actuator/health

### Run Locally (Development)

**Backend:**
```bash
cd backend
# Start PostgreSQL first (or use docker-compose up db)
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

### Default Admin Credentials
- Email: `admin@platform.com`
- Password: `Admin1234!`

## API Documentation

Full Swagger/OpenAPI docs available at `/swagger-ui.html` when running.

Key endpoint groups:
- `POST /api/v1/auth/register` — Register
- `POST /api/v1/auth/login` — Login → JWT
- `GET  /api/v1/regions` — List regions (paginated)
- `POST /api/v1/clustering/run` — Execute K-Means
- `POST /api/v1/trends/compute` — Compute linear forecast
- `GET  /api/v1/export/clustering/{id}/pdf` — PDF export
- `GET  /api/v1/export/clustering/{id}/excel` — Excel export

## Testing

```bash
cd backend
mvn test                    # Run all tests
mvn verify                  # Run tests + JaCoCo coverage check (≥40%)
mvn jacoco:report           # Generate HTML coverage report
```

## K-Means Algorithm

Pure Java implementation in `KMeansAlgorithm.java`:
1. Random centroid initialization (seeded for reproducibility)
2. Z-score normalization of feature matrix
3. Iterative label assignment + centroid recomputation
4. Convergence detection (threshold 1e-6) or max 300 iterations
5. Inertia (WCSS) stored as metadata in JSONB

## Trend Analysis

Linear regression in `TrendService.java`:
- Ordinary Least Squares (OLS) on historical time-series
- Computes slope, intercept, R² coefficient
- Stores forecast value for target year
- Parameters persisted in JSONB for reproducibility

## World Bank Sync

`WorldBankSyncService` runs weekly (Sunday 02:00) via `@Scheduled`:
- Fetches last 10 years of data per indicator per region
- Skips existing records (upsert-safe)
- Configurable via `app.world-bank.sync-cron`
