# Coding Conventions Guide — Ellora Booking Backend

> Coding style, modern Java 17 idiom standards, and architectural design conventions for developers and AI agents working on `ellora-booking-be`.

---

## 1. General Java 17 Conventions

### Immutability & Data Transfer Objects
- **Use Java Records for all DTOs and Value Objects**:
  ```java
  public record CreateBookingRequest(
      @NotNull UUID salonId,
      @NotNull UUID serviceId,
      @NotNull Instant startTime,
      @Size(max = 500) String customerNote
  ) {}
  ```
- **Avoid mutable state in business models**: Prefer returning new instances rather than mutating fields in place where practical.
- **Pattern Matching & Switch Expressions**:
  Use Java 17 pattern matching for `instanceof` and modern switch expressions with exhaustive arrow syntax:
  ```java
  return switch (status) {
      case PENDING_CONFIRMATION, PENDING_DEPOSIT -> false;
      case CONFIRMED, IN_SERVICE, COMPLETED -> true;
      case CANCELLED, NO_SHOW -> false;
  };
  ```

---

## 2. Layer-Specific Conventions

### A. Domain Layer (`ellora-domain`)
- **Pure Java**: NO Spring (`@Service`, `@Component`, `@Autowired`), NO JPA (`@Entity`, `@Table`), NO Jackson annotations in `ellora-domain`.
- **Entities**: Plain Old Java Objects (POJOs) with encapsulated state and validation methods.
- **Exceptions**: All domain errors extend `DomainException` or `BusinessRuleException`:
  ```java
  public class BusinessRuleException extends RuntimeException {
      private final String errorCode;
      public BusinessRuleException(String errorCode, String message) {
          super(message);
          this.errorCode = errorCode;
      }
      public String getErrorCode() { return errorCode; }
  }
  ```
- **Repository Ports**: Interfaces only. Define what the domain needs, not how it is queried:
  ```java
  public interface BookingRepository {
      Optional<Booking> findById(UUID id);
      Booking save(Booking booking);
      boolean hasConflictingBooking(UUID staffId, Instant start, Instant end);
  }
  ```

### B. Application Layer (`ellora-application`)
- **Package by Feature**: Organize use cases under `com.ellora.application.feature.<feature_name>`:
  - `auth`, `salon`, `booking`, `owner`, `admin`.
- **Use Case Single Responsibility**: Each use case class represents an intentional application operation (e.g., `CreateBookingUseCase`, `CancelBookingUseCase`, `GetSalonDetailsUseCase`).
- **Transaction Boundaries**:
  - Apply `@Transactional` explicitly at the use case / application service level:
    ```java
    @Service
    @RequiredArgsConstructor
    public class CreateBookingUseCase {
        private final BookingRepository bookingRepository;
        private final ApplicationEventPublisher eventPublisher;

        @Transactional
        public BookingResponse execute(CreateBookingRequest request) {
            // business orchestration
        }
    }
    ```
  - For query use cases, use `@Transactional(readOnly = true)`.
- **Outward Ports**: Define interfaces for all external systems in `com.ellora.application.common.port`:
  - `TokenProvider`, `PasswordHasher`, `MailSender`, `ImageStorage`, `Clock`, `CurrentUser`.

### C. Infrastructure Layer (`ellora-infrastructure`)
- **JPA Entities & Tables**:
  - Tables are named in lowercase snake_case plural (`users`, `bookings`, `salons`).
  - Columns are named in snake_case (`created_at`, `salon_id`, `is_active`).
  - Entities extend a common audit base or include `@CreatedDate` / `@LastModifiedDate`.
- **Mappers**:
  - Use MapStruct for converting between JPA Entities, Domain Models, and Application DTOs.
  - Avoid hand-rolled mapping boilerplate when mapping large objects.
- **Adapters**:
  - Implement ports defined in `ellora-application` and repositories defined in `ellora-domain`.
  - Handle low-level transport errors and wrap them into clean domain or application exceptions.

### D. API Layer (`ellora-api`)
- **Controllers**:
  - Lightweight adapters responsible only for HTTP transport mapping, parameter validation (`@Valid`), and delegating to use cases.
  - Return DTOs or `ResponseEntity<T>` directly.
  - Do NOT inject JPA repositories or entities into controllers.
- **Path Naming**:
  - Lowercase, hyphen-separated (kebab-case) REST paths: `/api/v1/salons`, `/api/v1/user-profiles`.
  - Always version public business APIs (`/api/v1/...`).
- **Validation**:
  - Use Jakarta Bean Validation annotations on incoming request records (`@NotBlank`, `@NotNull`, `@Size`, `@Pattern`, `@Min`, `@Max`).

---

## 3. Error Handling Conventions

- **Never return raw runtime exceptions to clients**: All exceptions are translated by `GlobalExceptionHandler` into RFC 9457 `ProblemDetail`.
- **Domain Exceptions**: Mapped to `400 Bad Request` or `422 Unprocessable Entity` with a corresponding problem `type` URI (e.g. `https://ellora.vn/errors/booking.conflict`).
- **Resource Not Found**: Mapped to `404 Not Found` with problem `type` (e.g. `https://ellora.vn/errors/resource-not-found`).
- **Input Validation Failures**: `MethodArgumentNotValidException` is mapped to `400 Bad Request` with an `invalidParams` array containing specific field violations.

---

## 4. Logging & Diagnostics

- **SLF4J Logger**:
  ```java
  private static final Logger log = LoggerFactory.getLogger(CreateBookingUseCase.class);
  ```
- **Parameterized Logging**: Always use SLF4J placeholders (`log.info("Booking created: id={}, salonId={}", booking.getId(), booking.getSalonId())`) rather than string concatenation.
- **PII & Secret Protection**: NEVER log raw passwords, JWT tokens, credit card details, or authentication secrets.
- **Trace Correlation**: Log statements in web filters should include or correlate with the request `traceId`.
