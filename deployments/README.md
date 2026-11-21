# deployments/

Tài nguyên triển khai production của Ellora Booking Backend.

| File | Dùng cho |
|---|---|
| `profile.env` | Giá trị không secret, máy đọc (nguồn: `docs/deployment/DECISIONS.md`) |
| `docker-compose.yml` | Stack production: `postgres`, `redis`, `api`, `cloudflared` |
| `scripts/deploy-release.sh` | Rollout image theo digest, rollback tự động khi lỗi |
| `scripts/verify-env.sh` | Kiểm tra `.env.production` đủ key trước khi deploy |
| `scripts/backup-db.sh` | `pg_dump` hằng ngày, giữ 7 ngày |
| `scripts/restore-db.sh` | Restore có xác nhận, dùng khi có sự cố |

Không có `proxy/` và không có cert: TLS nằm ở Cloudflare edge, ingress đi qua
Cloudflare Tunnel (xem `docs/deployment/cloudflare-tunnel.md`).

## Cấu trúc trên VPS

```text
/opt/ellora/
├── .env.production          # secret thật, mode 600, KHÔNG trong Git
├── .env.production.deploy   # digest image, do CD ghi
├── .env.production.deploy.prev
└── deployments/             # thư mục này, do CD upload

/opt/ellora-backups/production/<timestamp>/ellora.sql.gz
```

## Lệnh thường dùng

```bash
cd /opt/ellora

# trạng thái
docker compose --env-file .env.production --env-file .env.production.deploy \
  -f deployments/docker-compose.yml ps

# log
docker compose --env-file .env.production --env-file .env.production.deploy \
  -f deployments/docker-compose.yml logs -f --tail=200 api

# validate trước khi thao tác
bash deployments/scripts/verify-env.sh .env.production

# backup thủ công
bash deployments/scripts/backup-db.sh production
```
