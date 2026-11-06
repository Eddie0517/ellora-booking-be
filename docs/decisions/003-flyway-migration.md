# ADR 003: Database Baseline Migration via Flyway and Strict DDL Management

- **Status**: Accepted
- **Date**: 2026-11-06
- **Authors**: Eddie0517
- **Deciders**: Architecture Team

---

## Context and Problem Statement

The legacy repository accumulated 13 fragmented SQL migration and setup files scattered across `src/main/resources/db/` without an automated versioning engine. Developers previously relied on ad-hoc script execution or Hibernate automatic schema generation (`ddl-auto: update`), which presents severe operational risks:
- Hibernate auto-generation does not track history, cannot rollback, and silently fails or drops columns/indexes in production.
- Discrepancies between local development schemas and production database structures caused runtime SQL syntax errors.
- External database connection poolers (such as Supabase Transaction-Mode PgBouncer on port 6543) block migration tools like Flyway from acquiring necessary transactional advisory locks (`pg_locks`).

A deterministic, repeatable, and automated schema migration management strategy was required.

---

## Decision Drivers

- **Zero Schema Drift**: Development, staging, CI containers, and production databases must maintain strictly identical relational schemas.
- **Auditability & Traceability**: Every schema modification must be represented as an immutable, versioned migration script tracked in a metadata history table.
- **Production Safety**: Hibernate MUST NEVER alter the database structure automatically at runtime.
- **Supabase / Cloud Compatibility**: Migration executions must succeed against managed cloud databases without advisory lock failures.

---

## Considered Options

1. **Option 1: Hibernate Automatic DDL (`ddl-auto: update`)**
   - *Pros*: Zero migration files to write.
   - *Cons*: Extremely dangerous for production. Modifies columns unpredictably, never cleans up deprecated tables/indexes, and offers no audit trail.
2. **Option 2: Liquibase**
   - *Pros*: Powerful cross-database abstractions, rollback support via XML/YAML/JSON.
   - *Cons*: High boilerplate, cumbersome XML changelogs, unnecessary complexity for a pure PostgreSQL deployment.
3. **Option 3: Flyway with Baseline Migration and Strict DDL Management** (Chosen)
   - *Pros*: Uses native, clean SQL scripts; lightweight; deep Spring Boot integration; transparent tracking via `flyway_schema_history`.
   - *Cons*: Backward migrations must be written as forward-compensating migrations.

---

## Decision Outcome

Chosen Option: **Option 3 — Flyway with Baseline Migration and Strict DDL Management**.

### Key Technical Specifications:

1. **Single Consolidated Baseline**:
   - The 13 legacy SQL scripts were consolidated into a single clean baseline migration script:
     `ellora-api/src/main/resources/db/migration/V1__baseline_schema.sql`.
   - Defines all 21 core tables (`users`, `user_roles`, `salons`, `salon_branches`, `services`, `service_categories`, `staff_members`, `staff_services`, `staff_working_hours`, `bookings`, `booking_items`, `booking_history`, `reviews`, `payments`, `refresh_tokens`, `notifications`, `salon_images`, `coupons`, `user_coupons`, `audit_logs`, `settings`), primary keys (UUIDs / BigInts), foreign key constraints with indexes, and audit columns (`created_at`, `updated_at`).
2. **Strict Hibernate DDL Settings**:
   - In all environments, Hibernate DDL auto-generation is disabled:
     ```yaml
     spring:
       jpa:
         hibernate:
           ddl-auto: none
     ```
   - In local and test profiles, `ddl-auto: validate` is optionally used to ensure JPA `@Entity` definitions stay 100% synchronized with the Flyway schema.
3. **Direct Database Connection for Cloud Migrations (Port 5432)**:
   - When connecting to managed cloud providers like Supabase, Flyway migrations must connect to the **Session Mode** direct port (`5432`) rather than the **Transaction Mode** PgBouncer port (`6543`).
   - Transaction poolers do not support PostgreSQL session-level advisory locks (`pg_advisory_lock`), which Flyway uses to prevent concurrent migration race conditions.
4. **Automated Startup Migration**:
   - Flyway runs automatically on application startup inside the containerized Spring Boot backend, applying any pending migrations sequentially before accepting HTTP traffic.

---

## Consequences

### Positive:
- **Deterministic Deployments**: Fresh database containers and existing databases reach the exact same state reliably.
- **Safe CI/CD Pipelines**: Automated test containers and integration tests execute against the true production-grade schema.
- **Accurate Audit Trail**: The `flyway_schema_history` table records exact checksums, timestamps, and execution durations for every migration.

### Negative / Trade-offs:
- **Discipline Required**: Developers cannot edit JPA entity annotations to alter database columns; every database change requires a new versioned script (`V2__...sql`, `V3__...sql`).
