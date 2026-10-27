# SDLC Security Controls

## Design

- Keep threat modeling current for authentication, tenant data ownership, request abuse, and administrator actions.
- Review API changes for authorization at object level, not only endpoint level.
- Treat Flyway migrations as reviewed production code.

## Development

- Never commit `.env`, private keys, JWT values, database passwords, or production URLs.
- Use DTOs and explicit validation at the HTTP boundary.
- Use parameterized repository queries and avoid logging sensitive payloads.
- Add unit tests for authorization, state transitions, token rotation, and validation failures.

## CI/CD Gates

- Run `gradle test --no-daemon` and `gradle clean bootJar --no-daemon`.
- Run dependency vulnerability scanning and secret scanning on every change.
- Build images with pinned base image versions and scan the resulting image.
- Require review for security configuration, migrations, Docker, and deployment changes.

## Operations

- Store secrets in a secret manager or protected runtime environment, never in repository files.
- Restrict database network access and use least-privilege database credentials.
- Monitor authentication failures, rate-limit responses, authorization failures, migration failures, and health status.
- Rotate JWT and database secrets according to an operational schedule; design refresh-token revocation procedures.
- Maintain backup, restore, incident response, and rollback runbooks.
