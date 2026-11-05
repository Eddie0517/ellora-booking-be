# Technical Research: Foundation Setup for Ellora Backend Refactor

**Feature**: `specs/eddie-001-foundation-setup` | **Date**: 2026-11-06

---

## 1. Clean Architecture Organization (L2 - 4 Maven Modules)

### Decision
Organize the Java Spring Boot monolith into 4 distinct Maven modules under root `code/backend/` (or root project):
1. `ellora-domain`: Zero framework dependencies (JDK 17 only). Domain entities, value objects, domain exceptions, and repository interfaces.
2. `ellora-application`: Depends only on `ellora-domain`. Contains feature use cases, input/output DTOs, domain event publishers, and port interfaces (`ImageStorage`, `MailSender`, `TokenProvider`, `CurrentUser`).
3. `ellora-infrastructure`: Depends on `ellora-application` and `ellora-domain`. Implements ports and repository interfaces using Spring Data JPA, Redis, Cloudinary SDK, and Google SMTP.
4. `ellora-api`: Deployable composition root (`@SpringBootApplication`). Depends on `ellora-application` and `ellora-infrastructure`. Hosts REST controllers, Spring Security filter chains, Problem Details exception advice, and OpenAPI configurations.
5. `ellora-architecture-tests`: Standalone ArchUnit test module asserting layer boundary rules at build time.

### Rationale
- Matches the architecture proven in `longevia-be` (Domain, Application, Infrastructure, Api) while retaining the simplicity of a single deployable Spring Boot monolith.
- Prevents database entities and framework concerns from polluting business logic.
- ArchUnit enforces rules automatically on every build: `domain` cannot import Spring or JPA; `application` cannot import `infrastructure`; `api` controllers cannot touch `@Entity` or JPA repositories directly.

### Alternatives Considered
- **Package-by-feature in a single JAR**: Easy to set up initially, but package-private visibility in Java is easily bypassed by developers, leading to architectural erosion over time.
- **Microservices decomposition (3-4 separate services)**: Overkill for Ellora's current scale (monolith PostgreSQL, ~100 endpoints). Introduces distributed transaction overhead, outbox messaging, and operational complexity without immediate business justification.

---

## 2. 100% Containerized Toolchain & Runtime (Zero Host Java Dependency)

### Decision
Do not install JDK or Maven on the host machine. Instead:
- Use official Docker images (`maven:3.9-eclipse-temurin-17-alpine` for building/testing and `eclipse-temurin:17-jre-alpine` for lightweight runtime).
- Provide a multi-stage `Dockerfile`:
  - Stage 1 (`build`): Copies `pom.xml`, caches dependencies, compiles all modules, runs unit & ArchUnit tests.
  - Stage 2 (`runtime`): Copies the compiled deployable fat JAR from `ellora-api/target/*.jar` and runs with non-root user.
- Configure `docker-compose.yml` to run the entire backend stack together: `postgres`, `redis`, `mailpit`, `pgadmin` (profile `tools`), and `backend`.

### Rationale
- Completely eliminates "works on my machine" issues and host PATH / environment variable pollution.
- Works consistently across Windows, macOS, Linux, and CI/CD pipelines with only Docker Engine required.
- Easy hot-reloading in dev using volume mounts (`target` or code mounting if needed) while keeping the environment isolated.

### Alternatives Considered
- **Local Host JDK 17 Installation**: Requires manual package manager commands (`winget install EclipseAdoptium.Temurin.17.JDK`), manual PATH updates, and machine restarts. User explicitly requested running 100% in Docker.

---

## 3. Database Schema Baseline & Migration (Flyway)

### Decision
- Consolidate all 13 legacy SQL migration scripts from `src/main/resources/db/*.sql` into a single, clean baseline migration: `ellora-api/src/main/resources/db/migration/V1__baseline_schema.sql`.
- Configure Spring Boot JPA with `spring.jpa.hibernate.ddl-auto=none` (or `validate` in dev).
- Use Flyway directly managed within `ellora-api` on container startup.
- Document direct connection (port 5432) requirement for Supabase in staging/production to avoid PgBouncer transaction-mode pooler issues.

