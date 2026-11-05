# Contracts: Foundation Setup for Ellora Backend Refactor

**Feature**: `specs/eddie-001-foundation-setup` | **Date**: 2026-11-06

---

## 1. Overview & Single Source of Truth

The canonical contract between backend and frontend is defined in machine-readable OpenAPI format and RFC 9457 Problem Details schemas.

- **Primary OpenAPI Contract**: Located at repository root `contracts/openapi.json`.
- **API Base URL**: `http://localhost:8080` (or reverse-proxy prefix `/api/**`).
- **Standard Content-Type**:
  - Success: `application/json; charset=utf-8`
  - Error: `application/problem+json; charset=utf-8`

---

## 2. Standard Problem Details Contract (RFC 9457)

All non-2xx responses MUST adhere to RFC 9457 Problem Details:

```json
{
  "type": "https://ellora.vn/errors/auth.invalid_credentials",
  "title": "Thông tin đăng nhập không chính xác",
  "status": 401,
  "detail": "Email hoặc mật khẩu không đúng.",
  "instance": "/api/auth/login",
  "traceId": "9fa85f64-5717-4562-b3fc-2c963f66afa6",
  "invalidParams": [
    {
      "name": "email",
      "reason": "Email không đúng định dạng"
    }
  ]
}
```

### Standard Status Codes
- `200 OK`: Request succeeded, returns DTO directly (envelope `ApiResponse<T>` is deprecated).
- `201 Created`: Resource successfully created, returns created DTO and `Location` header if applicable.
- `204 No Content`: Successful action with no body.
- `400 Bad Request`: Syntax or deserialization error.
- `401 Unauthorized`: Authentication missing, expired, or invalid.
- `403 Forbidden`: Authenticated user lacks permission or email unverified.
- `404 Not Found`: Resource does not exist.
- `409 Conflict`: Business or slot conflict (e.g. concurrent slot booking).
- `422 Unprocessable Entity`: Business validation failure.
- `500 Internal Server Error`: Unhandled exception (sanitized detail, trace ID preserved).

---

## 3. Standard Pagination Contract

```json
{
  "items": [],
  "page": 1,
  "pageSize": 20,
  "totalItems": 120,
  "totalPages": 6
}
```

---

## 4. Automation & Frontend Synchronization

1. Whenever a controller or DTO changes, the developer executes:
   ```powershell
   powershell -ExecutionPolicy Bypass -File scripts/export-openapi.ps1
   ```
2. The script updates `contracts/openapi.json`.
3. Frontend developers or autonomous agents run:
   ```bash
   npx @openapitools/openapi-generator-cli generate -i ./contracts/openapi.json -g typescript-angular -o ./src/app/core/api-client
   ```
   Or use `ng-openapi-gen` to regenerate Angular models and HTTP services directly.
