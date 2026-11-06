# ADR 001: Monolith Clean Architecture L2 with 4 Maven Modules

- **Status**: Accepted
- **Date**: 2026-11-06
- **Authors**: Eddie0517
- **Deciders**: Architecture Team

---

## Context and Problem Statement

The legacy Ellora Nail Salon booking backend was structured as a flat, single-module Spring Boot application where database entities, web controllers, repository queries, and business logic were tightly coupled. Developers routinely injected JPA repositories and entities directly into REST controllers, leading to:
- Silent domain logic leakage into web controllers and templates.
- High risk of regression when database schemas changed.
- Difficulty in unit testing core business workflows without spinning up Spring contexts or in-memory databases.
- Vulnerability to architectural erosion over time because package-private visibility in Java is easily bypassed.

We needed a structure that enforces clean separation of concerns, guarantees domain isolation, remains simple to deploy and operate, and provides automated enforcement against boundary violations.

---

## Decision Drivers

- **Domain Isolation**: Core business rules and entities must be independent of external frameworks (no Spring, JPA, or third-party persistence dependencies).
- **Maintainability & Testability**: Business use cases must be testable with pure unit tests (Mockito / POJOs) without heavy container initialization.
- **Simplicity of Deployment**: The system scale (~21 tables, ~92 endpoints, single development team) does not justify the operational overhead of microservices (distributed transactions, network latencies, outbox patterns).
- **Compile-Time & Build-Time Enforcement**: Layer boundaries must be verified at build time by the compiler and automated architecture tests (ArchUnit).

---

## Considered Options

1. **Option 1: Single-Module Package-by-Feature**
   - *Pros*: Simple Maven setup, fastest initial project initialization.
   - *Cons*: Package-private visibility is easily bypassed by developers; cyclic imports between packages inevitably appear over time; hard to verify framework isolation in domain.
2. **Option 2: Microservices Decomposition (3–4 Services: Identity, Salon, Booking)**
   - *Pros*: Strong physical isolation, independent deployment.
   - *Cons*: Massive overhead for distributed transactions, network serialization, event brokers, Docker infrastructure, and small-team coordination; violates the principle of starting with a well-factored monolith.
3. **Option 3: Modular Monolith with Clean Architecture L2 (4 Maven Modules)** (Chosen)
   - *Pros*: Strict compile-time boundary enforcement, zero framework dependencies in domain, deployable as a single Spring Boot fat JAR, zero network latency between layers.
   - *Cons*: Requires DTO mapping between layers and multi-module Maven POM maintenance.

---

## Decision Outcome

Chosen Option: **Option 3 — Modular Monolith with Clean Architecture L2 (4 Maven Modules)**.

### Architectural Blueprint:

1. **`ellora-domain`**:
   - Zero framework dependencies (JDK 17 LTS only).
   - Contains core business entities (`User`, `Salon`, `Booking`, `Service`, `Review`), value objects, business enums (`Role`, `BookingStatus`, `SalonStatus`), domain repository interfaces (outward ports), and domain exceptions (`DomainException`, `BusinessRuleException`).
2. **`ellora-application`**:
   - Depends only on `ellora-domain`.
   - Organizes business use cases and CQRS features (`auth`, `salon`, `booking`, `owner`, `admin`).
   - Defines input/output records, pagination DTO (`PagedResult<T>`), and outward port interfaces (`TokenProvider`, `PasswordHasher`, `MailSender`, `ImageStorage`, `Clock`, `CurrentUser`).
   - Declares the **transaction boundary**: `@Transactional` is applied strictly to application services/use cases, ensuring all business mutations commit or rollback atomically.
3. **`ellora-infrastructure`**:
   - Depends on `ellora-application` and `ellora-domain`.
   - Implements ports and persistence: Spring Data JPA entities, JPA repositories, database adapters, MapStruct mappers, Redis cache/rate limiter, Cloudinary adapter, and Google SMTP/Mailpit adapters.
4. **`ellora-api`**:
   - Composition root (`@SpringBootApplication`). Depends on `ellora-application` and `ellora-infrastructure`.
   - Exposes REST controllers, Spring Security configuration (`SecurityFilterChain`), RFC 9457 Global Exception Handler, OpenAPI 3 configuration, and Flyway database migration scripts.
5. **`ellora-architecture-tests`**:
   - Standalone module executing ArchUnit assertions during the Maven `test` phase. Fails the build immediately if any layer dependency rule is breached.

---

## Consequences

### Positive:
- **Compile-time enforcement**: `ellora-domain` physically cannot import Spring, Hibernate, or Jackson because they are not present in its `pom.xml`.
- **Fast and reliable testing**: Domain and Application layers can be tested with rapid unit tests executing in milliseconds.
- **Single artifact deployment**: The build produces a single executable fat JAR in `ellora-api/target/` deployable with standard Docker containers.
- **Consistent transaction management**: Transaction boundaries are declared exclusively at the application layer (`@Transactional`), avoiding fragmented transaction logic in controllers or repositories.

### Negative / Trade-offs:
- **Mapping overhead**: Entities in `ellora-infrastructure` must be mapped to/from domain models and application DTOs (mitigated by MapStruct code generation).
- **Maven multi-module complexity**: Requires managing parent-child POM versions and inter-module dependencies.
