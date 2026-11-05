# Tasks: Foundation Setup for Ellora Backend Refactor

**Input**: Design documents from `specs/eddie-001-foundation-setup/` (`spec.md`, `plan.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`)

**Prerequisites**: `spec.md`, `plan.md`, `research.md`, `data-model.md`, `contracts/`

**Organization**: Tasks are grouped by phase and user story to enable independent implementation and testing.

## Format: `- [ ] [TaskID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story identifier (`[US1]`, `[US2]`, `[US3]`, `[US4]`, `[US5]`)
- Exact file paths are specified in all task descriptions.

---

## Phase 1: Setup (Repository Infrastructure & Environment)

**Purpose**: Initialize repository-level hygiene, Git rules, environment templates, and secrets handling.

- [x] T001 Create git ignore file in `.gitignore` ignoring `.env`, `target/`, `*.pem`, and build outputs
- [x] T002 [P] Create git attributes file in `.gitattributes` enforcing standard LF line endings (`* text=auto eol=lf`)
- [x] T003 [P] Create environment template in `.env.example` documenting all PostgreSQL, Redis, Mailpit, JWT, Cloudinary, and Google SMTP properties
- [x] T004 [P] Create active local environment file in `.env` populated with local development defaults and placeholders for real external keys
- [x] T005 [P] Create HS256 secret generator script for Windows PowerShell in `scripts/jwt-keys.ps1`
- [x] T006 [P] Create HS256 secret generator script for Linux/macOS in `scripts/jwt-keys.sh`

---

## Phase 2: Foundational (Docker Stack & Containerized Build Engine)

**Purpose**: Establish the zero-host-Java containerized runtime and compose orchestration that blocks all subsequent application work.

- [ ] T007 Create declarative container stack in `docker-compose.yml` defining `postgres`, `redis`, `mailpit`, `pgadmin` (`tools` profile), and `backend` services
- [ ] T008 Create multi-stage container build file in `Dockerfile` using `maven:3.9-eclipse-temurin-17-alpine` (build stage) and `eclipse-temurin:17-jre-alpine` (runtime stage)
- [ ] T009 Create parent Maven POM in `pom.xml` managing packaging, plugin versions (compiler, surefire, spotless), and dependencies (Spring Boot 3.2.5, Lombok, MapStruct, ArchUnit, SpringDoc)

**Checkpoint**: Core container stack and parent build definition are verified runnable via `docker compose config`.

---

## Phase 3: User Story 1 - Local Development Infrastructure & Environment Readiness (Priority: P1) 🎯 MVP

**Goal**: Enable a developer to boot all supporting infrastructure (PostgreSQL 17, Redis 7, Mailpit) and verify healthy local services without external cloud dependencies.

**Independent Test**: Run `docker compose up -d postgres redis mailpit` and verify all 3 services report healthy status on ports 5432, 6379, and 1025/8025.

### Implementation for User Story 1

- [ ] T010 [P] [US1] Configure PostgreSQL 17 service healthcheck and volume persistence in `docker-compose.yml`
- [ ] T011 [P] [US1] Configure Redis 7 Alpine service with allkeys-lru memory policy and healthcheck in `docker-compose.yml`
- [ ] T012 [P] [US1] Configure Mailpit SMTP catcher and Web UI service on ports 1025 and 8025 in `docker-compose.yml`
- [ ] T013 [US1] Execute and verify local container infrastructure startup and healthcheck via `docker compose ps`

**Checkpoint**: Infrastructure services are healthy; local database, cache, and mail capture are functional.

---

## Phase 4: User Story 2 - Clean Architecture Boundaries & Build Enforcement (Priority: P1)

**Goal**: Structure the backend into 4 Maven modules obeying strict inward dependencies, enforced by ArchUnit rules to prevent domain pollution.

**Independent Test**: Execute `docker compose run --rm backend mvn clean test` and verify that all 4 modules compile and ArchUnit layer verification passes with zero violations.

### Implementation for User Story 2

