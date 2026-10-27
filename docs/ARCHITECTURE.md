# Architecture

## Runtime Shape

```text
Browser / frontend
        |
      Nginx
        |
Spring Boot REST API
  |      |       |
 JWT   Services  Actuator
  |      |       |
       JPA     Audit events
        |
 PostgreSQL (private network)
```

The application is organized by feature package: `common`, `auth`, `user`, `room`, `favorite`, `rentalrequest`, and `admin`. Controllers accept validated request DTOs and return response DTOs. Services contain business rules and authorization checks. Repositories isolate JPA access.

## Security Flow

1. Authentication endpoints issue a short-lived signed JWT and an opaque refresh token.
2. `JwtAuthenticationFilter` validates the JWT signature and expiry, then resolves the current user from the database.
3. Spring Security protects all non-public API routes and applies the `ADMIN` role to admin routes.
4. Service methods enforce ownership for room and rental-request mutations.
5. Audit events are persisted for authentication and material state changes.

## Consistency and Failure Handling

- Each command service is transactional.
- PostgreSQL foreign keys and check constraints protect core invariants.
- A unique partial index prevents duplicate pending requests.
- Global exception handling emits a stable JSON error shape without stack traces or raw persistence details.
- Database health is included in Actuator health.

## Scaling Notes

The API is stateless except for PostgreSQL-backed refresh-token and audit state, so multiple API replicas can run behind a load balancer. Move the in-memory `RateLimitFilter` to Redis or a gateway limiter before multi-replica production use.
