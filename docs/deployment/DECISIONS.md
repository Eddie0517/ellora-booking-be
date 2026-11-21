# DECISIONS — ellora-booking-be

Nguồn sự thật cho mọi giá trị triển khai. Agent đọc file này trước khi làm bất cứ
gì; không đoán giá trị thiếu.

Trạng thái: `Confirmed` (đã chốt), `Proposed` (agent đề xuất, chờ chốt),
`Undecided` (chưa có).

## 1. Dự án

| Mục | Giá trị | Trạng thái |
|---|---|---|
| App slug | `ellora` | Confirmed |
| Repo / owner | `github.com/Eddie0517/ellora-booking-be` | Confirmed |
| Visibility | Public | Confirmed |
| Một service / monorepo | Monolith, **1 service deploy**: `api` | Confirmed |
| Service và stack | `api` — Java 17, Spring Boot 3.2.5, Maven multi-module | Confirmed |
| Người release | Eddie0517 | Confirmed |
| Cửa sổ release | Bất kỳ, downtime ngắn (~1 phút) chấp nhận được | Confirmed |

## 2. Môi trường

| Env | Mục đích | Người dùng | Dữ liệu | Provider | Trigger | Approval |
|---|---|---|---|---|---|---|
| production | Phục vụ người dùng thật | End user + FE | Thật | VPS `vps-hycat` | annotated tag `v*` | GitHub Environment `production` (self-review) |

Chỉ một môi trường. Bù lại, mọi thay đổi phải qua CI xanh trước khi tag.

## 3. Hạ tầng

| Mục | Giá trị | Trạng thái |
|---|---|---|
| VPS provider | (đã có) | Confirmed |
| IP / SSH alias | `160.22.107.140` / `vps-hycat`, user `deploy` | Confirmed |
| OS | Ubuntu, kernel 5.15 x86_64 | Confirmed |
| RAM / disk | 3.8 GiB (2.4 GiB available), 40 GB (18 GB free) | Confirmed |
| Riêng hay dùng chung | **Dùng chung** với `closy-prod`, `littlecafe-staging`, `littlecafe-prod`, `artist-shop-prod`, `portainer` | Confirmed |
| Stack khác đang chạy | 80/443 (closy), 8081/8443, 8082/8444, 8083/8445, postgres 5432–5434 (loopback) | Confirmed |
| Firewall quản ở đâu | Panel provider (Docker bypass ufw) | Confirmed |
| Proxy | **Không dùng reverse proxy trên VPS** | Confirmed |
| TLS model | **Cloudflare Edge** (Cloudflare Tunnel) | Confirmed |
| ACME email | Không áp dụng | Confirmed |
| DNS provider | Cloudflare, zone `ellorabooking.online` | Confirmed |
| Cloudflare Tunnel | `ellora-prod` — ID `4242f655-f2a1-45cd-991d-bebeaf40c719` | Confirmed |
| Ai giữ token DNS / tunnel | Eddie0517, đặt trên VPS | Confirmed |

### 3.1 Mô hình expose: Cloudflare Tunnel

Lý do: 80/443 đã bị `closy-prod` chiếm; Cloudflare proxy chỉ forward HTTPS trên
443/2053/2083/2087/2096/8443; port 8443 cũng đã bị chiếm. Cloudflare Tunnel
không cần mở port, không cần cert trên VPS, không cần cron renew.

Hệ quả:

- Container `cloudflared` chạy outbound-only, route `api.ellorabooking.online`
  → `http://api:8080` trong network nội bộ của compose.
- TLS do Cloudflare edge đảm nhiệm; origin là HTTP trong Docker network.
- **Không publish port nào ra host.**
- Không có `nginx`/`lego`/`certbot` trong stack này.

## 4. Domain và port

| Env | Domain | HTTP port | HTTPS port | Path | Health URL |
|---|---|---|---:|---:|---|---|
| production | `api.ellorabooking.online` | — (tunnel) | — (Cloudflare edge) | `/opt/ellora` | `https://api.ellorabooking.online/healthz` |

Không có host port. Container `api` chỉ lắng nghe `8080` trong network nội bộ
`ellora-prod_app`.

## 5. Database

