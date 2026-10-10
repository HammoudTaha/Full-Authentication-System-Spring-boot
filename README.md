# Spring Boot Authentication Backend

A backend authentication service built with Spring Boot, designed to provide
a structured foundation for user authentication, secure password handling,
OTP verification, and token-based session management.

The project follows a layered architecture that separates HTTP request
handling, business logic, data persistence, and security configuration.

> **Project Status:** In Development

## Overview

This project is an authentication backend designed to support common
authentication workflows, including user registration, login, phone
verification, and password recovery.

It uses Spring Security for request authorization, Spring Data JPA for
database access, PostgreSQL for persistent storage, and JWT-based access
tokens for stateless authentication.

The architecture is designed to support refresh-token rotation, device-aware
session management, and OTP-based verification flows.

Features should be considered complete only when their implementation
and tests are available in the repository.

## Key Features

- User registration and login.
- Password hashing using BCrypt.
- Spring Security integration.
- JWT-based access-token authentication.
- OTP-based verification workflows.
- Password-reset flow using a short-lived reset token.
- PostgreSQL persistence using Spring Data JPA.
- Refresh-token and device-aware session management.
- Centralized API response and exception-handling patterns.
- Environment-based configuration for sensitive settings.

Some features are still under development and may require additional
implementation, testing, or security hardening.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java | Primary programming language |
| Spring Boot | Application framework |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Database persistence |
| PostgreSQL | Relational database |
| JWT | Access-token representation |
| BCrypt | Password hashing |
| Maven | Dependency and build management |
| RabbitMQ | Asynchronous message delivery, where configured |
| Redis | Optional infrastructure for caching or token-related state |

Only technologies configured and used by the current implementation should
be considered active project dependencies.

## Architecture

The application follows a layered architecture.

```text
Client
  |
  v
Controller Layer
  |
  v
Service Layer
  |
  +--------------------+
  |                    |
  v                    v
Repository Layer   Security Components
  |
  v
PostgreSQL
```

### Controller Layer

Responsible for receiving HTTP requests, validating request DTOs,
and returning API responses.

Controllers delegate business operations to the service layer.

### Service Layer

Contains application and business logic, including authentication workflows,
OTP verification, password-reset operations, and session management.

### Repository Layer

Uses Spring Data JPA to access persistent entities and execute database
queries.

### Security Layer

Uses Spring Security to enforce endpoint authorization and validate
authenticated requests.

JWT validation and session-revocation checks must be configured according
to the application's security requirements.

## Authentication Flow

The login workflow is designed around the following sequence:

1. The client submits its credentials.
2. Spring Security authenticates the supplied credentials.
3. The application issues an access token after successful authentication.
4. A refresh token can be issued to support session renewal.
5. Refresh-token state is persisted so that rotation and revocation
   can be enforced.

Access tokens and refresh tokens serve different purposes and should not
be treated interchangeably.

## Password Security

Passwords must never be stored in plaintext.

The application uses BCrypt to hash passwords before persistence.
During authentication, Spring Security verifies the submitted password
against the stored hash.

Password-reset operations should validate the user's recovery proof before
updating the password.

## Password Reset

The planned password-reset workflow consists of three stages:

1. Request an OTP for the registered phone number.
2. Verify the OTP and issue a short-lived reset token.
3. Validate and consume the reset token before updating the password.

Reset tokens should be generated using a cryptographically secure random
generator. Only their hashes should be stored in the database.

The reset token must be single-use, expire after a limited period, and
must not grant access to protected application endpoints.

After a successful password reset, existing refresh-token sessions should
be revoked according to the application's session-revocation policy.

## Database

PostgreSQL is used for persistent application data.

The data model includes user and authentication-related entities, with
additional entities for device and token management where implemented.

Database schema changes should be managed through versioned migrations
when Flyway is configured.

Production deployments should validate the schema rather than relying
on automatic schema updates.

## Configuration

Configure sensitive values through environment variables instead of
committing credentials or secret keys to source control.

Typical configuration values include:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

Additional variables may be required depending on the configured
messaging infrastructure and token-storage implementation.

Never commit real credentials, production secrets, or private keys.

## Getting Started

### Prerequisites

- Java version compatible with the project's `pom.xml`.
- Maven, or the included Maven Wrapper.
- PostgreSQL.
- Any additional services required by the enabled application features.

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/spring-boot-auth.git
cd spring-boot-auth
```

Replace `YOUR_USERNAME` with the actual GitHub username.

### 2. Configure the environment

Set the required environment variables before starting the application.

Example placeholders:

```text
DB_URL=jdbc:postgresql://localhost:5432/auth_demo
DB_USERNAME=your_local_username
DB_PASSWORD=your_local_password
JWT_SECRET=your_local_secret
```

These are configuration examples, not production credentials.

Use a sufficiently strong secret and follow the application's actual
JWT configuration requirements.

### 3. Create the database

Create the PostgreSQL database if it does not already exist:

```sql
CREATE DATABASE auth_demo;
```

Configure the database connection using the environment variables
required by the application.

If Flyway is enabled, allow the configured migrations to create or update
the schema.

### 4. Build the application

On Linux or macOS:

```bash
./mvnw clean verify
```

On Windows:

```powershell
.\mvnw.cmd clean verify
```

If the Maven Wrapper is not included, use an installed Maven distribution:

```bash
mvn clean verify
```

### 5. Run the application

On Linux or macOS:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Ensure that all required environment variables and supporting services
are configured before starting the application.

## API Documentation

The authentication API includes endpoints for workflows such as:

| Endpoint | Purpose |
|---|---|
| `POST /api/auth/register` | Register a user |
| `POST /api/auth/login` | Authenticate a user |
| `POST /api/auth/forgot-password` | Request a password-reset OTP |
| `POST /api/auth/verify-reset-otp` | Verify the password-reset OTP |
| `POST /api/auth/reset-password` | Set a new password using a reset token |

The exact endpoints, request formats, response formats, and authorization
requirements should be verified against the current controller
implementation.

## Security Considerations

- Store passwords using an appropriate password-hashing algorithm.
- Keep secrets out of source control.
- Apply rate limiting to OTP-related endpoints.
- Enforce OTP expiration and attempt limits.
- Store refresh tokens securely and implement rotation and revocation.
- Treat device identifiers and User-Agent values as untrusted metadata.
- Trust forwarded IP headers only when the reverse-proxy configuration
  is controlled and correctly configured.
- Ensure password-reset tokens are short-lived and single-use.
- Test transaction boundaries and concurrent token-consumption behavior.
- Use HTTPS in production.
- Review authentication and authorization behavior before deployment.

This documentation does not constitute a security audit or guarantee
that the implementation is free from vulnerabilities.

## Development Status

The project is under active development.

Planned improvements may include:

- Additional authentication and recovery tests.
- Stronger OTP abuse prevention and rate limiting.
- Comprehensive session-revocation behavior.
- API documentation and integration testing.
- Database migration hardening.
- Production deployment configuration.

## Contributing

Contributions, suggestions, and bug reports are welcome.

Before submitting changes, ensure that the project builds successfully
and that relevant tests pass.

## License

No license has been specified yet.

Add a license before permitting others to reuse, modify, or distribute
the project.