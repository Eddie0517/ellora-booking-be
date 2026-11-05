# Frontend Integration Guide — eddie-001-foundation-setup

> Hướng dẫn tích hợp Frontend cho Phase Foundation Setup của Ellora Nail Salon Booking Backend.
> Tài liệu này chuẩn hóa hợp đồng giao tiếp API, cấu trúc lỗi RFC 9457 Problem Details, và cơ chế tự động sinh mã client cho Angular.

## 1. Summary

- Feature/spec: `specs/eddie-001-foundation-setup/spec.md`
- Requirement: `specs/eddie-001-foundation-setup/spec.md#functional-requirements` (FR-008, FR-009)
- Handoff status: `READY_FOR_FRONTEND`
- Canonical contracts: `contracts/openapi.json` (OpenAPI 3.0.1)
- Counts: new endpoints 1 · changed 0 · removed 0 · events 0 · DTOs 4

Status rules:
- `READY_FOR_FRONTEND`: Hợp đồng OpenAPI đã được cấu hình và xuất tự động, test và schema đồng bộ hoàn toàn.

## 2. Authentication and behavior

- **Authentication/authorization**:
  - Chuẩn: BearerAuth JWT (HMAC-SHA256 / ES256).
  - Header: `Authorization: Bearer <token>`.
  - Phân quyền: Public endpoints (`/healthz`, `/v3/api-docs/**`, `/swagger-ui/**`, `/actuator/**`) không yêu cầu token xác thực (`permitAll`).
- **Idempotency**:
  - `GET /healthz`: Idempotent và an toàn (Safe/Idempotent GET request), không gây tác dụng phụ tới trạng thái cơ sở dữ liệu.
- **Pagination**:
  - Định dạng phân trang chuẩn `PagedResult<T>` dùng chung cho toàn bộ danh sách truy vấn:
    - `items`: Danh sách phần tử dữ liệu trang hiện tại.
    - `page`: Chỉ số trang hiện tại (1-based index).
    - `pageSize`: Số lượng phần tử tối đa trên mỗi trang.
    - `totalItems`: Tổng số lượng bản ghi khớp điều kiện lọc.
    - `totalPages`: Tổng số trang được tính toán (`ceil(totalItems / pageSize)`).
- **Realtime/SSE behavior and REST fallback**:
  - Không áp dụng cho Phase Foundation (N/A). Giao tiếp thời gian thực sẽ được bổ sung ở các phase tính năng sau.

## 3. Endpoints

| Method | Path | Status (new/changed/removed) | Before → After (changed only) |
|---|---|---|---|
| `GET` | `/healthz` | new | N/A → Trả về `HealthStatus(status: "UP", timestamp: Instant)` |

- **Canonical schema reference**: `#/components/schemas/HealthStatus`
- **Response status codes**:
  - `200 OK`: Hệ thống hoạt động bình thường (`{"status": "UP", "timestamp": "2026-11-06T02:00:00Z"}`).
  - `500 Internal Server Error`: Sự cố hạ tầng nội bộ, trả về `ProblemDetail`.

## 4. Events

| Topic / routing key | Payload summary | Status (new/changed/removed) | Before → After |
|---|---|---|---|
| N/A | N/A | unchanged | Không có event message ở foundation phase |

## 5. Client-facing DTOs

