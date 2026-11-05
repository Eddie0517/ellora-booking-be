# Quickstart Guide: Foundation Setup for Ellora Backend Refactor

**Feature**: `specs/eddie-001-foundation-setup` | **Date**: 2026-11-06

---

## 1. Prerequisites

- **Docker Desktop**: Docker 20.10+ (v29+ verified) and Docker Compose v2+ (v5.5+ verified).
- **Git**: Configured locally with `user.name "Eddie0517"` and `user.email "khang17122005@gmail.com"`.
- **Zero Host Java**: No JDK or Maven is required on the host system.

---

## 2. Environment Setup

1. Copy the environment configuration template:
   ```powershell
   Copy-Item .env.example .env
   ```
2. Generate an ES256 cryptographic key pair for local JWT operations:
   ```powershell
   powershell -ExecutionPolicy Bypass -File scripts/jwt-keys.ps1
   ```
   Copy the output `JWT_PRIVATE_KEY` and `JWT_PUBLIC_KEY` values into your `.env` file.
3. In `.env`, fill in required external credentials:
   - `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`
   - `MAIL_USERNAME` (Gmail), `MAIL_PASSWORD` (Google 16-character App Password)

---

## 3. Launching the Full Containerized Stack

Launch PostgreSQL, Redis, Mailpit, and the Backend container:

```powershell
docker compose up --build -d
```

Verify service health:
```powershell
docker compose ps
```
All services (`postgres`, `redis`, `mailpit`, `backend`) should report `healthy` or `running`.

---

## 4. Running Verification & Architectural Tests (in Docker)

Run Maven build, spotless format check, and ArchUnit architectural tests inside the build container:

```powershell
docker compose run --rm backend mvn clean test
```

Expected output:
- `ellora-domain`: COMPILED (0 framework dependencies)
- `ellora-application`: COMPILED
- `ellora-infrastructure`: COMPILED
- `ellora-api`: COMPILED
- `ellora-architecture-tests`: 100% PASSED (0 layer violations)

---

## 5. Validating Flyway Database Migrations

Verify that baseline schema migrations were applied cleanly:

```powershell
docker compose exec postgres psql -U ellora -d ellora -c "\dt"
```
Expected output: 21 core tables present (including `users`, `salons`, `bookings`, `services`, `reviews`, `flyway_schema_history`).

---

## 6. Exporting OpenAPI Specification for Frontend

Trigger the OpenAPI contract exporter:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/export-openapi.ps1
```
Check that `contracts/openapi.json` is generated and valid.

---

## 7. Web Interfaces

- **Backend API**: `http://localhost:8080/healthz`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Mailpit Web UI (captured emails)**: `http://localhost:8025`
- **PGAdmin (optional)**: `http://localhost:5050` (start with `docker compose --profile tools up -d`)
