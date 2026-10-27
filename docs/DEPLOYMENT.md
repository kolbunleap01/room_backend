# Deployment

## Configuration

Required production values:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET_BASE64` containing at least 32 random decoded bytes
- `CORS_ALLOWED_ORIGINS`
- `SPRING_PROFILES_ACTIVE=prod`

Use a secret manager or protected deployment environment. `.env.example` contains placeholders only.

## Docker Compose

```bash
cp .env.example .env
# Replace placeholders, then:
docker compose up -d --build
docker compose ps
curl -f http://localhost:8080/actuator/health
```

The Compose database is private and persistent through the named volume. Nginx is the public entrypoint. For real production, terminate TLS at a managed load balancer or configure certificates and security headers at the reverse proxy.

## Release Verification

1. Build and test the exact commit.
2. Run Flyway against a staging database and verify health/readiness.
3. Exercise registration, login, refresh rotation, room ownership, and admin authorization smoke tests.
4. Check logs and metrics for migration, database, authentication, and rate-limit errors.

## Rollback

## First administrator

Register your own account in the mobile app, then an operator with database access
can grant that existing account the ADMIN role. There is no public role-promotion
endpoint and no default admin password.

```bash
docker compose exec db sh -c 'exec psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

In psql, replace the example email with the account you registered:

```sql
UPDATE users SET role = 'ADMIN', updated_at = now()
WHERE email = 'your-registered-email@example.com';
```

Sign out and sign in again. Only a trusted database operator should perform this step.

## Existing databases

The new UUID schema and Flyway migrations are intended for a fresh database.
Back up an older MVP database and plan a data migration before reusing it. Do not
delete volumes or baseline an incompatible schema to bypass validation.

## Rollback procedure

- Application rollback: redeploy the previously built immutable image/tag.
- Database rollback: do not edit an applied migration; use a forward corrective migration after review.
- Verify `/actuator/health`, login, room search, and database connectivity after rollback.
- Preserve audit events and database backups before destructive operational changes.

## Limitations

The included Compose stack is suitable for local/staging use and a small single-host deployment. It does not provide TLS certificates, external secret management, automated backups, centralized logs, Redis rate limiting, or an orchestrator-level rolling deployment.