| DTO | Field | Type | Status | Before → After |
|---|---|---|---|---|
| `HealthStatus` | `status` | `string` | new | Trạng thái hệ thống (`"UP"`) |
| `HealthStatus` | `timestamp` | `string` (`date-time`) | new | Thời điểm kiểm tra liveness theo chuẩn ISO-8601 UTC |
| `ProblemDetail` | `type` | `string` (`uri`) | new | URI định danh loại lỗi (ví dụ: `https://ellora.vn/errors/validation-error`) |
| `ProblemDetail` | `title` | `string` | new | Tóm tắt ngắn gọn lỗi bằng tiếng Việt |
| `ProblemDetail` | `status` | `integer` | new | Mã trạng thái HTTP chuẩn (ví dụ: `400`, `422`, `500`) |
| `ProblemDetail` | `detail` | `string` | new | Mô tả chi tiết nguyên nhân lỗi |
| `ProblemDetail` | `instance` | `string` | new | Đường dẫn URI của endpoint gây ra lỗi |
| `ProblemDetail` | `traceId` | `string` | new | UUID định danh vết request phục vụ tra cứu log |
| `ProblemDetail` | `invalidParams` | `InvalidParam[]` | new | Danh sách các tham số đầu vào không hợp lệ (nếu có) |
| `InvalidParam` | `name` | `string` | new | Tên trường/thuộc tính vi phạm validation |
| `InvalidParam` | `reason` | `string` | new | Lý do vi phạm quy tắc validation |
| `PagedResult` | `items` | `array` | new | Danh sách phần tử dữ liệu |
| `PagedResult` | `page` | `integer` | new | Chỉ số trang hiện tại |
| `PagedResult` | `pageSize` | `integer` | new | Số phần tử trên mỗi trang |
| `PagedResult` | `totalItems` | `integer` (`int64`) | new | Tổng số phần tử thỏa mãn điều kiện |
| `PagedResult` | `totalPages` | `integer` | new | Tổng số trang |

## 6. Enums

| Enum | Value | Status (new/changed/removed) |
|---|---|---|
| `BookingStatus` | `CONFIRMED` | new |
| `BookingStatus` | `PENDING_CONFIRMATION` | new |
| `BookingStatus` | `PENDING_DEPOSIT` | new |
| `BookingStatus` | `IN_SERVICE` | new |
| `BookingStatus` | `COMPLETED` | new |
| `BookingStatus` | `CANCELLED` | new |
| `BookingStatus` | `NO_SHOW` | new |
| `Role` | `ADMIN` | new |
| `Role` | `OWNER` | new |
| `Role` | `STAFF` | new |
| `Role` | `CUSTOMER` | new |
| `SalonStatus` | `ACTIVE` | new |
| `SalonStatus` | `INACTIVE` | new |
| `SalonStatus` | `PENDING_APPROVAL` | new |
| `SalonStatus` | `REJECTED` | new |
| `SalonStatus` | `SUSPENDED` | new |

## 7. Errors and compatibility

- **RFC 9457 Problem Details (`application/problem+json`)**:
  - Không sử dụng cấu trúc đóng gói kiểu cũ `ApiResponse<T>` (`{ success: true, data: ... }`).
  - Phản hồi thành công: trả trực tiếp DTO tương ứng.
  - Phản hồi lỗi: trả cấu trúc RFC 9457 `ProblemDetail`.
- **Mã lỗi chuẩn**:
  - `400 Bad Request`: Lỗi tham số cú pháp hoặc validation (`type: https://ellora.vn/errors/validation-error`, `https://ellora.vn/errors/domain-error`).
  - `422 Unprocessable Entity`: Vi phạm ràng buộc quy tắc nghiệp vụ (`type: https://ellora.vn/errors/business-rule-violation`).
  - `500 Internal Server Error`: Lỗi hệ thống nội bộ (`type: https://ellora.vn/errors/internal-server-error`).
- **Tự động sinh mã Frontend**:
  - Dùng `@openapitools/openapi-generator-cli` hoặc `ng-openapi-gen` trỏ vào file `contracts/openapi.json` để tự động cập nhật Angular models và services.

## 8. Sync checklist (fill counts before closing the feature)

- [x] Every JSON property the feature's DTOs emit appears above (count: 16)
- [x] Every value of every enum compared against its source constant block, in both directions (count: 16: 7 BookingStatus + 4 Role + 5 SalonStatus)
- [x] Every route compared after normalizing path params (`:param` vs `{param}`) (count: 1)
- [x] Every HTTP status claimed above is one the handler can actually return (count: 4: 200, 400, 422, 500)
