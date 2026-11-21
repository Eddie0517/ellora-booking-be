# Deploy — ellora-booking-be

Tài liệu triển khai của dự án. Quy trình chung nằm ở `_tools/deploy-playbook`;
file này chỉ ghi phần riêng của dự án và trỏ tới nguồn sự thật.

## 1. Nguồn sự thật

- Quyết định môi trường/hạ tầng: [`DECISIONS.md`](DECISIONS.md)
- Giá trị máy đọc: `deployments/profile.env`
- Hướng dẫn tạo tunnel: [`cloudflare-tunnel.md`](cloudflare-tunnel.md)
- Việc cần dev làm: [`HANDOFF.md`](HANDOFF.md)
- Báo cáo từng lần deploy: [`DEPLOY_REPORT.md`](DEPLOY_REPORT.md)

Không chép lại domain/port/secret vào file này.

## 2. Môi trường

| Env | Public URL | Path trên VPS | Trigger |
|---|---|---|---|
| production | `https://api.ellorabooking.online` | `/opt/ellora` | annotated tag `v*` |

## 3. Kiến trúc triển khai

```
Cloudflare Edge (TLS)  ──►  cloudflared (outbound)  ──►  api:8080
                                                        ├── postgres:17 (volume)
                                                        └── redis:7 (volume)
```

- Không publish port ra host.
- Không có nginx/cert trên VPS.
- Migration Flyway chạy trong app lúc khởi động.

## 4. Deploy

```bash
# 1. CI phải xanh trên main
git checkout main
git pull --ff-only

# 2. Tạo tag release
git tag -a vX.Y.Z -m "Release vX.Y.Z"
git push origin vX.Y.Z
```

Workflow `CD — ellora production` sẽ:

1. build image `ghcr.io/eddie0517/ellora-api:production-<sha>` và push GHCR;
2. upload `deployments/` lên `/opt/ellora`;
3. kiểm tra `.env.production` đủ key (không ghi đè);
4. ghi digest vào `.env.production.deploy` (giữ `.prev`);
5. `up -d --wait` cho `postgres` + `redis`, rồi `api`, rồi `cloudflared`;
6. smoke `https://api.ellorabooking.online/healthz`.

Sau deploy, verify tay:

```bash
curl -fsS https://api.ellorabooking.online/healthz
curl -i -X OPTIONS https://api.ellorabooking.online/api/v1/auth/login \
  -H "Origin: https://ellorabooking.online" \
  -H "Access-Control-Request-Method: POST"
```

## 5. Rollback

```bash
cd /opt/ellora
cp -a .env.production.deploy.prev .env.production.deploy
docker compose --env-file .env.production --env-file .env.production.deploy \
  -f deployments/docker-compose.yml up -d --wait --no-deps api
docker compose --env-file .env.production --env-file .env.production.deploy \
  -f deployments/docker-compose.yml up -d --force-recreate --no-deps cloudflared
```

Rollback schema: chỉ khi có backup mới, dùng `deployments/scripts/restore-db.sh`.

## 6. Vận hành

```bash
cd /opt/ellora

# trạng thái
docker compose --env-file .env.production --env-file .env.production.deploy \
  -f deployments/docker-compose.yml ps

# log
docker compose --env-file .env.production --env-file .env.production.deploy \
  -f deployments/docker-compose.yml logs -f --tail=200 api

# backup
bash deployments/scripts/backup-db.sh production
```

- Backup cron: `30 2 * * *`.
- Cert renewal: không cần (Cloudflare edge).
- Sự cố: xem `deploy-playbook/operations/troubleshooting.md`.

## 7. Liên hệ

| Vai trò | Người | Kênh |
|---|---|---|
| Release | Eddie0517 | — |
| VPS/DNS | Eddie0517 | — |
| Database | Eddie0517 | — |
