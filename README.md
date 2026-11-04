# Familie Munshi Backend

[![Java](https://img.shields.io/badge/Java-26-orange)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-green)](https://spring.io/projects/spring-boot)

REST API for Familie Munshi, built with Spring Boot and PostgreSQL. The backend provides user authentication, branch management, platform roles, permissions, and branch administrator onboarding through an N-tier architecture.

## Features

- User registration and login.
- JWT access tokens and refresh tokens.
- Authenticated user profile and permission-protected user browsing.
- Platform role creation and browsing.
- Permission listing, lookup, and grouping by module.
- Branch creation, listing, lookup, and partial updates.
- Branch administrator provisioning and password-based activation.
- MinIO storage for branch logos with presigned URLs.
- Optional super administrator bootstrap.
- Request validation, centralized exception handling, and paginated responses.
- OpenAPI documentation with Swagger UI.

## Technology Stack

| Component | Technology |
| --- | --- |
| Language | Java 26 |
| Framework | Spring Boot 4.1.0 |
| Build | Maven Wrapper |
| Database | PostgreSQL, Spring Data JPA / Hibernate |
| Security | Spring Security, JJWT 0.12.6 |
| Object storage | MinIO |
| Production cache | Redis |
| API documentation | springdoc OpenAPI 3.0.2 |
| Boilerplate reduction | Lombok |
| Containers | Docker and Docker Compose |

Versions above reflect the repository configuration. Local caching is disabled; the production profile configures Redis.

## Architecture

Requests pass through Spring Security and controllers to services and JPA repositories. Request/response DTOs, service entities, and persistence DAOs have separate responsibilities.

| Package | Responsibility |
| --- | --- |
| `com.familiemunshi.http.controllers` | REST endpoints |
| `com.familiemunshi.http.dtos.requests` | Request validation and DTO conversion |
| `com.familiemunshi.http.dtos.responses` | API response models |
| `com.familiemunshi.service` | Business logic |
| `com.familiemunshi.service.entities` | Service/domain models |
| `com.familiemunshi.service.enums` | Permission codes and role scopes |
| `com.familiemunshi.service.filters` | Search and filter models |
| `com.familiemunshi.jpa` | Persistence layer |
| `com.familiemunshi.common` | Security, configuration, bootstrap, storage, utilities, and exceptions |


## Prerequisites

- JDK 26, matching `pom.xml`.
- Git.
- Docker with a recent Docker Compose version supporting `dockerfile_inline`.
- Internet access to resolve Maven dependencies and build the MinIO service.

The Maven Wrapper is included; a separate Maven installation is optional.

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/rahat9016/familie.munshi.backend.git
cd familie.munshi.backend
chmod +x mvnw
```

### 2. Start PostgreSQL and MinIO

```bash
docker compose up -d postgres minio
```

| Service | Local address |
| --- | --- |
| PostgreSQL | `localhost:5433` by default |
| MinIO API | http://localhost:9000 |
| MinIO console | http://localhost:9001 |

Compose maps PostgreSQL host port **5433** to container port **5432**. The checked-in local Spring configuration uses host port **5432**, so override the datasource URL as shown below.

The Compose MinIO service builds from source on its first run.

### 3. Configure the local application

Set environment variables in the same terminal that will run Spring Boot, or configure equivalent values in your IDE.

```bash
export SPRING_PROFILES_ACTIVE=local
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/familie_munshi_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=9016

export JWT_SECRET="$(openssl rand -hex 32)"
export BRANCH_ADMIN_EMAIL_DOMAIN=accounts.example.com
export BRANCH_ADMIN_COMMON_PASSWORD='REPLACE_WITH_A_LONG_RANDOM_SECRET'
```

The database password above matches the existing Compose development default. If you customize the Compose credentials, update the Spring variables to match. Use separate credentials for deployed environments.

Keep `JWT_SECRET` stable across restarts when existing tokens need to remain valid. The application reads its UTF-8 bytes directly as the HMAC key; use at least 32 bytes.

A Compose `.env` file supplies Compose interpolation values. It is not automatically loaded by a Spring Boot process started directly from your terminal.

### 4. Run the API

```bash
./mvnw spring-boot:run
```

- API base URL: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

The local profile uses Hibernate `ddl-auto: update` to update the database schema.

## Configuration

| Variable | Purpose |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `local` or `prod`; base configuration selects `local` |
| `JWT_SECRET` | Required JWT signing key |
| `BRANCH_ADMIN_EMAIL_DOMAIN` | Domain used for generated branch administrator email addresses |
| `BRANCH_ADMIN_COMMON_PASSWORD` | Password used by the branch administrator provisioning flow |
| `SPRING_DATASOURCE_URL` | Direct Spring datasource URL override, useful locally |
| `SPRING_DATASOURCE_USERNAME` | Direct Spring datasource username override |
| `SPRING_DATASOURCE_PASSWORD` | Direct Spring datasource password override |
| `DATABASE_URL` | PostgreSQL JDBC URL consumed by the production profile |
| `DATABASE_USERNAME` | Production database username |
| `DATABASE_PASSWORD` | Production database password |
| `REDIS_HOST` | Production Redis host; default `redis` |
| `REDIS_PORT` | Production Redis port; default `6379` |
| `REDIS_PASSWORD` | Production Redis password |
| `MINIO_URL` | Production MinIO endpoint |
| `MINIO_ACCESS_KEY` | Production MinIO access key; falls back to `MINIO_ROOT_USER` |
| `MINIO_SECRET_KEY` | Production MinIO secret key; falls back to `MINIO_ROOT_PASSWORD` |
| `MINIO_BUCKET_NAME` | Production bucket; default `familiemunshi-bucket` |
| `PORT` | Production server port; default `8080` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed frontend origins |

The local profile has fixed MinIO settings. To override them locally, use `MINIO_URL`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, and `MINIO_BUCKET_NAME`, which map directly to the MinIO property names.

### Super administrator bootstrap

Bootstrap is disabled by default. To enable it, supply:

```bash
export SUPER_ADMIN_BOOTSTRAP_ENABLED=true
export SUPER_ADMIN_FIRST_NAME=Platform
export SUPER_ADMIN_LAST_NAME=Administrator
export SUPER_ADMIN_EMAIL=admin@gmail.com
export SUPER_ADMIN_PASSWORD='replace-with-a-strong-unique-password'
```

### IDE environment variables

For an IDE environment-variable field that accepts semicolon-separated entries, use:

```text
BRANCH_ADMIN_COMMON_PASSWORD=REPLACE_WITH_A_LONG_RANDOM_SECRET;BRANCH_ADMIN_EMAIL_DOMAIN=accounts.example.com;JWT_SECRET=REPLACE_WITH_A_RANDOM_SECRET_OF_AT_LEAST_32_BYTES;SUPER_ADMIN_BOOTSTRAP_ENABLED=true;SUPER_ADMIN_EMAIL=admin@gmail.com;SUPER_ADMIN_FIRST_NAME=Platform;SUPER_ADMIN_LAST_NAME=Administrator;SUPER_ADMIN_PASSWORD=REPLACE_WITH_A_STRONG_UNIQUE_PASSWORD
```

Replace the secret placeholders in your private run configuration. Keep real passwords and JWT signing keys out of this README and version control.

## API Overview

All paths below are relative to `/api/v1`. Consult Swagger for current request fields and response schemas.

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/auth/signup` | Register a user |
| POST | `/auth/login` | Obtain access and refresh tokens |
| POST | `/auth/refresh-token` | Obtain a new access token |
| POST | `/auth/activate-branch-admin` | Activate a branch administrator |
| GET | `/users/me` | Retrieve the authenticated user's profile |
| GET | `/users` | List users; requires `USER_READ` |
| GET | `/users/{id}` | Retrieve a user; requires `USER_READ` |
| POST | `/system/roles` | Create a platform role |
| GET | `/system/roles` | List roles; requires `ROLE_READ` |
| GET | `/system/roles/{id}` | Retrieve a role |
| GET | `/permissions` | List permissions; requires `PERMISSION_READ` |
| GET | `/permissions/tree` | Group permissions by module; requires `PERMISSION_READ` |
| GET | `/permissions/{id}` | Retrieve a permission; requires `PERMISSION_READ` |
| POST | `/branches` | Create a branch and its administrator |
| GET | `/branches` | List branches; requires `BRANCH_READ` |
| GET | `/branches/{id}` | Retrieve a branch; requires `BRANCH_READ` |
| PATCH | `/branches/{id}` | Update a branch; requires `BRANCH_UPDATE` |

Branch creation and update use `multipart/form-data`. Paginated list endpoints use one-based page numbers, default `page=1`, default `size=20`, and maximum `size=100`.

### Authentication examples

Register:

```bash
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{
    "firstName": "Minhaz",
    "lastName": "Rahman",
    "email": "minhaz@example.com",
    "password": "ExamplePass123!"
  }'
```

Log in:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"minhaz@example.com","password":"ExamplePass123!"}'
```

Use the returned access token:

```bash
curl http://localhost:8080/api/v1/users/me \
  -H 'Authorization: Bearer YOUR_ACCESS_TOKEN'
```

Refresh the access token:

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh-token \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken":"YOUR_REFRESH_TOKEN"}'
```

Access tokens expire after **15 minutes**; refresh tokens expire after **7 days**. The refresh endpoint returns a new access token without rotating the refresh token.

## Build and Test

| Command | Purpose |
| --- | --- |
| `./mvnw spring-boot:run` | Run the application |
| `./mvnw test` | Run the test suite |
| `./mvnw clean verify` | Build and run Maven verification |
| `./mvnw clean package` | Build the executable JAR |
| `java -jar target/backend-0.0.1-SNAPSHOT.jar` | Run the packaged application |

On Windows, use `mvnw.cmd` instead of `./mvnw`. Application-context tests require the referenced infrastructure and configuration.

## Deployment Notes

- The production profile uses Hibernate `ddl-auto: validate`. Provision the schema before startup; production startup does not create it.
- Production caching requires Redis. The current Compose file defines PostgreSQL, MinIO, and the app, but no Redis service.
- The Compose app defaults to the local profile, whose datasource and MinIO settings point to localhost. Container deployment needs explicit Spring property overrides or a correctly configured production profile.
- The Compose app environment block does not forward `JWT_SECRET` or the branch administrator settings. Add those mappings through a Compose override or deployment configuration before running the app container.
- File uploads are limited to 10 MB; the configured extension allowlist is `jpg,jpeg,png,webp,pdf`.
- The current branch creation controller has no permission annotation, and branch routes have permissive filter rules. Review access control before exposing branch creation.
- Branch administrator activation is described as development-only, but its controller has no active profile restriction.
- This README documents the inspected source; startup, build, and deployment commands have not been executed as part of writing it.

## Repository

[rahat9016/familie.munshi.backend](https://github.com/rahat9016/familie.munshi.backend)

Documentation baseline: commit `35c2e52dedf34a76a5bd8490c7419c5fb791a062`.

