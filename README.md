# Room Rental MVP Backend

Spring Boot 3 / Java 17 / Gradle backend for the Room Rental MVP. The service exposes JSON REST APIs under `/api/v1`, uses PostgreSQL with Flyway migrations, and returns DTOs rather than persistence entities.

## Stack

- Spring Boot 3.3, Spring Web, Validation, Security, Data JPA, Actuator
- PostgreSQL 16 and Flyway
- JWT access tokens with short expiry
- Opaque, SHA-256-hashed, rotating refresh tokens
- BCrypt password hashing
- Gradle and Java 17

## Local Development

1. Install Java 17+ and Gradle 8+.
2. Start PostgreSQL, or use the provided Docker Compose stack.
3. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and a generated `JWT_SECRET_BASE64`.
4. Run `gradle clean bootJar --no-daemon`.
5. Run the application with `gradle bootRun`.

The default local profile is `dev`. The application requires a JWT secret at startup; no development secret is committed.

## Docker Compose

```bash
cp .env.example .env
# Replace every replace-with value in .env.
docker compose up --build
```

The database is attached only to the internal `private-db` network and uses the named `room-rental-postgres` volume. Nginx exposes the API at `http://localhost:8080`.

## API Summary

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET/PATCH /api/v1/me`
- `GET /api/v1/rooms` and `GET /api/v1/rooms/{roomId}`
- `POST/PATCH/DELETE /api/v1/rooms[/{roomId}]` for authenticated users and owners
- `GET /api/v1/me/rooms`
- `GET/POST/DELETE /api/v1/me/favorites[/{roomId}]`
- `POST /api/v1/rooms/{roomId}/rental-requests`
- `GET /api/v1/me/rental-requests`
- `GET /api/v1/me/room-requests`
- `POST /api/v1/rental-requests/{requestId}/approve|reject`
- `DELETE /api/v1/rental-requests/{requestId}`
- `GET /api/v1/admin/dashboard`, `/users`, `/rooms`, `/rental-requests`, `/audit-logs`, `/security-events`
- `PATCH/POST /api/v1/admin/users/{userId}/status|activate|deactivate`
- `PATCH /api/v1/admin/rooms/{roomId}` and `POST/DELETE /api/v1/admin/rooms/{roomId}/deactivate|delete`
- `POST /api/v1/admin/rental-requests/{requestId}/approve|reject|cancel`

Use `Authorization: Bearer <accessToken>` for protected endpoints. Pagination uses Spring Data parameters such as `?page=0&size=20&sort=createdAt,desc`.

## Verification

```bash
gradle clean bootJar --no-daemon
gradle test --no-daemon
```

See `docs/` for requirements, architecture, data model, endpoint contracts, security requirements, SDLC controls, and deployment operations.
