# AssetPulse — Investment Portfolio Tracker

A personal portfolio tracker for stocks, ETFs, and crypto across US, HK, and A-share markets. You manually manage your holdings, and the backend pulls live prices from [Futu OpenAPI](https://openapi.futunn.com/futu-api-doc/en/) every 5 minutes to keep your P&L current.

---

## Screenshots

<!-- Add your screenshots here -->

---

## Features

- **JWT auth** — register/login, all data is scoped to your account
- **Multi-market holdings** — supports US (AAPL, VOO), HK (00700), and A-share (600519) markets
- **Live prices via Futu OpenAPI** — prices refresh automatically every 5 minutes; you need Futu/Moomoo's OpenD gateway running locally
- **Portfolio dashboard** — total value, total gain/loss, and overall return at a glance
- **Charts** — donut chart for allocation breakdown, bar chart for per-holding gain/loss
- **Holdings table** — quantity, avg cost, current price, market value, gain/loss with percentage

---

## Tech Stack

| Layer | Stack |
|---|---|
| Backend | Java 21, Spring Boot 4.0.5, Spring Security, JPA/Hibernate |
| Database | PostgreSQL 16 (Docker) |
| Auth | JWT (jjwt 0.12) |
| Market data | Futu OpenAPI Java SDK |
| Frontend | React 19, TypeScript, Vite |
| Styling | Tailwind CSS (dark theme only) |
| Forms | react-hook-form + zod |
| Data fetching | TanStack Query v5 |
| Charts | Recharts |
| State | Zustand |
| API client | Axios |

---

## Prerequisites

- Java 21
- Node.js 22+
- Docker (for PostgreSQL)
- [Futu OpenD](https://openapi.futunn.com/futu-api-doc/en/intro/intro.html) — the Futu desktop gateway app, running locally on port 11111

---

## Getting Started

### 1. Start the database

```bash
docker-compose up -d
```

This spins up PostgreSQL 16 on port `5432` and pgAdmin on `http://localhost:5050`.

### 2. Run the backend

Open the `backend/` folder in IntelliJ IDEA. In your run configuration, set these environment variables:

```
DB_USERNAME=assetpulse_user
DB_PASSWORD=assetpulse_pass
JWT_SECRET=<any-secret-at-least-32-chars>
```

If your OpenD gateway is running somewhere other than the default, also set:

```
FUTU_HOST=<host>
FUTU_PORT=<port>
```

The backend starts at `http://localhost:8080`. Hibernate will create/update the schema automatically on first run.

### 3. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

The dev server starts at `http://localhost:5173`.

---

## Futu OpenD Setup

Price data comes from the Futu/Moomoo brokerage API. You'll need:

1. A Futu/Moomoo account (free to open)
2. [OpenD](https://openapi.futunn.com/futu-api-doc/en/quick/opend-base.html) installed and running — this is a local gateway app that the backend connects to over TCP

By default the backend expects OpenD at `172.22.32.1:11111` (the WSL2 host IP). Adjust `FUTU_HOST` and `FUTU_PORT` to match your setup.

Without OpenD running, price refreshes will silently fail but the rest of the app still works fine — you can manually enter prices when adding holdings.

---

## API Reference

The backend exposes a Swagger UI at `http://localhost:8080/swagger-ui.html` when running locally.

Key endpoints:

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Create account |
| `POST` | `/api/auth/login` | Get JWT token |
| `GET` | `/api/holdings` | List your holdings |
| `POST` | `/api/holdings` | Add a holding |
| `DELETE` | `/api/holdings/{id}` | Remove a holding |
| `GET` | `/api/market/price` | Fetch live price for a symbol |

---

## Project Structure

```
AssetPulse/
├── backend/
│   └── src/main/java/com/assetpulse/backend/
│       ├── auth/          # Register, login, JWT issuance
│       ├── holding/       # Holdings CRUD
│       ├── market/        # Futu client, price refresh scheduler
│       └── common/        # User entity, security config, exception handling
├── frontend/
│   └── src/
│       ├── api/           # Axios client + holdings API calls
│       ├── components/    # AppLayout, PortfolioCharts, Toast
│       ├── pages/         # LoginPage, RegisterPage, DashboardPage
│       ├── store/         # Zustand stores (auth, toast)
│       └── types/         # Shared TypeScript types
└── docker-compose.yml
```

---

## Notes

- Prices are stored as `BigDecimal` — no floating-point rounding surprises
- All timestamps use `Instant` (UTC), no `LocalDateTime`
- DB queries are always scoped to the authenticated user; you can't see someone else's holdings
- The schema auto-updates on startup (`ddl-auto: update`) — fine for local dev, change to `validate` for anything production-facing
