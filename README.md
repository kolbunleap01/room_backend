# Room Rental Backend

Spring Boot 3 / Java 17 API with PostgreSQL for a monthly-room-rental MVP.

## Features

- USER registration and HTTP Basic sign-in (no OTP); passwords use BCrypt.
- Public browsing of available rooms.
- USER room listings, favorites, and rental requests.
- Listing ownership checks and ADMIN-wide management.
- Admin user/room overview and summary endpoint.
- PostgreSQL via Docker Compose; admin and sample listings are seeded from environment configuration.

## Start locally

1. Copy `.env.example` to `.env`.
2. Set a unique local `DB_PASSWORD`, `ADMIN_EMAIL`, and strong `ADMIN_PASSWORD` in `.env`.
3. Run:

```bash
docker compose up --build
```

API: `http://localhost:8080`

For local Android emulator builds, use API base `http://10.0.2.2:8080`. For a physical phone, use the computer's reachable LAN address. HTTP is for local demonstration only; use HTTPS outside local development.

## Main endpoints

| Method | Path | Access |
|---|---|---|
| POST | `/api/auth/register` | Public; creates USER |
| GET | `/api/auth/me` | Signed in |
| GET | `/api/rooms`, `/api/rooms/{id}` | Public, available rooms |
| GET | `/api/rooms/mine` | Signed in |
| POST/PUT/DELETE | `/api/rooms` | Signed in; USER owns listing, ADMIN may manage all |
| GET/POST/DELETE | `/api/favorites` | Signed in |
| POST | `/api/requests` | Signed in |
| GET | `/api/requests/outbox`, `/api/requests/inbox` | Signed in |
| PATCH | `/api/requests/{id}/status` | Request owner, requester cancellation, or ADMIN |
| GET | `/api/admin/users`, `/api/admin/rooms`, `/api/admin/summary` | ADMIN |

## Security and delivery notes

- Supply secrets only through environment variables; do not commit `.env`.
- HTTP Basic must be protected by HTTPS outside local demonstration.
- This is an MVP: monthly listings, favorites, and requests are implemented. Contract files, deposit collection, invoices, and payment processing are not yet implemented.
- SDLC and phase-by-phase security activities: [docs/SDLC_SECURITY.md](docs/SDLC_SECURITY.md).
