# Functional Requirements

## Authentication and Profile

- FR-AUTH-01: A guest can register with unique email, password, full name, and optional phone.
- FR-AUTH-02: A registered user can log in and receive an access token and refresh token.
- FR-AUTH-03: The service returns the same invalid-credential message for unknown and known emails.
- FR-AUTH-04: Failed login attempts are counted and the account is temporarily locked after the configured threshold.
- FR-AUTH-05: A valid refresh token produces a new access token and a replacement refresh token; the old token is revoked.
- FR-AUTH-06: A user can revoke the presented refresh token through logout.
- FR-USER-01: An authenticated user can retrieve and update their own profile.

## Rooms

- FR-ROOM-01: Guests can search active rooms by text, city, and rent range with pagination.
- FR-ROOM-02: Guests can retrieve an active room detail DTO.
- FR-ROOM-03: An authenticated user can create a room listing.
- FR-ROOM-04: Only the listing owner can update or deactivate that listing.
- FR-ROOM-05: An authenticated user can list their own rooms.
- FR-ROOM-06: An administrator can inspect room listings and deactivate any listing.

## Favorites

- FR-FAV-01: A user can add an active room to favorites once.
- FR-FAV-02: A user can list their favorites with pagination.
- FR-FAV-03: A user can remove their own favorite; repeating removal is idempotent.

## Rental Requests

- FR-REQ-01: A user can submit one pending rental request per room.
- FR-REQ-02: A room owner cannot request their own room.
- FR-REQ-03: Applicants can list and cancel their own pending requests.
- FR-REQ-04: Owners can list requests for their rooms.
- FR-REQ-05: Only the owning user can approve or reject a pending request.
- FR-REQ-06: Request state transitions are limited to pending to approved, rejected, or cancelled.

## Administration and Operations

- FR-ADMIN-01: Administrators can view user, room, audit, and aggregate dashboard data.
- FR-ADMIN-02: Security-sensitive authentication, authorization, and write actions create audit events.
- FR-OPS-01: Health and readiness endpoints are available through Actuator.
- FR-OPS-02: Database changes are applied by Flyway and Hibernate validates the schema at startup.
