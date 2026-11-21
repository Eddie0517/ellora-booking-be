# HANDOFF — việc cần dev làm

Các bước dưới đây chỉ người làm được (DNS, token, secret, `sudo`). Agent đã dừng
ở đây. Không tiếp tục deploy cho tới khi mọi mục được xác nhận `DONE`.

Trạng thái hiện tại: **P0–P2 đã xong trong repo**. P3 (VPS bootstrap) và P4
(deploy) chờ các mục dưới.

---

## 1. Cloudflare zone phải Active

**Trạng thái: DONE 2026-11-21** — zone Active, NS `peter.ns.cloudflare.com`
/ `colette.ns.cloudflare.com`. Giữ lại mục này làm bản ghi.

- **Việc**: xác nhận `ellorabooking.online` đã Active trong Cloudflare.
- **Vì sao cần người**: agent không có quyền vào tài khoản Cloudflare.
- **Kiểm tra**:

```bash
nslookup -type=NS ellorabooking.online
# phải trả về nameserver của Cloudflare (vd: *.ns.cloudflare.com)
```

Hoặc Dashboard → Websites → trạng thái **Active**.

- **Kết quả mong đợi**: zone Active, không phải Pending.
- Hiện tại DNS đang **NXDOMAIN** → chưa dùng được.

Xác nhận: `ZONE_DONE` hoặc `BLOCKED: <lý do>`.

---

## 2. Tạo Cloudflare Tunnel

**Trạng thái: DONE 2026-11-21** — tunnel `ellora-prod`
(`4242f655-f2a1-45cd-991d-bebeaf40c719`), route
`api.ellorabooking.online` → `http://api:8080`. Token: còn phải đưa vào
`.env.production` ở mục 3.

- **Việc**: theo [`cloudflare-tunnel.md`](cloudflare-tunnel.md) mục 1.
- **Cần**: tên tunnel `ellora-prod`, Public Hostname
  `api.ellorabooking.online` → service `HTTP` `api:8080`.
- **Lấy**: token (chuỗi sau `--token`).
- **Không gửi token qua chat.** Điền trực tiếp vào `.env.production` ở mục 3.

Xác nhận: `TUNNEL_DONE` (kèm tên tunnel, không kèm token).

---

## 3. Tạo `/opt/ellora` và `.env.production` trên VPS

`deploy` không có quyền ghi `/opt` và `sudo` cần password → phần này dev làm.

### 3.1 Tạo thư mục

```bash
ssh vps-hycat "sudo mkdir -p /opt/ellora /opt/ellora-backups && \
  sudo chown -R deploy:deploy /opt/ellora /opt/ellora-backups && \
  chmod 750 /opt/ellora && chmod 700 /opt/ellora-backups && \
  ls -ld /opt/ellora /opt/ellora-backups"
```

Kết quả mong đợi:

```text
drwxr-x--- ... deploy deploy ... /opt/ellora
drwx------ ... deploy deploy ... /opt/ellora-backups
```

### 3.2 Sinh JWT secret mới (không dùng lại secret local)

```powershell
bash scripts/jwt-keys.sh
# hoặc trên Windows:
powershell -ExecutionPolicy Bypass -File scripts/jwt-keys.ps1
```

### 3.3 Tạo `.env.production` tại máy local rồi upload

```powershell
Copy-Item .env.production.example .env.production
notepad .env.production      # điền toàn bộ giá trị
```

Các key **bắt buộc** phải có giá trị:

| Key | Lấy ở đâu |
|---|---|
| `POSTGRES_PASSWORD` | tự sinh (>= 24 ký tự) |
| `JWT_SECRET` | mục 3.2 |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Gmail + App Password 16 ký tự |
| `CLOUDINARY_*` | Cloudinary console |
| `PAYOS_*` | my.payos.vn |
| `GOOGLE_CLIENT_ID` | Google Cloud console |
| `CLOUDFLARE_TUNNEL_TOKEN` | mục 2 |
| `CORS_ALLOWED_ORIGINS` | giữ mặc định `https://ellorabooking.online,https://www.ellorabooking.online` |

Upload:

```powershell
scp .env.production vps-hycat:/opt/ellora/.env.production
ssh vps-hycat "chmod 600 /opt/ellora/.env.production && ls -l /opt/ellora/.env.production"
```

Kết quả mong đợi: `-rw------- ... deploy deploy ... .env.production`.

### 3.4 Kiểm tra đủ key

```bash
ssh vps-hycat "cd /opt/ellora && bash deployments/scripts/verify-env.sh .env.production"
```

Lệnh này cần `deployments/` đã được upload — workflow CD lần đầu sẽ làm, nên
bước này có thể chạy sau khi merge.

Xác nhận: `ENV_DONE`.

---

## 4. GHCR PAT để VPS pull image private

