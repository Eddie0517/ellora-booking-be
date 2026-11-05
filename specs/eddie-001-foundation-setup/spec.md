# Feature Specification: Foundation Setup for Ellora Backend Refactor

**Feature Branch**: `feat/foundation-setup`

**Created**: 2026-11-06

**Status**: Draft

**Input**: User description: "Foundation setup for Ellora backend refactor: Clean Architecture 4-module skeleton, Docker Compose, Flyway baseline, OpenAPI contracts, and characterization tests"

## Clarifications

### Session 2026-11-06

- Q: Môi trường chạy và quản lý toolchain Java/Maven cho backend? → A: Không cài đặt JDK/Maven trực tiếp lên máy host. Sử dụng Docker hoàn toàn: pull image chính thức của Java 17 (Eclipse Temurin) và Maven; toàn bộ quy trình biên dịch, kiểm thử, chạy server backend và database migrations đều vận hành 100% bên trong Docker container.
- Q: Đối với các dịch vụ bên ngoài (gửi mail và lưu trữ ảnh Cloudinary), bạn muốn thiết lập trong môi trường local và bộ test như thế nào? → A: Cấu hình sẵn sàng và yêu cầu cung cấp ngay thông tin tài khoản Cloudinary thật và Google SMTP App Password thật trong file .env local ngay từ Phase P0, có tài liệu hướng dẫn và ghi chú rõ ràng các key thật cần điền.
- Q: Mô hình vận hành docker-compose cho backend khi phát triển và kiểm thử ở local nên được cấu hình theo hướng nào? → A: docker-compose quản lý toàn bộ stack dịch vụ bao gồm PostgreSQL 17, Redis 7, Mailpit, PGAdmin (profile tools) và service `backend` chạy ứng dụng Spring Boot (multi-stage Dockerfile dựa trên image Temurin Java 17 / Maven), đảm bảo BE chạy hoàn toàn trong Docker.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Local Development Infrastructure & Environment Readiness (Priority: P1)

As a backend engineer starting the refactoring process, I need a deterministic local infrastructure environment (database, caching, and local mail capturing) with clear configuration templates so that I can run, migrate, and test backend components locally without external cloud dependencies.

**Why this priority**: Without a stable local runtime environment, no automated tests, database migrations, or server modules can be built or verified.

**Independent Test**: Can be independently verified by executing the local container infrastructure command and confirming that the core data and mail services become healthy and accessible with default environment variables.

**Acceptance Scenarios**:

1. **Given** a clean clone of the backend repository with an environment template, **When** the developer launches the local container stack, **Then** PostgreSQL, Redis, and Mailpit services become healthy within 30 seconds.
2. **Given** missing or misconfigured sensitive environment variables, **When** the application starts up, **Then** the application fails immediately with explicit, actionable error messages rather than starting in an undefined state.

---

### User Story 2 - Clean Architecture Boundaries & Build Enforcement (Priority: P1)

As a software architect and developer, I need the backend codebase structured into 4 distinct modular boundaries (`domain`, `application`, `infrastructure`, `api`) backed by strict automated architectural rules, so that business domain logic remains completely decoupled from frameworks and database persistence details.

**Why this priority**: Architectural drift is the primary cause of maintainability collapse; boundaries must be enforced mechanically from day zero before any use case code is written.

**Independent Test**: Can be independently verified by building the multi-module project and running automated architectural rule checks, confirming that invalid cross-layer dependencies (e.g., domain importing framework libraries) immediately fail the build.

**Acceptance Scenarios**:

1. **Given** the 4-module project structure, **When** the build command is executed, **Then** all 4 modules compile cleanly in the correct dependency order (`domain` -> `application` -> `infrastructure` -> `api`).
2. **Given** a prohibited dependency introduction (such as Spring or persistence annotations added to the domain module), **When** architectural verification tests run, **Then** the verification fails with a clear violation description.

---

### User Story 3 - Database Migration Baseline (Priority: P2)

As a developer and system operator, I need a clean Flyway database baseline containing all existing tables, constraints, and indexes, so that database schema evolution is versioned, predictable, and fully independent of ORM auto-generation.

**Why this priority**: Moving away from automated ORM schema updates (`ddl-auto: update`) to controlled, versioned migrations is mandatory for production safety and zero-downtime evolution.

**Independent Test**: Can be tested independently by applying the migration baseline onto a fresh database instance and verifying that all 21 core tables, relationships, and custom indexes match the legacy database schema.

**Acceptance Scenarios**:

1. **Given** an empty PostgreSQL instance, **When** the database migration engine executes against it, **Then** all baseline tables, foreign keys, and indexes are created without error.
2. **Given** an already migrated database, **When** the migration validation command is executed, **Then** the validation succeeds with zero checksum discrepancies.

---

### User Story 4 - Standardized OpenAPI Contract Export & Frontend Handoff (Priority: P2)

As a frontend developer or frontend AI agent, I need a machine-readable OpenAPI specification file (`contracts/openapi.json`) and a structured frontend integration guide generated for the backend APIs, so that I can reliably map Angular models, HTTP services, and error handling without guesswork or manual coordination.

**Why this priority**: Eliminates contract drift between separate backend and frontend repositories, enabling autonomous frontend development and code generation.

