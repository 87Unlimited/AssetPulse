# AssetPulse — Investment Portfolio Tracker

## Project Overview
A personal investment portfolio tracker. Users can track stocks, ETFs, and crypto holdings with manual price updates (market data API to be added later).

## Repository Structure
Monorepo:
- `backend/` — Java 21, Spring Boot 4.0.5
- `frontend/` — React 19, TypeScript, Vite
- `docker-compose.yml` — PostgreSQL 16

## Backend
- Base package: `com.assetpulse.backend`
- Feature packages: `auth`, `portfolio`, `holding`, `transaction`, `market`, `calculation`, `common`
- Each feature is self-contained: entity + repository + service + controller + DTOs in one package
- Security: JWT authentication via `common/security/`
- Database: PostgreSQL 16 running in Docker container `assetpulse-db`

### Key Patterns
- Never return JPA entities from controllers — always map to response DTOs
- Always get current user from `SecurityContextHolder`, never from request body
- Use `BigDecimal` for all monetary values, never `double`
- Use `Instant` for all timestamps, never `LocalDateTime`
- `@ManyToOne(fetch = FetchType.LAZY)` for all relationships
- Always scope DB queries to current user ID for security

### Running the Backend
- Open in IntelliJ IDEA
- Set env vars in run config: `DB_USERNAME=assetpulse_user`, `DB_PASSWORD=assetpulse_pass`, `JWT_SECRET=<secret>`
- Runs on `http://localhost:8080`

## Frontend
- Framework: React 19 + TypeScript + Vite
- Styling: Tailwind CSS, always dark theme (bg-gray-950 / bg-gray-900 / bg-gray-800)
- Forms: react-hook-form + zod + @hookform/resolvers
- State: Zustand (`src/store/authStore.ts`)
- API calls: axiosClient (`src/api/axiosClient.ts`) — auto-attaches JWT
- Routing: React Router DOM v7
- Data fetching: TanStack Query for GET, direct axiosClient for POST/PUT/DELETE
- Charts: Recharts

### Key Patterns
- Always use `axios.isAxiosError()` for error handling, never `catch (error: any)`
- Always use `z.infer<typeof schema>` for form types
- Never send fields backend doesn't need (e.g. confirmPassword)
- TanStack Query queryKey naming: `['holdings']`, `['portfolio']` etc.

### Running the Frontend
```bash
