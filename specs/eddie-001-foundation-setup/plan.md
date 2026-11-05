# Implementation Plan: Foundation Setup for Ellora Backend Refactor

**Branch**: `feat/foundation-setup` | **Feature**: `specs/eddie-001-foundation-setup` | **Date**: 2026-11-06 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/eddie-001-foundation-setup/spec.md`

## Summary

Establish Phase P0 foundation for the Ellora backend refactor:
1. Multi-module Maven Clean Architecture L2 (`ellora-domain`, `ellora-application`, `ellora-infrastructure`, `ellora-api`, `ellora-architecture-tests`).
2. 100% containerized Docker runtime and build environment (zero host Java dependency) using official Eclipse Temurin 17 and Alpine images.
3. Declarative `docker-compose.yml` managing PostgreSQL 17, Redis 7, Mailpit, PGAdmin (`tools`), and the `backend` service.
4. Database baseline schema migration managed by Flyway (`V1__baseline_schema.sql`).
5. Standardized OpenAPI specification (`contracts/openapi.json`) and automated export script for Frontend Agent/Dev handoff.
6. Local cryptographic ES256 keypair generator script (`scripts/jwt-keys.ps1`).
7. Strict local Git commit workflow (`Eddie0517 <khang17122005@gmail.com>`) with zero remote push.

## Technical Context

**Language/Version**: Java 17 LTS (Eclipse Temurin JDK 17 Alpine image inside Docker).

**Primary Dependencies**: Spring Boot 3.2.5, Spring Data JPA, Spring Security, SpringDoc OpenAPI 2.3.0, MapStruct 1.5.5, Lombok, Bucket4j, ArchUnit 1.3.0, Flyway.

**Storage**: PostgreSQL 17 (relational database), Redis 7 (rate limiting, blacklist, cache).

**Testing**: JUnit 5, Mockito, AssertJ, MockMvc, ArchUnit, Testcontainers (PostgreSQL).

**Target Platform**: Docker Engine (Linux containers) on local Windows development machine, deployable to Render/Supabase.

**Project Type**: Monolith Web Service (Clean Architecture L2, 4 Maven modules).

**Performance Goals**: <50ms p95 latency for health and cached endpoints, container boot <20 seconds.

**Constraints**:
- Host requires ZERO Java installation; all builds, migrations, tests, and servers run in Docker.
- Monolith architecture (no microservices, no RabbitMQ, in-process Spring Events only).
- Problem Details (RFC 9457) error format; direct DTO return (no `ApiResponse<T>`).
- Strict Git author identity: `Eddie0517 <khang17122005@gmail.com>` with absolute rule: **NO PUSH TO REMOTE**.

**Scale/Scope**: 21 relational tables, ~92 REST endpoints, 4 modules.

## Constitution Check

*GATE: Passed prior to Phase 0 research. Re-evaluated post Phase 1 design.*

- **Principle I: Service Boundaries & Data Ownership**: PASS. Monolith owns a single PostgreSQL database with Flyway versioning. Cross-module isolation enforced via Clean Architecture layers.
- **Principle II: Synchronous Communication via Contract**: PASS. REST API contracts documented via OpenAPI 3.0 (`contracts/openapi.json`) and RFC 9457 Problem Details.
- **Principle III: Asynchronous Communication via Events**: PASS. Monolith uses internal in-process Spring `ApplicationEventPublisher`. No outbox or external broker needed.
- **Principle IV: Contract-First Interfaces**: PASS. Single source of truth OpenAPI spec maintained in `contracts/openapi.json` with `frontend-guide.md` handoff for Frontend Agent.
- **Principle V: Test-First & Verified Gates**: PASS. ArchUnit gates prevent layer violations; characterization tests ensure zero legacy regression.

## Project Structure

### Documentation (this feature)

```text
specs/eddie-001-foundation-setup/
├── plan.md              # This implementation plan
├── research.md          # Technical research & architectural decisions
├── data-model.md        # 21-table database baseline schema & entity mapping
├── quickstart.md        # Runnable verification & startup guide
├── contracts/           # Problem Details RFC 9457 & pagination specifications
└── checklists/
    └── requirements.md  # 16/16 requirements validation checklist
