# DEPLOY REPORT — ellora-booking-be

Ghi sau mỗi phase và mỗi lần deploy. Không chứa secret.

---

## 2026-11-21 18:29–18:38 (+07) — production (deploy đầu tiên)

- Commit: `a08232a` (merge PR #4)
- Tag: `v1.0.1` (tag `v1.0.0` fail, xem "Sai lệch")
- Người thực hiện: agent + Eddie0517
- Người approve: Eddie0517
- Run: `CD — ellora production` #44899133836 (re-run sau khi sửa DNS)

**Image digest**

```text
api=ghcr.io/eddie0517/ellora-api@sha256:4712bdde7933e6c921a37ee84f049494f39e0b91cdd5c623c3ed71314c0eed78
```

**Digest trước đó (rollback)**

```text
không có — đây là deploy đầu tiên; .env.production.deploy.prev giữ cùng digest
```

**Lệnh đã chạy (thay đổi trạng thái)**

```bash
git tag -a v1.0.1 -m "Release v1.0.1" && git push origin v1.0.1
# GitHub Actions: build → push GHCR → SSH → verify env → pin digest → deploy-release.sh
# Trên VPS: docker compose up -d postgres redis; up -d --no-deps api; force-recreate cloudflared
# Cài cron backup 02:30 và chạy backup thủ công 1 lần
```

**Kết quả smoke**

| Lớp | Kết quả | Ghi chú |
|---|---|---|
| Container | ✅ | api/postgres/redis healthy, cloudflared Up |
| Health nội bộ | ✅ | `{"status":"UP"}` từ trong network |
| Health qua tunnel | ✅ | `https://api.ellorabooking.online/healthz` → 200 |
| Public HTTPS | ✅ | Cloudflare edge, TLS hợp lệ |
| CORS | ✅ | preflight 200, `access-control-allow-origin: https://ellorabooking.online` |
| Migration | ✅ | Flyway V1–V11, 27 bảng, `success=t` |
| Auth | ✅ | login sai → 401 Problem Details; endpoint bảo vệ → 401 |
| Validation | ✅ | register sai → 400 + `invalidParams` |
| Swagger | ✅ | 404 (đã tắt ở prod) |
| Actuator | ✅ | chỉ `status` + `groups`, không lộ chi tiết DB |
| Backup | ✅ | `/opt/ellora-backups/production/20261121-183853/ellora.sql.gz` |
| Tài nguyên | ✅ | api 319MiB/768, pg 50/384, redis 8/128, cf 16/128; RAM còn 2.0Gi; disk còn 17G |
| Stack khác | ✅ | artist-shop, closy, littlecafe ×2, portainer giữ nguyên trạng thái và port |

**Sai lệch / xử lý**

1. Lần deploy `v1.0.0` fail: `no such service: db-init`.
   Nguyên nhân: `deploy-release.sh` dùng `${VAR:-default}`, nên
   `DB_INIT_SERVICE=''` vẫn fallback `db-init`.
   Sửa: PR #4 — unset giữ default, rỗng hoặc `none` thì tắt phase.
2. Smoke fail sau khi deploy: DNS `api` là CNAME DNS-only tới
   `cfargotunnel.com` (không resolve công khai).
   Sửa: bật Proxy (orange cloud) cho record `api`.
3. `MAIL_PASSWORD` trong `.env` local có dấu cách (Gmail hiển thị 4 nhóm 4 ký tự).
   Sửa: bỏ dấu cách khi tạo `.env.production`; script đổi sang parse env thay vì `source`.

**Việc còn treo**

- Đổi mật khẩu admin mặc định `admin@ellora.vn` / `admin`.
- Đăng ký webhook PayOS: `https://api.ellorabooking.online/api/v1/webhooks/payos`.
- Bật branch protection cho `main` (cần quyền admin repo).
- Chưa có monitoring/alert.
- Chưa thử rollback bằng digest trên production.

---

## Mẫu một lần deploy

### \<YYYY-MM-DD HH:MM\> — \<env\>

- Commit:
- Tag (nếu có):
- Người thực hiện:
- Người approve:
- Bắt đầu / kết thúc:

**Image digest**

```text
api=ghcr.io/eddie0517/ellora-api@sha256:...
```

**Digest trước đó (rollback)**

```text
api=ghcr.io/eddie0517/ellora-api@sha256:...
```

**Lệnh đã chạy**

```bash
# chỉ các lệnh thay đổi trạng thái
```

**Kết quả smoke**

| Lớp | Kết quả | Ghi chú |
|---|---|---|
| Container | | |
| Health nội bộ | | |
| Public HTTPS (tunnel) | | |
| CORS | | |
| Migration (Flyway) | | |
| Tài nguyên (`free -h`, `df -h`, `docker stats`) | | |
| Stack khác không đổi | | |

**Sai lệch / xử lý**

-

**Việc còn treo**

-

---

## Mẫu sự cố

### \<YYYY-MM-DD HH:MM\> — \<env\>

- Triệu chứng:
- Phạm vi ảnh hưởng:
- Phát hiện bởi:
- Nguyên nhân gốc:
- Hành động khắc phục:
- Rollback (nếu có):
- Thời gian phục hồi:
- Phòng ngừa lần sau:
