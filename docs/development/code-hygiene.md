# Code Hygiene Guide — Ellora Booking Backend

> Repository hygiene, formatting, dependency management, and quality control rules for developers and AI agents.

---

## 1. Naming Standards

| Element | Convention | Example |
|---|---|---|
| **Classes, Records, Interfaces** | PascalCase | `BookingConfirmationUseCase`, `SalonSummaryResponse` |
| **Methods & Variables** | camelCase | `calculateTotalPrice()`, `availableSlotCount` |
| **Constants & Enum Values** | UPPER_SNAKE_CASE | `MAX_RETRY_ATTEMPTS`, `BookingStatus.CONFIRMED` |
| **Package Names** | lowercase.concatenated | `com.ellora.application.feature.booking` |
| **REST URLs** | kebab-case | `/api/v1/salon-branches/{id}/working-hours` |
| **Database Tables & Columns** | snake_case | `booking_items`, `service_category_id` |
| **Configuration Properties** | kebab-case / snake_case | `spring.jpa.hibernate.ddl-auto`, `JWT_SECRET` |

---

## 2. Commenting & Documentation Hygiene

- **Self-Documenting Code**: Choose expressive method, variable, and class names so that basic comments become redundant.
- **Language Requirements**:
  - Source code comments and Javadocs MUST be written in **English**.
  - All comments in files starting with `.env` (e.g. `.env`, `.env.example`) MUST be written in **English**.
  - Specifications under `specs/` MUST be written in **Vietnamese**.
  - Architecture Decision Records (ADRs) and development guides in `docs/` are in **English**.
- **Javadocs**: Required for all public port interfaces (`TokenProvider`, `ImageStorage`) explaining the contractual expectations and failure behavior.

---

## 3. Dependency & Import Hygiene

- **No Wildcard Imports**: Never use `import java.util.*;` or `import org.springframework.web.bind.annotation.*;`. Always import explicit types.
- **No Unused Imports**: Remove dead imports before saving or committing.
- **Dependency Flow Rules**:
  - `ellora-domain` -> (No internal dependencies, JDK only).
  - `ellora-application` -> depends only on `ellora-domain`.
  - `ellora-infrastructure` -> depends on `ellora-application`, `ellora-domain`.
  - `ellora-api` -> depends on `ellora-application`, `ellora-infrastructure`.
  - Cyclic dependencies between packages or modules are strictly forbidden and will fail compilation or ArchUnit tests.

---

## 4. Git Hygiene & Artifact Integrity

- **Never Commit Secrets**:
  - Never commit `.env`, private keys, database passwords, or third-party API keys.
  - The `.gitignore` file enforces that `.env`, `.env.local`, `target/`, and IDE metadata (`.idea`, `.vscode`) are ignored.
- **Line Endings**:
  - Git line endings are normalized to LF (`.gitattributes` specifies `* text=auto eol=lf`).
- **No Push Policy**:
  - AI agents and automated pipelines MUST NOT push to remote repositories without explicit developer command. All commits remain strictly local.
- **Author Identity**:
  - Local commits must use: `Eddie0517 <khang17122005@gmail.com>`.

---

## 5. Code Review & Verification Checklist

Before completing any task or pull request:
1. [ ] Code compiles cleanly with zero compilation warnings: `docker compose run --rm backend mvn compile -q`.
2. [ ] All unit and ArchUnit tests pass: `docker compose run --rm backend mvn test`.
3. [ ] No framework or Spring dependencies introduced into `ellora-domain`.
4. [ ] No JPA entities or repositories leaked into `@RestController` classes.
5. [ ] Any new REST endpoint has its request and response models reflected in `contracts/openapi.json`.
6. [ ] Any schema modification is encapsulated in a new sequential Flyway script (`V{n}__...sql`).
