<!--
Sync Impact Report
==================
Version change: 2.1.0 (Longevia) -> 1.0.0 (Ellora reset)
Bump rationale: MAJOR - New project reset. Longevia-specific bindings removed
  (Identity/Diet/Progress/Recommendation, Nginx gateway roles, pgvector/RAG,
  Cloudinary/Google AI/Brevo). Stack and architecture marked TODO pending
  follow-up tasks. Core governance (Test-First, Contract-First, workflow,
  owner.json identity) preserved.

Modified principles:
  - I. Service Boundaries & Data Ownership (Longevia w/ Recommendation exception
    -> generic, TODO architecture)
  - II. Synchronous Inter-Service Calls via gRPC (gRPC-only -> contract-based,
    TODO chốt REST vs gRPC)
  - III. Asynchronous Communication via RabbitMQ (outbox+idempotency kept,
    broker TODO chốt)

Added sections: none.

Removed sections: none (Technology section generalized, not removed).

Deferred TODOs:
  - TODO(STACK): chốt ngôn ngữ/framework/DB cho ellora-booking-be
  - TODO(ARCH): chốt monolith vs microservices, gateway, broker
  - TODO(EXTERNAL): liệt kê dịch vụ ngoài (media, AI, email...) nếu có
-->

# Ellora Booking Backend Constitution

Booking service BE.

## Core Principles

### I. Service Boundaries & Data Ownership

Mỗi đơn vị triển khai sở hữu dữ liệu của chính nó.

- Mỗi service/module sở hữu database (hoặc schema) riêng và MUST chỉ đọc/ghi
  kho dữ liệu của chính nó. Truy cập chéo database của đơn vị khác là vi phạm
  nghiêm trọng, kể cả chỉ để đọc.
- Service MUST NOT import code nội bộ của service khác. Mọi phụ thuộc liên
  service đi qua contract (API, gRPC, event), không qua source.
- Một thay đổi schema chỉ được thực hiện bởi đơn vị sở hữu schema đó.
- Ranh giới service là quyết định cấp hiến pháp.
- TODO(ARCH): chốt monolith/modular vs microservices cho Ellora; nếu chỉ một
  service thì nguyên tắc này áp cho module boundaries.

Rationale: dữ liệu dùng chung qua DB là nguyên nhân gốc của coupling; sở hữu
dữ liệu riêng là điều kiện để các phần hệ thống tiến hóa độc lập.

### II. Synchronous Communication via Contract

Giao tiếp đồng bộ MUST qua contract có version.

- Client bên ngoài truy cập qua API công khai (REST/HTTP); đây là ranh giới
  công khai, không phải giao tiếp liên service nội bộ.
- Giao tiếp request/response giữa các đơn vị nội bộ MUST qua contract định
  nghĩa trước (OpenAPI cho REST hoặc `.proto` cho gRPC). Gọi trực tiếp không
  qua contract bị cấm.
- Call liên đơn vị MUST có deadline/timeout và MUST xử lý lỗi transport tường
  minh. Không giả định phía đích luôn sẵn sàng.
- Thay đổi contract là breaking change nếu phá vỡ consumer; xem §IV.
- TODO(ARCH): chốt REST vs gRPC cho giao tiếp nội bộ Ellora.

Rationale: một cơ chế contract tường minh giữ hợp đồng có kiểu và khả năng
tiến hóa rõ ràng; gọi ad-hoc làm hợp đồng trôi nổi.

### III. Asynchronous Communication via Events

Giao tiếp bất đồng bộ MUST qua broker event, publish và consume theo contract.

- Event MUST được publish trong cùng transaction nghiệp vụ với thay đổi dữ
  liệu (transactional outbox); MUST NOT publish rời rạc sau commit.
- Producer và consumer MUST chia sẻ một event contract có version; consumer
  MUST chịu được event lạ và bỏ qua an toàn (không vỡ khi field mới xuất hiện).
- Consumer MUST idempotent: xử lý trùng event không được tạo hiệu ứng phụ
  lần hai.
- TODO(ARCH): chốt broker (mặc định đề xuất RabbitMQ như Longevia) và danh
  sách event đầu tiên của Booking.

Rationale: event là hợp đồng giữa các đơn vị rời rạc; outbox và idempotency
bảo đảm không mất/không nhân đôi hiệu ứng khi lỗi hoặc retry.

### IV. Contract-First Interfaces

Mọi giao diện liên đơn vị MUST được định nghĩa bằng contract trước khi code.

- REST contract: OpenAPI. gRPC contract: file `.proto`. Event contract:
  schema event có version.
- Contract là nguồn chân lý; consumer MUST NOT phụ thuộc vào chi tiết nội bộ
  của producer (entity, bảng, cấu trúc module).
