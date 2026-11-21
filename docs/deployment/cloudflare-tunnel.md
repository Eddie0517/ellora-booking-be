# Cloudflare Tunnel — hướng dẫn tạo

Backend được expose qua **Cloudflare Tunnel** thay cho nginx + cert trên VPS.
`cloudflared` chạy trong compose, mở kết nối outbound tới Cloudflare edge và
forward `https://api.ellorabooking.online` → `http://api:8080`.

Không cần: mở port, cấp cert, cron renew, sửa firewall.

## 0. Điều kiện

- Zone `ellorabooking.online` phải ở trạng thái **Active** trong Cloudflare
  (nameservers đã trỏ về Cloudflare). Kiểm tra: Dashboard → Websites → trạng
  thái phải là *Active*, không phải *Pending*.
- Không cần record A/AAAA cho `api` — tunnel sẽ tự tạo CNAME.

Nếu zone chưa Active: dừng lại. Không thể tạo Public Hostname.

## 1. Tạo tunnel (Dashboard)

1. Vào **Zero Trust** → **Networks** → **Tunnels** → **Create a tunnel**.
2. Chọn **Cloudflared**, đặt tên: `ellora-prod`.
3. Ở bước cài đặt, chọn **Docker** và copy **token** (chuỗi dài sau
   `--token`). Không cần chạy lệnh cài đặt trên VPS — compose đã có container.
4. Chuyển sang tab **Public Hostname** → **Add a public hostname**:
   - Subdomain: `api`
   - Domain: `ellorabooking.online`
   - Path: (để trống)
   - Service type: `HTTP`
   - URL: `api:8080`
5. Save.

Kết quả: Cloudflare tự tạo CNAME `api.ellorabooking.online` trỏ vào tunnel
(`<tunnel-id>.cfargotunnel.com`), proxied.

## 2. Đặt token lên VPS

Token là secret. Không gửi qua chat, không commit.

Trên máy local, mở file `.env.production` sẽ đặt lên VPS và điền:

```dotenv
CLOUDFLARE_TUNNEL_TOKEN=<token-copy-tu-dashboard>
```

Sau đó upload (xem `HANDOFF.md`), đảm bảo mode 600.

## 3. Khởi động

Lần deploy đầu, workflow CD sẽ `up -d` service `cloudflared`. Kiểm tra:

```bash
ssh vps-hycat "cd /opt/ellora && \
  docker compose --env-file .env.production --env-file .env.production.deploy \
  -f deployments/docker-compose.yml logs --tail=50 cloudflared"
```

Log tốt có dạng:

```text
INF Registered tunnel connection connIndex=0 ...
```

## 4. Kiểm tra public

```bash
curl -fsS https://api.ellorabooking.online/healthz
# {"status":"UP","timestamp":"..."}
```

## 5. Xoay token / tạo lại tunnel

Nếu token lộ hoặc tunnel hỏng:

1. Dashboard → Tunnel `ellora-prod` → **Refresh token** (hoặc tạo tunnel mới).
2. Cập nhật `CLOUDFLARE_TUNNEL_TOKEN` trong `/opt/ellora/.env.production`.
3. `docker compose ... up -d --force-recreate cloudflared`.
4. Nếu tạo tunnel mới: thêm lại Public Hostname `api.ellorabooking.online`
   → `http://api:8080`.

## 6. Lưu ý

- `cloudflared` phải ở cùng Docker network với `api` — compose đã đảm bảo.
- Không đổi URL service thành `http://localhost:8080`: trong container
  `cloudflared`, `localhost` là chính nó.
- Cloudflare Access (Zero Trust) **không** được bật cho hostname này, vì
  PayOS webhook và frontend cần gọi công khai.