### Rationale
- Replaces unversioned, brittle ORM auto-generation with deterministic, auditable SQL versioning.
- Ensures identical database schema across local development, CI test containers, and remote production databases.

### Alternatives Considered
- **Liquibase**: More verbose XML/YAML changelogs, unnecessary complexity when Flyway handles standard PostgreSQL SQL scripts directly and transparently.
- **Hibernate `ddl-auto: update`**: Unsafe for production; silently alters or drops columns and indexes without version tracking.

---

## 4. OpenAPI Contract & Frontend Agent Handoff Mechanism

### Decision
- Include `springdoc-openapi-starter-webmvc-ui` (v2.3.0+) in `ellora-api`.
- Provide an automated export script (`scripts/export-openapi.ps1` and `scripts/export-openapi.sh`) that runs a lightweight context test to export the active OpenAPI spec directly to `contracts/openapi.json`.
- Adopt the `frontend-guide.md` pattern from `longevia-be`: For each feature/phase, backend generates a structured guide summarizing:
  - Added / modified endpoints
  - Client-facing request / response DTOs
  - Enums and status codes
  - RFC 9457 Problem Details error codes
- Frontend developers or AI agents can feed `contracts/openapi.json` into `@openapitools/openapi-generator-cli` or `ng-openapi-gen` to generate Angular models and services automatically.

### Rationale
- Bridges the gap between separate frontend and backend repositories without manual documentation drift.
- Follows the OpenAI OpenAPI repository pattern (`openai/openai-openapi`) where the JSON/YAML specification is the single contract of truth.

### Alternatives Considered
- **Manual Postman collections**: Difficult to keep synchronized; no automatic TypeScript type generation for frontend.
- **Code-first contract sharing via git submodule**: Requires npm/maven publishing or complex cross-repo git submodules. A committed `contracts/openapi.json` is universal and tool-agnostic.

---

## 5. ES256 Keypair Generation Script

### Decision
Provide `scripts/jwt-keys.ps1` (Windows PowerShell) and `scripts/jwt-keys.sh` (Linux/bash) using Java/OpenSSL to:
1. Generate an ECDSA P-256 (prime256v1) key pair.
2. Output one-line base64-encoded PKCS#8 PEM for `JWT_PRIVATE_KEY` and SPKI PEM for `JWT_PUBLIC_KEY` directly to standard output.
3. Never write private key files to disk.

### Rationale
- ES256 tokens are compact, fast to sign/verify, and comply with ADR 001/002.
- Printing base64 to terminal allows developers to copy-paste directly into local `.env` without accidentally committing secret PEM files to Git.

### Alternatives Considered
- **HMAC-SHA256 (HS256)**: Shared symmetric secret; if multiple services read tokens later, private key is exposed to verifiers. ES256 allows verifiers to hold only the public key.
- **RS256**: Key sizes are significantly larger (2048+ bits), generating larger JWT headers/payloads with higher CPU overhead than ECC P-256.

---

## 6. Characterization Test Strategy

### Decision
- Implement integration tests using JUnit 5 + MockMvc/WebTestClient targeting the 8 critical business flows:
  1. User Registration (with email verification request)
  2. User Login & Token issuance
  3. Salon Discovery & Public Search
  4. Booking Creation (authenticated customer)
  5. Guest Booking Creation (unauthenticated customer)
  6. Booking Status Transition (CONFIRMED -> COMPLETED / CANCELLED)
  7. Salon Review Submission
  8. Owner Booking Management
- Execute these tests against the PostgreSQL container before migrating logic into the new Clean Architecture structure.

### Rationale
- Guarantees zero regression and behavioral equivalence between the legacy code and refactored use cases.