- [ ] T014 [P] [US2] Create domain module descriptor in `ellora-domain/pom.xml` with zero framework dependencies (JDK 17 only)
- [ ] T015 [P] [US2] Create domain base packages and markers in `ellora-domain/src/main/java/com/ellora/domain/model/` and `ellora-domain/src/main/java/com/ellora/domain/exception/DomainException.java`
- [ ] T016 [P] [US2] Create application module descriptor in `ellora-application/pom.xml` depending only on `ellora-domain`
- [ ] T017 [P] [US2] Create application common DTOs in `ellora-application/src/main/java/com/ellora/application/common/dto/PagedResult.java`
- [ ] T018 [P] [US2] Create application port interfaces in `ellora-application/src/main/java/com/ellora/application/common/port/MailSender.java` and `ImageStorage.java`
- [ ] T019 [P] [US2] Create infrastructure module descriptor in `ellora-infrastructure/pom.xml` depending on `ellora-application`, `ellora-domain`, Spring Data JPA, and Redis
- [ ] T020 [P] [US2] Create API module descriptor in `ellora-api/pom.xml` depending on `ellora-application`, `ellora-infrastructure`, Spring Boot Web, and SpringDoc
- [ ] T021 [US2] Create Spring Boot application entrypoint in `ellora-api/src/main/java/com/ellora/api/ElloraApplication.java`
- [ ] T022 [P] [US2] Create architecture test module descriptor in `ellora-architecture-tests/pom.xml` with ArchUnit dependencies
- [ ] T023 [US2] Implement domain layer architectural rules test in `ellora-architecture-tests/src/test/java/com/ellora/arch/DomainLayerRulesTest.java`
- [ ] T024 [US2] Implement controller layer architectural rules test in `ellora-architecture-tests/src/test/java/com/ellora/arch/ControllerLayerRulesTest.java`

**Checkpoint**: 4-module Clean Architecture skeleton compiles and ArchUnit passes 100% inside container.

---

## Phase 5: User Story 3 - Database Migration Baseline (Priority: P2)

**Goal**: Establish an immutable Flyway schema baseline consolidating all 21 existing tables, foreign keys, and indexes without ORM auto-generation.

**Independent Test**: Spin up a fresh database container and run Flyway migration, verifying 21 core tables exist via `\dt`.

### Implementation for User Story 3

- [ ] T025 [US3] Consolidate 13 legacy SQL scripts into Flyway baseline migration in `ellora-api/src/main/resources/db/migration/V1__baseline_schema.sql`
- [ ] T026 [P] [US3] Create base application configuration in `ellora-api/src/main/resources/application.yml` setting `spring.jpa.hibernate.ddl-auto=none` and enabling Flyway
- [ ] T027 [P] [US3] Create local profile configuration in `ellora-api/src/main/resources/application-local.yml` binding container database, redis, and mailpit URLs
- [ ] T028 [US3] Execute Flyway migration verification against PostgreSQL container and assert 21 tables created

**Checkpoint**: Database baseline is versioned and applied deterministically on startup.

---

## Phase 6: User Story 4 - Standardized OpenAPI Contract Export & Frontend Handoff (Priority: P2)

**Goal**: Provide a machine-readable OpenAPI specification (`contracts/openapi.json`) and RFC 9457 Problem Details error handler to ensure zero frontend-backend contract drift.

**Independent Test**: Run `scripts/export-openapi.ps1` and verify that `contracts/openapi.json` is exported with endpoints and Problem Details schema.

### Implementation for User Story 4

- [ ] T029 [P] [US4] Configure SpringDoc OpenAPI bean and security scheme in `ellora-api/src/main/java/com/ellora/api/config/OpenApiConfig.java`
- [ ] T030 [P] [US4] Implement global RFC 9457 Problem Details exception handler in `ellora-api/src/main/java/com/ellora/api/exception/GlobalExceptionHandler.java`
- [ ] T031 [P] [US4] Implement system liveness controller in `ellora-api/src/main/java/com/ellora/api/controller/HealthController.java` exposing `GET /healthz`
- [ ] T032 [US4] Create automated OpenAPI specification export script in `scripts/export-openapi.ps1`
- [ ] T033 [US4] Export and synchronize live schema into root `contracts/openapi.json`
- [ ] T034 [US4] Create frontend handoff guide in `specs/eddie-001-foundation-setup/frontend-guide.md` based on `.specify/templates/frontend-guide-template.md`

**Checkpoint**: OpenAPI specification and frontend guide are up to date and verifiable.

---

## Phase 7: User Story 5 - Legacy Characterization Test Suite (Priority: P3)

**Goal**: Establish automated characterization tests covering the 8 legacy core flows against the baseline database schema to prevent behavioral regression.

**Independent Test**: Execute characterization tests inside container and confirm 8/8 core business flows pass without error.

### Implementation for User Story 5

