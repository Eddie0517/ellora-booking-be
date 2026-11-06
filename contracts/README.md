# Ellora API Contracts & OpenAPI Specification

> **Nguồn chân lý hợp đồng API (Single Source of Truth) giữa Backend và Frontend.**
> Được tham chiếu trực tiếp bởi các nhà phát triển và AI Agent Frontend.

## 1. OpenAPI Specification

- **File đặc tả chính**: [`openapi.json`](./openapi.json)
- **Chuẩn**: OpenAPI 3.0.1
- **Định dạng lỗi**: [RFC 9457 Problem Details](https://datatracker.ietf.org/doc/html/rfc9457) (`application/problem+json`)
- **Phản hồi thành công**: Trả DTO trực tiếp (bỏ `ApiResponse<T>`)

## 2. Quy trình đồng bộ hóa sang Frontend (Angular)

### Cách 1: Tự động sinh mã qua OpenAPI Generator CLI
```bash
npx @openapitools/openapi-generator-cli generate \
  -i ./contracts/openapi.json \
  -g typescript-angular \
  -o ./src/app/core/api-client
```

### Cách 2: Tự động sinh mã qua ng-openapi-gen (tối ưu Angular Standalone & Signals)
```bash
npx ng-openapi-gen -i ./contracts/openapi.json -o ./src/app/core/api
```

## 3. Cập nhật đặc tả từ Backend

Mỗi khi bổ sung hoặc sửa đổi Controller trong `ellora-api`, chạy lệnh sau để dump bản đặc tả mới:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/export-openapi.ps1
```
