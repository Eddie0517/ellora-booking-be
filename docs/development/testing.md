# Testing Guide — Ellora Booking Backend

> Comprehensive testing strategy, test pyramid, and execution guidelines for `ellora-booking-be`.

---

## 1. Testing Philosophy & Test Pyramid

Our testing strategy adheres to the Constitution principle: **Test-First & Verified Gates (NON-NEGOTIABLE)**. Every behavioral change must be verified by automated tests before commit.

```text
       /\
      /  \       E2E / Container Integration Tests (~10%)
     /----\      - Docker full stack (PostgreSQL, Redis, Mailpit)
    /      \     Characterization Tests (~20%)
   /--------\    - 8 legacy user journeys regression suite
  /          \   Architectural Tests (~20%)
 /------------\  - ArchUnit compile-time layer boundary enforcement
/              \ Unit Tests (~50%)
---------------- - Domain entities & Application use case unit tests
```

---

## 2. Test Suites by Layer

### A. Unit Tests (Domain & Application)
- **Frameworks**: JUnit 5, Mockito, AssertJ.
- **Domain Tests**: Test pure business logic in `ellora-domain`. No Spring context, no mocking needed — pure POJO instantiation and assertion.
- **Application Tests**: Test use cases in `ellora-application`. Mock outward port interfaces (`BookingRepository`, `TokenProvider`, `MailSender`) using Mockito.
- **Target Execution Time**: <100ms per test class.

### B. Architectural Tests (`ellora-architecture-tests`)
- **Framework**: ArchUnit 1.3.0.
- **Enforced Rules**:
  1. `DomainLayerRulesTest`: Asserts that `com.ellora.domain..` does not access `org.springframework..`, `jakarta.persistence..`, or any infrastructure code.
  2. `ControllerLayerRulesTest`: Asserts that classes annotated with `@RestController` do not depend directly on classes in `com.ellora.infrastructure.persistence..` (no direct entity or repository access).
- **Execution**: Runs automatically as part of `mvn test`.

### C. Characterization Tests (Legacy Regression Guard)
- **Purpose**: Validate behavioral equivalence between legacy flows and new Clean Architecture use cases.
- **8 Core Covered Journeys**:
  1. User Registration (with verification email dispatch).
  2. User Login & Token Issuance (HS256 JWT generation).
  3. Salon Discovery & Public Search (filters, sorting, pagination).
  4. Authenticated Customer Booking Creation (validates slot availability and total pricing).
  5. Guest Booking Creation (unauthenticated customer contact capture).
  6. Booking Status Transition (`CONFIRMED` -> `IN_SERVICE` -> `COMPLETED` / `CANCELLED`).
  7. Salon Review Submission (rating and feedback persistence).
  8. Owner Booking Management (salon owner viewing and modifying appointments).

### D. Integration Tests (Infrastructure & API)
- **Frameworks**: Spring Boot Test, MockMvc, Testcontainers (PostgreSQL).
- **Database Testing**: Tests execute against real PostgreSQL instances (via Testcontainers or local Docker PostgreSQL), validating actual SQL queries and Flyway migrations.

---

## 3. Running Tests via Docker

Because the host requires **zero host Java / Maven**, all tests are executed inside the containerized environment:

### Run All Unit and Architecture Tests:
```powershell
docker compose run --rm backend mvn test
```

### Run a Specific Test Class:
```powershell
docker compose run --rm backend mvn test -Dtest=DomainLayerRulesTest
```

### Run Full Verification (Clean, Compile, Test, Package):
```powershell
docker compose run --rm backend mvn clean verify
```

---

## 4. Test Best Practices

1. **Negative Controls (Test-First Principle)**:
   - When writing a new test, ensure it fails for the expected reason before implementing the production code.
2. **Clear Test Method Naming**:
   - Use standard given-when-then or condition-action-expectation naming:
     ```java
     @Test
     void shouldThrowBusinessRuleException_whenBookingSlotIsAlreadyTaken() { ... }
     ```
3. **AssertJ Assertions**:
   - Prefer fluent AssertJ assertions for clear failure diagnostics:
     ```java
     assertThat(response.status()).isEqualTo("CONFIRMED");
     assertThat(response.totalPrice()).isEqualByComparingTo(new BigDecimal("350000"));
     ```
4. **No Flaky Tests**:
   - Never use arbitrary thread sleeps (`Thread.sleep()`). Use deterministic awaiters (`Awaitility`) or mock clock instances (`Clock` port) when testing time-sensitive logic.
