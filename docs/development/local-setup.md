# Local Setup Guide — Ellora Booking Backend

> Developer environment setup instructions for running, debugging, and testing `ellora-booking-be` using 100% containerized Docker workflows.

---

## 1. Prerequisites

- **Docker Desktop** (or Docker Engine on Linux):
  - Docker v20.10+ (v29+ verified).
  - Docker Compose v2+ (v5.5+ verified).
- **Git**: Configured locally with required author identity:
  ```powershell
  git config user.name "Eddie0517"
  git config user.email "khang17122005@gmail.com"
  ```
- **Zero Host Java Requirement**:
  - You do NOT need to install JDK 17, JRE, or Maven on your operating system. Docker handles compilation, testing, and runtime environments entirely.

---

## 2. Step-by-Step Setup

### Step 1: Clone Repository
```powershell
git clone <repo-url> ellora-booking-be
cd ellora-booking-be
```

### Step 2: Configure Environment Variables
Copy the template file to create your active local `.env`:
```powershell
# Windows PowerShell
Copy-Item .env.example .env

# Bash / Linux / macOS
cp .env.example .env
```

### Step 3: Generate Local JWT Cryptographic Secret
Run the automated key generation script:
```powershell
# Windows PowerShell
powershell -ExecutionPolicy Bypass -File scripts/jwt-keys.ps1

# Bash / Linux / macOS
bash scripts/jwt-keys.sh
```
Copy the generated secret and set `JWT_SECRET=<your-secret>` in `.env`.

### Step 4: Configure External Credentials in `.env` (Optional for basic development)
If you wish to test real email sending or image uploads:
- **Mail (Gmail SMTP)**:
  - `MAIL_USERNAME=your-email@gmail.com`
  - `MAIL_PASSWORD=your-google-16-char-app-password`
  - *(Note: Local development defaults to Mailpit on port 1025; emails are visible at `http://localhost:8025`)*
- **Cloudinary (Image Storage)**:
  - `CLOUDINARY_CLOUD_NAME=your_cloud_name`
  - `CLOUDINARY_API_KEY=your_api_key`
  - `CLOUDINARY_API_SECRET=your_api_secret`

---

## 3. Launching the Container Stack

Start all core services in detached mode:
```powershell
docker compose up --build -d
```

### Verify Service Status:
```powershell
docker compose ps
```
All containers should report `healthy` or `running`:
- `ellora-postgres` (PostgreSQL 17 on port 5432)
- `ellora-redis` (Redis 7 on port 6379)
- `ellora-backend` (Spring Boot API on port 8080)

### Check Backend Health:
```powershell
curl -s http://localhost:8080/healthz
```
Expected output:
```json
{"status":"UP","timestamp":"2026-11-06T..."}
```

---

## 4. Useful Operational Commands

| Action | Command |
|---|---|
| **View Backend Logs** | `docker compose logs -f backend` |
| **Run Tests in Docker** | `docker compose run --rm backend mvn test` |
| **Verify Flyway Tables** | `docker compose exec postgres psql -U ellora -d ellora -c "\dt"` |
| **Access Redis CLI** | `docker compose exec redis redis-cli ping` |
| **Stop All Containers** | `docker compose down` |
| **Reset Database & Cache** | `docker compose down -v` |
| **Re-export OpenAPI Spec** | `powershell -ExecutionPolicy Bypass -File scripts/export-openapi.ps1` |

---

## 5. Web Dashboards & Tooling

- **API Liveness**: `http://localhost:8080/healthz`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Mailpit Web Interface**: `http://localhost:8025`
- **PGAdmin (Optional)**:
  Start with `docker compose --profile tools up -d`, then access `http://localhost:5050` (Login: `admin@ellora.vn` / `ellora_admin_secret`).

---

## 6. Troubleshooting Common Issues

### Port Conflicts (5432, 6379, 8080 already in use):
If you have local PostgreSQL or Redis installed on the host:
- Modify `DB_PORT`, `REDIS_PORT`, or `SERVER_PORT` in `.env` (e.g. `DB_PORT=5433`).
- Docker Compose automatically maps to the customized host ports.

### Docker Daemon Not Running:
- Ensure Docker Desktop is launched and running in Linux containers mode.

### Database Connection Refused:
- PostgreSQL container takes ~5–10 seconds to initialize on a clean volume. The backend container uses `depends_on: postgres: condition: service_healthy` to wait for database readiness before booting.