| Mục | Giá trị |
|---|---|
| Engine / version | PostgreSQL 17 (`postgres:17-alpine`) |
| Mỗi service một DB | Một database `ellora`, một role `ellora` |
| Mapping | `POSTGRES_DB=ellora`, `POSTGRES_USER=ellora` |
| Migration tool | **Flyway**, chạy trong app lúc khởi động (`spring.flyway.enabled=true`) |
| Seed data | `V3__seed_default_admin.sql` tạo `admin@ellora.vn` — **phải đổi mật khẩu ngay sau deploy đầu** |
| Backup: nơi lưu | `/opt/ellora-backups` (ngoài thư mục deploy) |
| Backup: tần suất / retention | Hằng ngày 02:30, giữ 7 ngày |
| RPO / RTO | RPO 24h / RTO ~30 phút (restore từ dump) |
| Ai được restore production | Eddie0517, cần backup mới trước khi restore |

**Deviation có chủ đích**: Flyway chạy trong app thay vì bước riêng. Chấp nhận
được vì chỉ có 1 instance; nếu migration lỗi thì app không start, healthcheck
fail, `up --wait` fail và `deploy-release.sh` tự rollback. Nếu sau này scale
>1 instance, phải tách bước migration và bật lock.

## 6. Secret

| Mục | Giá trị |
|---|---|
| Model | **GitHub Environment secrets (CD) + file `.env.production` trên VPS (app)** |
| Đường dẫn trên VPS | `/opt/ellora/.env.production` (mode 600, owner deploy) |
| Secret CD | GitHub Environment `production`: `DEPLOY_*`, `GHCR_*` |
| Ai giữ | Eddie0517 |
| Chu kỳ rotate | `JWT_SECRET` 6 tháng hoặc khi nghi lộ; tunnel token khi nghi lộ |
| Quy trình thu hồi | Xoá secret khỏi GitHub Environment; rotate tunnel token trong Cloudflare; đổi `JWT_SECRET` (buộc logout toàn bộ) |

`deployments/profile.env` **không chứa secret**. `.env.production` không nằm
trong Git và **không bị CD ghi đè**.

## 7. Registry và CI

| Mục | Giá trị |
|---|---|
| Registry | GHCR |
| Image prefix | `ghcr.io/eddie0517/ellora` |
| Image | `ghcr.io/eddie0517/ellora-api:production-<sha>` |
| Deploy | bằng **digest** (`@sha256:...`), ghi vào `.env.production.deploy` |
| Package visibility | Private |
| PAT read owner | `Eddie0517`, scope `read:packages` |
| CI gate | `mvn -B verify` (Spotless + unit test + ArchUnit) + `docker build` + gitleaks |
| Branch → env | `main` + tag `v*` → production |

## 8. Vận hành

| Mục | Giá trị |
|---|---|
| Log retention | Docker `json-file`, `max-size=10m`, `max-file=3` |
| Monitoring / alert | Chưa có (ghi nhận, không bỏ trống) |
| Kênh cảnh báo | Chưa có |
| Cert renewal | Không cần (Cloudflare edge) |
| Backup cron | `30 2 * * *` (tránh 03:00 của littlecafe/artist-shop) |
| Rollback owner | Eddie0517, dùng `.env.production.deploy.prev` |

## 9. Câu hỏi mở

| # | Câu hỏi | Ảnh hưởng | Tạm thời |
|---|---|---|---|
| 1 | Zone `ellorabooking.online` đã Active trên Cloudflare chưa? (hiện DNS NXDOMAIN) | Chặn P3/P4: không tạo được tunnel route và không có public HTTPS | Chờ dev xác nhận; không deploy production trước khi zone Active |

## 10. Lịch sử quyết định

| Ngày | Quyết định | Lý do | Người chốt |
|---|---|---|---|
| 2026-11-21 | 4 Maven module Clean Architecture L2 | Tách layer, ArchUnit enforce | Eddie0517 |
| 2026-11-21 | JWT HS256 tự quản lý | Đơn giản hoá so với ES256, đủ cho monolith | Eddie0517 |
| 2026-11-21 | Chỉ 1 môi trường production | 1 dev, chưa có người dùng thật, VPS 3.8 GiB | Eddie0517 |
| 2026-11-21 | Cloudflare Tunnel thay nginx/lego | 80/443 và 8443 đều bị chiếm; tunnel không cần port/cert | Eddie0517 |
| 2026-11-21 | Postgres container trên VPS | Tránh phụ thuộc SaaS, dữ liệu nằm cùng VPS | Eddie0517 |
| 2026-11-21 | App secret ở VPS, CD secret ở GitHub | Tách quyền, không commit secret, không cần SOPS | Eddie0517 |
