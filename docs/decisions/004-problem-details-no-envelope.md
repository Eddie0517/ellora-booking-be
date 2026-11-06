# ADR 004: RFC 9457 Problem Details and Direct DTO Returns (Dropping ApiResponse<T>)

- **Status**: Accepted
- **Date**: 2026-11-06
- **Authors**: Eddie0517
- **Deciders**: Architecture Team

---

## Context and Problem Statement

The legacy backend wrapped all HTTP responses in a generic JSON envelope structure, commonly known as `ApiResponse<T>`:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```
This pattern introduced several chronic architectural and integration issues:
1. **HTTP Status Code Masking**: Controllers often returned `HTTP 200 OK` while embedding `"success": false` and error codes inside the body payload, breaking HTTP caching, API gateways, reverse proxies, and standard client interceptors.
2. **DTO Boilerplate & Double Deserialization**: Frontend applications (e.g. Angular, React) had to unwrap `.data` on every single request, complicating TypeScript code generation via OpenAPI tools (`openapi-generator-cli`, `ng-openapi-gen`).
3. **Inconsistent Error Formats**: Different exception handlers produced divergent JSON payloads when unexpected errors occurred (e.g., standard Spring Boot 500 error vs custom error envelopes).

A standardized, industry-accepted approach was needed to provide clear, uniform API contracts.

---

## Decision Drivers

- **Open Standards Compliance**: Align with IETF RFC 9457 (Problem Details for HTTP APIs), the global standard for HTTP error representation.
- **Frontend Developer Ergonomics**: Enable automatic TypeScript model and API client generation without redundant wrapping layers.
- **Accurate Transport Semantics**: HTTP status codes must faithfully reflect request results (200, 201, 204, 400, 401, 403, 404, 409, 422, 500).
- **Traceability**: All errors must provide an unambiguous `traceId` and error `type` URI to simplify production issue triage.

---

## Considered Options

1. **Option 1: Retain `ApiResponse<T>` Envelope**
   - *Pros*: Familiar to developers who have always used envelope wrappers.
   - *Cons*: Clutters OpenAPI specs with generic wrappers, breaks standard HTTP semantics, requires extra type unwrapping in frontend services.
2. **Option 2: Spring Boot Default WhiteLabel Error Page / Default BasicErrorController**
   - *Pros*: Built-in Spring Boot behavior with zero custom code.
   - *Cons*: Exposes internal exception class names, stack traces, and does not support domain-specific structured validation error arrays (`invalidParams`).
3. **Option 3: Direct DTO Returns for Success + RFC 9457 Problem Details for Errors** (Chosen)
   - *Pros*: Natural HTTP semantics; OpenAPI generates clean TypeScript interfaces directly matching backend DTOs; standardized error parsing across all HTTP clients.
   - *Cons*: Requires updating legacy frontend service calls that previously expected `.data` unwrapping.

---

## Decision Outcome

Chosen Option: **Option 3 — Direct DTO Returns for Success + RFC 9457 Problem Details for Errors**.

### Key Technical Specifications:

1. **Successful Responses**:
   - REST Controllers return domain DTOs or `ResponseEntity<T>` directly.
   - Examples:
     - Single Resource: `GET /healthz` -> returns `HealthStatus` (`{ "status": "UP", "timestamp": "..." }`).
     - Collections / Pagination: `GET /api/v1/salons` -> returns `PagedResult<SalonSummaryResponse>` directly.
     - Deletions / Empty Responses: Return `204 No Content` with an empty response body.
2. **Error Responses (RFC 9457 `application/problem+json`)**:
   - All errors are formatted via a unified `@RestControllerAdvice` (`GlobalExceptionHandler`) into the RFC 9457 schema:
     ```json
     {
       "type": "https://ellora.vn/errors/validation-error",
       "title": "Validation Failed",
       "status": 400,
       "detail": "Invalid request parameters provided",
       "instance": "/api/v1/bookings",
       "traceId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
       "invalidParams": [
         {
           "name": "email",
           "reason": "Email must be a valid RFC 5322 address"
         }
       ]
     }
     ```
3. **HTTP Status Code Mapping**:
   - `400 Bad Request`: Syntax or malformed JSON payloads, general input errors.
   - `401 Unauthorized`: Missing, expired, or invalid JWT Bearer token.
   - `403 Forbidden`: Authenticated user lacks permission / required role.
   - `404 Not Found`: Requested resource does not exist.
   - `409 Conflict`: Unique constraint violation or state concurrency conflict.
   - `422 Unprocessable Entity`: Business rule violations (e.g., salon is closed, slot already reserved).
   - `500 Internal Server Error`: Unhandled internal server or infrastructure failure.

---

## Consequences

### Positive:
- **Clean API Contracts**: OpenAPI schema (`contracts/openapi.json`) cleanly documents actual domain entities and responses without generic wrapper noise.
- **Frontend Code Generation**: Angular client tools generate strongly typed TypeScript services and interfaces seamlessly.
- **Standard HTTP Ecosystem Interoperability**: Observability tools, HTTP proxies, and browser caches operate naturally based on HTTP status codes.
- **Triage Speed**: Client error reports include `traceId`, which backend developers can immediately look up in logs.

### Negative / Trade-offs:
- **Client Migration**: Existing legacy frontend code calling old endpoints must be updated to expect direct DTOs rather than reading `.data`.
