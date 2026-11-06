# API Overview & Standards Guide — Ellora Booking Backend

> Public API design standards, contract specifications, authentication rules, error handling, and frontend client generation guidance for `ellora-booking-be`.

---

## 1. Architectural Principles

All REST endpoints in Ellora Booking Backend follow these foundational standards:
1. **Resource-Oriented REST**: Clean, intuitive URIs representing resources (`/api/v1/salons`, `/api/v1/bookings`).
2. **Contract-First Source of Truth**: The canonical OpenAPI 3.0 specification is maintained at `contracts/openapi.json`. Frontend client code is generated from this file.
3. **No Envelope Wrappers**: Responses return domain DTOs or collections directly. We do not use legacy `ApiResponse<T>` envelopes (`{ success: true, data: ... }`).
4. **Standard HTTP Semantics**: Status codes faithfully represent outcomes:
   - `200 OK`: Successful read or idempotent update.
   - `201 Created`: Successful creation of a new resource (e.g. Booking).
   - `204 No Content`: Successful deletion or state change with no payload.
   - `400 Bad Request`: Malformed syntax, invalid JSON, or field validation failures.
   - `401 Unauthorized`: Missing or invalid Bearer authentication token.
   - `403 Forbidden`: Authenticated user lacks permission for the resource.
   - `404 Not Found`: Target resource does not exist.
   - `409 Conflict`: Business or database uniqueness conflict.
   - `422 Unprocessable Entity`: Business rule violation.
   - `500 Internal Server Error`: Unhandled server exception.

---

## 2. Authentication & Authorization

- **Scheme**: HTTP Bearer JWT Authentication (`Authorization: Bearer <token>`).
- **Algorithm**: Symmetric HMAC-SHA256 (`HS256`).
- **Token Format**: Standard RFC 7519 JSON Web Token.
- **Payload Claims**:
  - `sub`: User UUID
  - `email`: User email address
  - `role`: User role enum (`ADMIN`, `OWNER`, `STAFF`, `CUSTOMER`)
  - `iss`: `ellora-booking-be`
  - `exp`: Expiration Unix epoch timestamp (15 minutes)
- **Public Endpoints** (Bypasses Authentication):
  - `GET /healthz` (Liveness probe)
  - `GET /v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`
  - `POST /api/v1/auth/login`, `POST /api/v1/auth/register`, `POST /api/v1/auth/refresh`
  - Public salon browsing & search endpoints

---

## 3. Standard Response Models

### Pagination: `PagedResult<T>`
For all paginated list endpoints, results are encapsulated in `PagedResult<T>`:
```json
{
  "items": [
    { "id": "4a737f10-9118-47fb-97be-fb1b1c3e387c", "name": "Ellora Luxury Spa" }
  ],
  "page": 1,
  "pageSize": 20,
  "totalItems": 150,
  "totalPages": 8
}
```
- `page`: 1-based page index.
- `pageSize`: Number of items requested per page (default: 20, max: 100).
- `totalItems`: Total number of matching records in database.
- `totalPages`: Calculated total pages (`ceil(totalItems / pageSize)`).

---

## 4. Error Representation (RFC 9457 Problem Details)

All API errors return `Content-Type: application/problem+json` following RFC 9457:

```json
{
  "type": "https://ellora.vn/errors/validation-error",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Request body failed validation constraints",
  "instance": "/api/v1/bookings",
  "traceId": "6f2e8250-b883-433a-967b-1cb2b1cb5b1a",
  "invalidParams": [
    {
      "name": "startTime",
      "reason": "Start time must be in the future"
    }
  ]
}
```

### Problem Details Schema Reference:
- `type` (URI): Standardized URI categorizing the error class.
- `title` (string): Short, human-readable summary of the problem type.
- `status` (integer): HTTP status code matching the transport header.
- `detail` (string): Specific description of the error occurrence.
- `instance` (string): Request URI that originated the problem.
- `traceId` (string): UUID correlation identifier for log auditing.
- `invalidParams` (array, optional): List of parameter-specific violation details (`name`, `reason`).

---

## 5. OpenAPI Contract & Frontend Code Generation

The API contract is version-controlled at `contracts/openapi.json`.

### Re-exporting Contract from Code:
```powershell
powershell -ExecutionPolicy Bypass -File scripts/export-openapi.ps1
```

### Generating Angular Client Services:
Frontend developers and agents can generate fully typed Angular models and HTTP client services using `@openapitools/openapi-generator-cli`:

```bash
npx @openapitools/openapi-generator-cli generate \
  -i ../ellora-booking-be/contracts/openapi.json \
  -g typescript-angular \
  -o src/app/core/api \
  --additional-properties=ngVersion=17,npmName=@ellora/api-client,supportsES6=true
```

Or using `ng-openapi-gen`:
```bash
npx ng-openapi-gen --input ../ellora-booking-be/contracts/openapi.json --output src/app/core/api
```

---

## 6. Health & Diagnostics Endpoints

- **Liveness Probe**: `GET /healthz`
  - Returns `200 OK` with `{"status":"UP","timestamp":"..."}` when the application container is alive.
  - Used by Kubernetes, Docker Compose, and cloud health monitors.
- **Spring Boot Actuator**: `GET /actuator/health`
  - Deep system health including PostgreSQL and Redis connection statuses.