- **Việc**: tạo Personal Access Token (classic).
- GitHub → Settings → Developer settings → Tokens (classic) → Generate new token.
- Scope: **`read:packages`** (chỉ vậy).
- Lưu ở đâu: GitHub Environment secret `GHCR_READ_TOKEN` (mục 6).
- Không dán token vào chat.

Xác nhận: `PAT_DONE`.

---

## 5. SSH key riêng cho GitHub Actions

Không dùng lại key cá nhân `artist-shop_admin_ed25519`.

```powershell
ssh-keygen -t ed25519 -f $env:USERPROFILE\.ssh\ellora_github_actions_ed25519 -C "github-actions-ellora" -N '""'
type $env:USERPROFILE\.ssh\ellora_github_actions_ed25519.pub | ssh vps-hycat "cat >> ~/.ssh/authorized_keys"
ssh-keyscan -p 22 160.22.107.140
```

- `DEPLOY_SSH_KEY` = nội dung **private** key
  (`Get-Content $env:USERPROFILE\.ssh\ellora_github_actions_ed25519 -Raw`)
- `DEPLOY_KNOWN_HOSTS` = output của `ssh-keyscan`

Kiểm tra key mới hoạt động:

```bash
ssh -i ~/.ssh/ellora_github_actions_ed25519 deploy@160.22.107.140 "hostname"
```

Xác nhận: `SSHKEY_DONE`.

---

## 6. GitHub Environment `production`

Repo → Settings → Environments → **New environment** → `production`.

### Secrets

| Secret | Giá trị |
|---|---|
| `DEPLOY_HOST` | `160.22.107.140` |
| `DEPLOY_PORT` | `22` |
| `DEPLOY_USER` | `deploy` |
| `DEPLOY_SSH_KEY` | private key ở mục 5 |
| `DEPLOY_KNOWN_HOSTS` | output `ssh-keyscan` |
| `GHCR_USERNAME` | `Eddie0517` |
| `GHCR_READ_TOKEN` | PAT ở mục 4 |

### Variables

| Variable | Giá trị |
|---|---|
| `APP_PATH` | `/opt/ellora` |
| `COMPOSE_FILE` | `deployments/docker-compose.yml` |
| `ENV_FILE` | `.env.production` |
| `DEPLOY_FILE` | `.env.production.deploy` |
| `HEALTH_URL` | `https://api.ellorabooking.online/healthz` |
| `APP_SERVICES` | `api` |
| `INFRA_SERVICES` | `postgres redis` |
| `PROXY_SERVICE` | `cloudflared` |
| `IMAGE_PREFIX` | `ghcr.io/eddie0517/ellora` |

Xác nhận: `GITHUB_ENV_DONE`.

---

## 7. Bảo vệ nhánh `main`

Repo → Settings → Branches → Add branch protection rule:

- Branch name pattern: `main`
- Require a pull request before merging (1 approval — có thể tự approve)
- Require status checks to pass: chọn `CI — ellora / checks`
- Do not allow force pushes
- Do not allow deletions

Xác nhận: `BRANCH_PROTECTION_DONE`.

---

## 8. Review + merge PR

Agent đã tạo PR `chore/deploy-foundation → main`. Sau khi CI xanh và bạn review:

```bash
gh pr merge --squash    # hoặc merge trên GitHub UI
```

Xác nhận: `PR_MERGED`.

---

## 9. Deploy lần đầu (sau khi merge)

```bash
git checkout main
git pull --ff-only
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

Theo dõi: GitHub → Actions → `CD — ellora production`.

Sau khi xanh, verify:

```bash
curl -fsS https://api.ellorabooking.online/healthz
```

Xác nhận: `FIRST_DEPLOY_DONE`.

---

## 10. Việc bắt buộc ngay sau deploy đầu

1. **Đổi mật khẩu admin mặc định** `admin@ellora.vn` / `admin`
   (`V3__seed_default_admin.sql` tạo sẵn).
2. Đăng ký webhook PayOS:
   `https://api.ellorabooking.online/api/v1/webhooks/payos`.
3. Kiểm tra smoke nghiệp vụ: register → login → refresh → logout → booking.
4. Cài cron backup trên VPS:

```bash
ssh vps-hycat 'crontab -l > /tmp/ct 2>/dev/null; \
  echo "30 2 * * * cd /opt/ellora && bash deployments/scripts/backup-db.sh production >> /home/deploy/ellora-backup.log 2>&1" >> /tmp/ct; \
  sort -u /tmp/ct | crontab -; crontab -l | grep ellora'
```

5. Chạy thử backup một lần:

```bash
ssh vps-hycat "cd /opt/ellora && bash deployments/scripts/backup-db.sh production"
```

6. Ghi kết quả vào [`DEPLOY_REPORT.md`](DEPLOY_REPORT.md).

Xác nhận: `POST_DEPLOY_DONE`.

---

## Ghi chú bảo mật

- Không gửi private key, token, password qua chat.
- Đặt secret vào đúng đường dẫn, mode 600, rồi chỉ xác nhận `DONE`.
- Không dán output có chứa secret.
