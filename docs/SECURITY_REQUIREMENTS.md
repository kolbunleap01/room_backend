# Security Requirements

- Passwords must never be stored or logged in plaintext; BCrypt is used with cost 12.
- JWT signing requires a configured base64 secret of at least 32 decoded bytes. Secrets are environment-provided.
- Access tokens should be short-lived. Refresh tokens are random opaque values, stored only as SHA-256 hashes, and rotated on use.
- Invalid login and unknown-user failures return the same generic message.
- Login attempts are limited per account and login requests are rate-limited per source IP.
- API requests are rate-limited per source IP. Replace the in-memory limiter with a shared limiter before horizontal scaling.
- All protected routes require authentication; admin routes require `ROLE_ADMIN`.
- Room, favorite, and rental-request operations enforce object ownership in the service layer.
- Request payloads are validated and persistence schema has database constraints.
- CORS origins are environment-configured and credentials are disabled for bearer-token APIs.
- Stack traces, SQL details, and raw exception messages are not returned by the API.
- Audit events record failed/successful auth, refresh lifecycle, account and business mutations, and admin moderation.
- Database access is isolated from the public network in Docker Compose.
- Production deployments must use TLS at the reverse proxy, secret management, dependency scanning, and restricted database credentials.
