# Project Analysis

## Scope

This repository contains only the Room Rental MVP backend. The frontend directory is intentionally not modified. The backend is a stateless Spring Boot REST service with PostgreSQL persistence and Flyway-managed schema evolution.

## MVP Actors

- Guest: browse active rooms and room details.
- Registered user: maintain a profile, create and manage owned room listings, favorite rooms, and submit or cancel rental requests.
- Room owner: review pending requests for owned rooms and approve or reject them.
- Administrator: view operational metrics, users, rooms, and audit events; deactivate listings.

## Key Decisions

- UUID identifiers avoid predictable sequential identifiers at the API boundary.
- Access tokens are short-lived JWTs; refresh tokens are opaque, stored only as SHA-256 hashes, and rotated on every refresh.
- Deletion is modeled as room deactivation to preserve request/favorite/audit history.
- Server-side authorization checks room owner and applicant identity in services, not only in controllers.
- Validation, rate limiting, generic authentication errors, audit events, and security headers are included in the MVP baseline.

## Assumptions and Deliberate Limits

- Room photos, messaging, payments, leases, email verification, password reset, and social login are outside this MVP.
- Rate limiting is an in-memory single-instance limiter. A Redis-backed limiter is needed for horizontally scaled deployments.
- Audit data is append-oriented but there is no separate immutable external archive in this repository.
- Admin provisioning is operational/database-managed; public registration always creates a `USER` role.