- [ ] T035 [P] [US5] Create base characterization test harness with containerized PostgreSQL in `ellora-api/src/test/java/com/ellora/api/characterization/BaseCharacterizationTest.java`
- [ ] T036 [P] [US5] Implement characterization test for User Registration flow in `ellora-api/src/test/java/com/ellora/api/characterization/AuthRegistrationCharacterizationTest.java`
- [ ] T037 [P] [US5] Implement characterization test for User Login flow in `ellora-api/src/test/java/com/ellora/api/characterization/AuthLoginCharacterizationTest.java`
- [ ] T038 [P] [US5] Implement characterization test for Salon Search flow in `ellora-api/src/test/java/com/ellora/api/characterization/SalonSearchCharacterizationTest.java`
- [ ] T039 [P] [US5] Implement characterization test for Booking Creation flow in `ellora-api/src/test/java/com/ellora/api/characterization/BookingCreationCharacterizationTest.java`
- [ ] T040 [P] [US5] Implement characterization test for Guest Booking flow in `ellora-api/src/test/java/com/ellora/api/characterization/GuestBookingCharacterizationTest.java`
- [ ] T041 [P] [US5] Implement characterization test for Booking Status Transition flow in `ellora-api/src/test/java/com/ellora/api/characterization/BookingStatusTransitionCharacterizationTest.java`
- [ ] T042 [P] [US5] Implement characterization test for Salon Review Submission in `ellora-api/src/test/java/com/ellora/api/characterization/SalonReviewCharacterizationTest.java`
- [ ] T043 [P] [US5] Implement characterization test for Owner Booking Management in `ellora-api/src/test/java/com/ellora/api/characterization/OwnerBookingCharacterizationTest.java`

**Checkpoint**: 8 core user journeys verified; regression safety net established.

---

## Phase 8: Polish, Standards Documentation & Verification

**Purpose**: Write architecture and conventions documentation, perform frontend contract sync, and verify entire stack end-to-end.

- [ ] T044 [P] Update working agreements and agent instructions in `AGENTS.md`
- [ ] T045 [P] Create ADR 001 on Monolith Clean Architecture in `docs/decisions/001-monolith-clean-architecture.md`
- [ ] T046 [P] Create ADR 002 on Self-Managed JWT ES256 in `docs/decisions/002-self-managed-jwt.md`
- [ ] T047 [P] Create ADR 003 on Flyway Migration in `docs/decisions/003-flyway-migration.md`
- [ ] T048 [P] Create ADR 004 on Problem Details RFC 9457 in `docs/decisions/004-problem-details-no-envelope.md`
- [ ] T049 [P] Create coding conventions and code hygiene guide in `docs/development/coding-conventions.md` and `docs/development/code-hygiene.md`
- [ ] T050 [P] Create testing guide in `docs/development/testing.md` and local setup guide in `docs/development/local-setup.md`
- [ ] T051 [P] Create API overview and standards guide in `docs/api/README.md`
- [ ] T052 Perform frontend guide close-out synchronization against source contracts, count routes, fields, enums, and Problem Details in `specs/eddie-001-foundation-setup/frontend-guide.md`
- [ ] T053 Build and launch full backend container stack via `docker compose up --build -d` and verify `GET http://localhost:8080/healthz` returns 200 OK
- [ ] T054 Commit all completed foundation tasks locally under `Eddie0517 <khang17122005@gmail.com>` (**DO NOT PUSH**)

---

## Dependencies & Execution Order

### Phase Dependencies
- **Phase 1 (Setup)**: No dependencies — executes immediately.
- **Phase 2 (Foundational)**: Depends on Phase 1 — blocks all user stories.
- **Phase 3 (US1 - Infra)**: Depends on Phase 2 — MVP infrastructure.
- **Phase 4 (US2 - Architecture)**: Depends on Phase 2 & 3.
- **Phase 5 (US3 - Flyway)**: Depends on Phase 3 & 4.
- **Phase 6 (US4 - OpenAPI)**: Depends on Phase 4 & 5.
- **Phase 7 (US5 - Characterization)**: Depends on Phase 5.
- **Phase 8 (Polish & Docs)**: Depends on all prior phases.

### Parallel Opportunities
- T002, T003, T004, T005, T006 can run in parallel in Phase 1.
- T010, T011, T012 can run in parallel in Phase 3.
- T014, T015, T016, T017, T018, T019, T020, T022 can run in parallel in Phase 4.
- T029, T030, T031 can run in parallel in Phase 6.
- T036 through T043 can run in parallel in Phase 7.
- T044 through T051 documentation tasks can run in parallel in Phase 8.

---

## Implementation Strategy

### MVP First (Phases 1, 2 & 3)
1. Complete repository setup (`.gitignore`, `.env.example`, `scripts/jwt-keys.ps1`).
2. Complete Docker Compose stack (`postgres`, `redis`, `mailpit`).
3. Boot infrastructure: `docker compose up -d` -> verify healthy.

### Incremental Delivery (Phases 4, 5, 6, 7 & 8)
4. Add 4-module Clean Architecture skeleton + ArchUnit tests.
5. Add Flyway baseline migration (`V1__baseline_schema.sql`) -> verify 21 tables.
6. Add OpenAPI spec exporter + Problem Details exception handler.
7. Add 8 characterization tests covering legacy behavior.
8. Polish documentation, ADRs, and perform final local commit.
