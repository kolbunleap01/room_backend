# API Documentation

Base path: `/api/v1`. Content type: `application/json`. Protected requests require `Authorization: Bearer <accessToken>`.

## Auth

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| POST | `/auth/register` | Public | Register and issue tokens |
| POST | `/auth/login` | Public | Authenticate and issue tokens |
| POST | `/auth/refresh` | Public | Rotate refresh token |
| POST | `/auth/logout` | Public | Revoke a refresh token |

Registration fields: `email`, `password` (12-72 characters), `fullName`, optional `phone`. Login fields: `email`, `password`. Token response contains `accessToken`, `refreshToken`, `tokenType`, and `expiresInSeconds`.

## Profile and Rooms

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| GET/PATCH | `/me` | User | Read/update own profile |
| GET | `/rooms` | Public | Search active rooms |
| GET | `/rooms/{roomId}` | Public | Read active room detail |
| POST | `/rooms` | User | Create listing |
| PATCH | `/rooms/{roomId}` | Owner | Update listing |
| DELETE | `/rooms/{roomId}` | Owner | Deactivate listing |
| GET | `/me/rooms` | User | List own listings |

Room fields are `title`, required `roomType`, `description`, `address`, `city`, optional `district`, positive `monthlyRent`, optional non-negative `deposit`, `bedrooms`, `bathrooms`, optional `areaSqm`, `furnished`, and optional `availableFrom`. `roomType` accepts `PRIVATE_ROOM`, `SHARED_ROOM`, `STUDIO`, or `APARTMENT`, including display forms such as `Private Room` and `Shared Room`; responses use the normalized bounded values.

Search query parameters: `query`, `city`, `minRent`, `maxRent`, `page`, `size`, and `sort`.

## Favorites and Rental Requests

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| GET | `/me/favorites` | User | List saved rooms |
| POST | `/me/favorites/{roomId}` | User | Save a room |
| DELETE | `/me/favorites/{roomId}` | User | Remove a saved room |
| POST | `/rooms/{roomId}/rental-requests` | User | Submit request |
| GET | `/me/rental-requests` | User | List submitted requests |
| GET | `/me/room-requests` | User | List requests for owned rooms |
| POST | `/rental-requests/{id}/approve` | Owner | Approve pending request |
| POST | `/rental-requests/{id}/reject` | Owner | Reject pending request |
| DELETE | `/rental-requests/{id}` | Applicant | Cancel pending request |

Rental request fields are `moveInDate` and optional `message`. Decision endpoints accept an optional `note`.

## Admin and Errors

Admin routes are `/admin/dashboard`, `/admin/users`, `/admin/rooms`, `/admin/rental-requests`, `/admin/audit-logs`, and `/admin/security-events`. Administrators can activate/deactivate users, edit/deactivate/delete any room, and approve/reject/cancel any rental request. They require `ROLE_ADMIN`.

Errors use:

```json
{
  "timestamp": "2026-10-27T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "path": "/api/v1/rooms",
  "fieldErrors": {"title": "must not be blank"}
}
```

Responses are paginated Spring Data `Page` objects containing `content`, `pageable`, `totalElements`, `totalPages`, and related metadata.