- Breaking change MUST đi kèm version mới và kế hoạch chuyển đổi; thay đổi
  tương thích ngược (thêm field optional) được khuyến khích.
- Không để lộ kiểu domain/persistence nội bộ ra ngoài contract công khai.

Rationale: contract tường minh cho phép các đơn vị deploy độc lập mà không
phá vỡ nhau.

### V. Test-First & Verified Gates (NON-NEGOTIABLE)

Mọi thay đổi hành vi MUST có kiểm thử, và gate MUST xanh trước khi commit.

- Viết test trước hoặc cùng lúc với code; test phải từng thấy đỏ vì đúng lý do
  trước khi thấy xanh (negative control).
- Gate đỏ là hard stop: MUST NOT nới gate, bỏ test, hay lách qua để có commit.
- Mỗi feature MUST có ít nhất một đường end-to-end thật chạy qua hạ tầng thật
  (DB thật, broker thật nếu dùng) và assert giá trị cụ thể, không chỉ
  "không lỗi".
- Gate chia theo độ lớn thay đổi (fast gate cho thay đổi nhỏ, full gate trước
  push/tag — chốt lệnh cụ thể khi stack được xác định).

Rationale: gate xanh chỉ chứng minh assertion đã viết đúng, không chứng minh
assertion là đúng cái cần; kiểm chứng thật là điều kiện để tin một feature
xong.

## Technology & Platform Constraints

- TODO(STACK): chốt stack cho Ellora (ngôn ngữ, framework, database, ORM).
  Mặc định đề xuất theo Longevia nếu không có lý do đổi: ASP.NET Core (.NET),
  PostgreSQL, EF Core Code First qua migrations; MUST NOT sửa database bằng tay.
- TODO(ARCH): chốt gateway công khai, broker, và ranh giới client-facing vs
  internal-only.
- TODO(EXTERNAL): liệt kê dịch vụ ngoài (media, AI, email...) — chỉ gọi qua
  adapter, không rải lời gọi trực tiếp khắp domain.
- Secret và cấu hình qua biến môi trường; comment trong mọi file tên bắt đầu
  bằng `.env` MUST viết bằng tiếng Anh.
- Ưu tiên lệnh chuẩn của repo (Makefile nếu có) hơn lệnh thủ công.

## Development Workflow & Quality Gates

- Task lớn/feature dùng Spec-kit; task nhỏ làm trực tiếp nhưng vẫn tuân Core
  Principles.
- Danh tính Spec-kit là cấu hình **máy-local** tại `.specify/owner.json`
  (gitignored), chỉ chứa handle ngắn chữ thường (2–20 ký tự `[a-z0-9-]`). Khi
  skill tạo feature mà file thiếu/trống, agent MUST dừng, hỏi dev handle, và
  ghi hộ bằng `scripts/speckit/team-feature.ps1 owner -Handle <handle>`; MUST
  NOT tự đoán handle hay tạo feature với owner rỗng.
- Không tự ý thao tác git khi chưa được cho phép; commit message tiếng Anh theo
  Conventional Commits.
- Tài liệu trong `specs/` MUST viết bằng tiếng Việt; đường dẫn tham chiếu MUST
  là tương đối.
- Khi tài liệu mâu thuẫn: Constitution > system-design > development > khác.
- Mọi plan MUST có Constitution Check; vi phạm một `MUST` là CRITICAL, MUST
  được sửa ở tài liệu chứ không được diễn giải lại cho qua.

## Governance

- Hiến pháp này thay thế mọi thực hành, quy ước và tài liệu khác khi có xung
  đột ở ranh giới kiến trúc và nguyên tắc.
- Sửa đổi hiến pháp: thay đổi MUST được ghi lại kèm lý do, cập nhật phiên bản
  theo semver, và cập nhật ngày. Việc sửa hiến pháp là hành động có chủ đích,
  tách khỏi công việc feature.
- Phiên bản: MAJOR khi bỏ/đổi nghĩa một nguyên tắc; MINOR khi thêm nguyên tắc
  hoặc mở rộng hướng dẫn; PATCH khi làm rõ câu chữ.
- Tuân thủ: mọi review và mọi `/speckit-plan` MUST kiểm tra tuân thủ; mọi vi
  phạm MUST được báo cáo và giải quyết, không im lặng bỏ qua.
- Dùng `AGENTS.md` làm hướng dẫn vận hành runtime khi có; hiến pháp là thẩm
  quyền cao nhất khi hai bên mâu thuẫn.

**Version**: 1.0.0 | **Ratified**: 2026-11-06 | **Last Amended**: 2026-11-06