**Independent Test**: Can be verified by running the contract export command and verifying that an OpenAPI specification file is written to the contracts directory matching standard schema definitions.

**Acceptance Scenarios**:

1. **Given** an updated controller or DTO definition in the backend API, **When** the contract export command is triggered, **Then** an updated `contracts/openapi.json` is exported reflecting the new paths, request schemas, and RFC 9457 Problem Details error schemas.
2. **Given** a newly introduced or changed endpoint, **When** the developer generates the feature handoff document, **Then** a structured `frontend-guide.md` is produced outlining endpoints, DTOs, Enums, and status codes.

---

### User Story 5 - Legacy Characterization Test Suite (Priority: P3)

As a maintainer refactoring legacy services, I need a comprehensive characterization test suite covering the 8 existing critical flows (user registration, login, salon search, booking creation, guest booking, status transition, review submission, owner booking), so that any behavioral divergence during subsequent refactoring phases is detected immediately.

**Why this priority**: Prevents silent regression of existing business behavior while rearranging code into the new architecture.

**Independent Test**: Can be tested by running the characterization test suite against the baseline implementation and confirming 100% passing results across all 8 flows.

**Acceptance Scenarios**:

1. **Given** the existing legacy operational logic, **When** characterization tests execute against a containerized database, **Then** all 8 core flows pass and produce expected responses and state transitions.

---

### Edge Cases

- **Container Port Collisions**: When a local developer already has local PostgreSQL (5432) or Redis (6379) running on host ports, the environment configuration must allow port remapping via `.env` without modifying container configuration files.
- **Fail-Open on Cache/Telemetry**: When auxiliary services like Redis or local mail are temporarily unreachable, non-critical lookup paths should fail-open with warnings rather than crashing primary flows.
- **Schema Validation Failures**: When an uncommitted or mismatched migration script is detected, the application must abort startup immediately with checksum details.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a declarative container orchestration configuration (`docker-compose.yml`) managing PostgreSQL 17, Redis 7, Mailpit, and the Spring Boot Backend (`backend`), with zero host JDK dependency.
- **FR-002**: System MUST provide an environment configuration template (`.env.example`) and active `.env` documentation explicitly distinguishing between mockable local properties (database, cache, local mailpit) and required external production-grade secrets (Google SMTP App Password, Cloudinary API keys).
- **FR-003**: System MUST structure the application into 4 distinct Maven modules (`ellora-domain`, `ellora-application`, `ellora-infrastructure`, `ellora-api`) obeying unidirectional inward dependencies, buildable and runnable via multi-stage Docker containers using official Eclipse Temurin Java 17 Alpine images.
- **FR-004**: System MUST enforce via automated architectural tests that the `ellora-domain` module contains zero external framework dependencies (specifically no Spring, JPA, or serialization libraries).
- **FR-005**: System MUST enforce via automated architectural tests that API controllers do not directly reference persistence entities, database managers, or persistence repositories.
- **FR-006**: System MUST provide a baseline database migration script (`V1__baseline_schema.sql`) consolidating all existing legacy tables, constraints, and indexes.
- **FR-007**: System MUST generate an ES256 cryptographic key pair on demand via dedicated scripts for local testing without persisting private keys to disk.
- **FR-008**: System MUST export an OpenAPI 3 specification artifact to `contracts/openapi.json` reflecting live endpoint definitions and Problem Details schemas.
- **FR-009**: System MUST provide a feature-level `frontend-guide.md` template facilitating API and DTO handoff to frontend engineers and agents.
- **FR-010**: System MUST maintain automated characterization test coverage for the 8 core user journeys prior to refactoring individual business modules.

### Key Entities

- **Module Boundary**: Defines the accessibility, dependency rules, and isolation level across domain, use cases, infrastructure adapters, and API endpoints.
- **Database Migration Baseline**: The foundational immutable schema snapshot capturing all relational structures and indexes.
- **API Contract Specification**: The machine-readable OpenAPI schema document serving as the single source of truth for client integrations.
- **Architecture Test Suite**: Code-level verification rules asserting structural compliance and package boundaries.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A new developer can boot the full local infrastructure stack and run database migrations in under 2 minutes from a clean environment.
- **SC-002**: 100% of architectural boundary violations (such as improper imports into the domain layer) are caught at build time before deployment or merge.
- **SC-003**: 100% of the 8 critical business flows are validated by automated characterization tests with zero regressions against existing legacy behavior.
- **SC-004**: 100% of exposed REST endpoints and error structures are documented in the exported OpenAPI contract file without manual schema synchronization.

## Assumptions

- **Host Zero-Java Dependency**: The host machine requires only Docker Engine and Docker Compose; Java 17 and Maven run entirely containerized via official Eclipse Temurin Docker images without installing JDK on the host OS.
- **Local Docker Engine**: Developers and CI environments have Docker and Docker Compose installed and capable of running Linux containers.
- **External Secret Provisioning**: Real Cloudinary credentials and Google SMTP App Password are provided by the user in the local `.env` file from Phase P0, with clear indicators of required keys.
- **Database Reset**: Old development and staging user data can be cleanly initialized from the baseline migration without requiring legacy Cognito user data migration.
- **Version Control Constraints**: All code changes are committed locally under identity `Eddie0517 <khang17122005@gmail.com>` with no remote git pushes permitted until formal sign-off.