```

### Source Code (repository root)

```text
ellora-booking-be/
├── pom.xml                               # Parent Maven POM (dependency & module management)
├── Dockerfile                            # Multi-stage Docker build (maven:3.9 -> eclipse-temurin:17)
├── docker-compose.yml                    # postgres, redis, mailpit, pgadmin, backend
├── .env.example                          # Environment template with clear comments
├── .gitignore                            # Ignored secrets, target, build outputs
├── .gitattributes                        # Line endings enforcement (LF)
├── contracts/
│   ├── README.md                         # OpenAPI contract documentation & Angular codegen
│   └── openapi.json                      # Single source of truth OpenAPI 3.0 spec
├── scripts/
│   ├── jwt-keys.ps1                      # ES256 P-256 keypair generator (PowerShell)
│   ├── jwt-keys.sh                       # ES256 P-256 keypair generator (Bash)
│   └── export-openapi.ps1                # Automated OpenAPI dump script
├── ellora-domain/                        # Pure Java 17 (0 framework dependencies)
│   ├── pom.xml
│   └── src/main/java/com/ellora/domain/
│       ├── model/                        # Core domain entities (POJOs)
│       ├── constant/                     # Enums: Role, BookingStatus, SalonStatus
│       ├── repository/                   # Domain repository interfaces
│       └── exception/                    # DomainException, BusinessRuleException
├── ellora-application/                   # Use Cases & Application Ports
│   ├── pom.xml
│   └── src/main/java/com/ellora/application/
│       ├── feature/                      # Features: auth, salon, booking, owner, admin
│       ├── common/dto/                   # PagedResult<T>, Request/Response records
│       ├── common/error/                 # AppException, ProblemDetail adapters
│       └── common/port/                  # Ports: MailSender, ImageStorage, TokenProvider, Clock
├── ellora-infrastructure/                # Persistence & Framework Adapters
│   ├── pom.xml
│   └── src/main/java/com/ellora/infrastructure/
│       ├── persistence/entity/           # 21 JPA @Entity definitions & MapStruct mappers
│       ├── persistence/repository/       # Spring Data Repositories & Domain Adapters
│       ├── adapter/mail/                 # GoogleSmtpMailSender / Mailpit
│       ├── adapter/storage/              # CloudinaryImageStorage
│       └── adapter/security/             # Es256TokenProvider, Argon2PasswordHasher
├── ellora-api/                           # Spring Boot Host & Composition Root
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/ellora/api/
│       │   ├── ElloraApplication.java    # Main Entrypoint
│       │   ├── controller/               # REST Controllers (Health, System)
│       │   ├── security/                 # SecurityFilterChain & TokenFilter
│       │   └── exception/                # GlobalExceptionHandler (RFC 9457)
│       └── resources/
│           ├── application.yml           # Base configurations
│           ├── application-local.yml     # Local Docker environment profile
│           └── db/migration/
│               └── V1__baseline_schema.sql # 21 core tables Flyway baseline
└── ellora-architecture-tests/            # ArchUnit Architectural Verification
    ├── pom.xml
    └── src/test/java/com/ellora/arch/
        ├── DomainLayerRulesTest.java     # Enforce 0 Spring/JPA in domain
        └── ControllerLayerRulesTest.java # Enforce no Entity in controllers
```

## Shared Files & Conflict Hotspots

| Path | Why this feature must touch it | Owner / other feature | Merge point |
|---|---|---|---|
| `contracts/openapi.json` | Baseline OpenAPI schema definition | eddie-001 | Initial baseline |
| `docker-compose.yml` | Base container topology (postgres, redis, mailpit, backend) | eddie-001 | Feature complete |
| `.env.example` | Global environment variables template | eddie-001 | Feature complete |
| `pom.xml` | Parent POM definition for 4 modules | eddie-001 | Feature complete |
| `ellora-api/.../V1__baseline_schema.sql` | Baseline Flyway migration for 21 tables | eddie-001 | Feature complete |

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| **4 Maven Modules instead of 1** | Strict compile-time and packaging enforcement of Clean Architecture (Domain isolation) | Single JAR package-private visibility is easily bypassed by team members over time. |
| **ES256 Token Signing** | Self-managed asymmetric JWT architecture with key rotation support (`kid`) | Symmetric HS256 exposes the signing secret to any verifying service or adapter. |
| **Problem Details RFC 9457** | Global standard error representation consumable by OpenAPI generators and frontend clients | Custom `ApiResponse<T>` envelope adds boilerplate and violates RFC HTTP status standards. |
