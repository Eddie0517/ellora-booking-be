# AGENTS.md — Working Agreements & Operational Rules for Ellora Backend

> Operational guidance and working agreements for AI agents and human developers working on the `ellora-booking-be` repository.
> When this guide conflicts with `.specify/memory/constitution.md`, the **constitution outranks everything**.

---

## 1. Technical Stack & Architecture

- **Language / Runtime**: Java 17 LTS running inside official Eclipse Temurin Alpine Docker containers (`maven:3.9-eclipse-temurin-17-alpine` for build, `eclipse-temurin:17-jre-alpine` for runtime).
- **Zero Host Java**: Developers and agents MUST NOT require host JDK or Maven installations. All builds, tests, migrations, and servers run containerized via Docker and Docker Compose.
- **Framework**: Spring Boot 3.2.5, Spring Data JPA, Spring Security, SpringDoc OpenAPI 2.3.0, MapStruct 1.5.5, Lombok, Bucket4j, ArchUnit 1.3.0, Flyway.
- **Datastores**:
  - PostgreSQL 17 (relational database; baseline Flyway migration `V1__baseline_schema.sql`).
  - Redis 7 (rate limiting, distributed caching, token blacklist with fail-open resilience).
- **Communication & Tooling**:
  - Mailpit (local SMTP on 1025, web UI on 8025) and Google SMTP adapter.
  - Cloudinary adapter for media and image storage.
  - In-process Spring Events (`ApplicationEventPublisher`) for decoupled event dispatch (Monolith architecture; no RabbitMQ/Kafka required).

---

## 2. Clean Architecture 4-Module Map

The repository is structured as a multi-module Maven project enforcing Clean Architecture Level 2 (L2) with unidirectional inward dependencies:

```text
ellora-booking-be/
├── ellora-domain/                 # Inner core: Pure Java 17 (0 framework dependencies)
│   ├── model/                     # Core domain POJO entities
│   ├── constant/                  # Enums: Role, BookingStatus, SalonStatus
│   ├── repository/                # Repository interfaces (Domain ports)
│   └── exception/                 # DomainException, BusinessRuleException
│
├── ellora-application/            # Use Cases & Application Ports (depends only on ellora-domain)
│   ├── feature/                   # Feature-driven use cases: auth, salon, booking, owner, admin
│   ├── common/dto/                # Request/Response records, PagedResult<T>
│   ├── common/error/              # AppException, ErrorCode mappings
│   └── common/port/               # Port interfaces: TokenProvider, PasswordHasher, MailSender,
│                                  # ImageStorage, Clock, CurrentUser
│
├── ellora-infrastructure/         # Adapters & Persistence (depends on application + domain)
│   ├── persistence/entity/        # JPA @Entity definitions & MapStruct mappers
│   ├── persistence/repository/    # Spring Data JPA repositories & Domain repository adapters
│   ├── adapter/security/          # Hs256TokenProvider, Argon2PasswordHasher
│   ├── adapter/mail/              # GoogleSmtpMailSender / Mailpit adapter
│   └── adapter/storage/           # CloudinaryImageStorage adapter
│
├── ellora-api/                    # Composition Root & Host (depends on application + infrastructure)
│   ├── ElloraApplication.java     # Spring Boot main entrypoint
│   ├── controller/                # REST Controllers (Health, Auth, Salon, Booking)
│   ├── security/                  # SecurityFilterChain, JwtAuthenticationFilter
│   ├── exception/                 # GlobalExceptionHandler (RFC 9457 Problem Details)
│   └── resources/
│       ├── application.yml        # Base configuration
│       ├── application-local.yml  # Local Docker profile
│       └── db/migration/          # Flyway SQL migrations (V1__baseline_schema.sql)
│
└── ellora-architecture-tests/     # Architectural Verification Suite
    └── arch/                      # ArchUnit tests enforcing layer boundaries at build time
```

### Architectural Dependency Rules:
1. `ellora-domain` MUST NOT depend on Spring, JPA, Hibernate, or any external framework.
2. `ellora-application` depends ONLY on `ellora-domain`. Application services/use cases own transaction boundaries via `@Transactional`.
3. `ellora-infrastructure` depends on `ellora-application` and `ellora-domain`, never on `ellora-api`.
4. `ellora-api` controllers MUST NOT touch JPA entities or repositories directly; controllers interact solely with application use cases and DTOs.
5. ArchUnit tests in `ellora-architecture-tests` MUST pass before code is committed.

---

## 3. Quality Gate Ladder (Containerized Docker Commands)

All quality verification commands MUST be executed through Docker to preserve zero host dependencies.

| Gate Stage | Command | Purpose |
|---|---|---|
| **Fast Gate** (Compilation) | `docker compose run --rm backend mvn compile -q` | Fast syntax and type checking across all modules |
| **Unit & Arch Gate** | `docker compose run --rm backend mvn test` | Run all unit tests and ArchUnit boundary enforcement |
| **Full Verify Gate** | `docker compose run --rm backend mvn clean verify` | Complete multi-module build, packaging, and testing |
| **Stack Boot** | `docker compose up --build -d` | Build and start postgres, redis, mailpit, backend |
| **Stack Health Check** | `docker compose ps` / `curl -s http://localhost:8080/healthz` | Verify container status and application liveness |
| **Stack Teardown** | `docker compose down` (or `docker compose down -v` to reset data) | Cleanly stop containers and networks |

---

## 4. Git Policy & Author Identity (CRITICAL)

- **Mandatory Git Author**:
  - Name: `Eddie0517`
  - Email: `khang17122005@gmail.com`
- **STRICT LOCAL COMMIT ONLY**:
  - AI agents and automated workflows MUST NOT run `git push`, create remote tags, or mutate remote repositories.
  - Commits MUST be made locally only.
  - Subagents/workers MUST NOT touch Git at all; Git commit operations belong strictly to the Orchestrator or human developer.
- **Commit Format**:
  - Follow Conventional Commits: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`, `perf:`.
  - Commit message summary and description MUST be written in **English**.

---

## 5. Language & Documentation Policy

1. **Specifications & Specs Directory**:
   - All documents inside `specs/` (e.g. `spec.md`, `plan.md`, `tasks.md`, `frontend-guide.md`) MUST be written in **Vietnamese**.
2. **Environment Files (`.env*`)**:
   - Every comment in any file whose name begins with `.env` (such as `.env`, `.env.example`, `.env.local`) MUST be written in **English**.
3. **Source Code & Technical Docs**:
   - Class names, method names, variables, and code comments in Java MUST be in **English**.
   - Git commit messages MUST be in **English**.
   - Architecture Decision Records (ADRs) and guides in `docs/` are maintained in **English** for standard technical reference.
4. **Developer-Facing Reports**:
   - Reports produced by workers and agents for feature status MUST be written in **Vietnamese** as per repository policy.

---

## 6. API & Contract Standards

- **Error Format**: Strictly RFC 9457 `application/problem+json` (`type`, `title`, `status`, `detail`, `instance`, `traceId`, `invalidParams`).
- **No Envelope Wrapper**: Do NOT use generic wrappers like `ApiResponse<T>` with `{ success, data }`. Return DTOs directly with accurate HTTP status codes.
- **Contract-First Source of Truth**: `contracts/openapi.json` is the single source of truth for the frontend client. Run `scripts/export-openapi.ps1` to re-export when API models change.
- **Pagination**: Use standard `PagedResult<T>` with `items`, `page`, `pageSize`, `totalItems`, and `totalPages`.
