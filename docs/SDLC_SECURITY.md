# Room Rental: SDLC and IT Security Plan

This project is a monthly room-rental platform with an Android app, REST API, and PostgreSQL database. It has two roles: ADMIN and USER. ADMIN can perform all USER actions and administer all listings and requests. A USER can search, save, and request rooms, and can also publish and manage their own listings. Contracts and deposits are optional. OTP and property-maintenance management are outside the project scope.

## Phase 1 — Project Initiation and Planning
- Define the room-rental problem, objectives, stakeholders, scope, schedule, and delivery plan.
- Identify personal data (name, email, phone), listing data, and rental-request data.
- Record risks: account compromise, fraudulent listings, unauthorized listing edits, database loss, and service outage.
- Plan access control, secure password storage, backups, and an incident contact before implementation begins.

## Phase 2 — Functional Requirements Definition
- Specify registration, sign-in, room search, favorites, listing creation/editing, and rental-request workflows.
- Define ADMIN and USER permissions and ownership rules.
- Require server-side checks so a USER can modify only their own listings and respond only to requests for their listings.
- Require validation for email, password length, prices, room fields, and request status transitions.
- Do not include OTP in the authentication flow.

## Phase 3 — System Design
- Separate Flutter UI, Spring Boot API, and PostgreSQL storage.
- Use role-based authorization at the API, BCrypt password hashing, and DTOs that never expose password hashes.
- Use HTTPS outside local development. Basic authentication must not be used over an untrusted cleartext network.
- Design database relations for users, rooms, favorites, and rental requests; plan backups and audit records for administrator actions.

## Phase 4 — Development and Acquisition
- Implement the API, Android app, database mapping, and local container setup.
- Keep database and administrator credentials in environment variables; never commit real secrets.
- Validate all inputs on the server, return safe error messages, and keep authorization checks in backend code.
- Review dependencies and changes before merging.

## Phase 5 — Implementation and Assessment
- Configure production secrets and HTTPS before deployment.
- Build the APK with the intended API URL and verify the role and ownership rules.
- Confirm public room browsing exposes only available listing data and that protected endpoints require authentication.
- Back up the database before deployment or schema changes.

## Phase 6 — Operations and Maintenance
- Monitor API availability and authentication failures.
- Apply dependency and security updates, review administrator activity, and maintain tested database backups.
- Respond to reported fraudulent listings and suspected account compromise.
- This phase concerns maintenance of the software service; it does not add property-maintenance features to the app.

## Phase 7 — Disposal and Retirement
- Announce service retirement and export records that must be retained.
- Revoke administrator credentials, database credentials, API access, and deployment secrets.
- Delete or archive personal data according to the project's retention policy.
- Remove deployed services and document the final data-disposal steps.

## Current MVP security notes
- Authentication uses HTTP Basic with BCrypt-protected passwords and is intended for local demonstration over HTTPS-capable deployment.
- The demo setup may use HTTP on an emulator or local network. Do not expose that configuration publicly; use HTTPS for real deployment.
- Admin credentials are seeded only when ADMIN_EMAIL and ADMIN_PASSWORD are supplied through the environment.
- PostgreSQL password is required from DB_PASSWORD; no production credentials belong in source files.
