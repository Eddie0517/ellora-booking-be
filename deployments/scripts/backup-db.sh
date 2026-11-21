#!/usr/bin/env bash
# Backup database của Ellora ra file .sql.gz + SHA256SUMS.
#
# usage: backup-db.sh <env>
#
# Env tuỳ chọn:
#   APP_ROOT       gốc deploy (mặc định: hai cấp trên thư mục script)
#   ENV_FILE       mặc định .env.<env>
#   DEPLOY_FILE    mặc định .env.<env>.deploy
#   COMPOSE_FILE   mặc định deployments/docker-compose.yml
#   BACKUP_DIR     mặc định /opt/ellora-backups
#   KEEP_DAYS      mặc định 7
set -Eeuo pipefail

usage() {
  cat >&2 <<'EOF'
usage: backup-db.sh <env>
EOF
  exit 64
}

fail() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

[ "$#" -eq 1 ] || usage
env_name=$1

script_dir=$(unset CDPATH; cd -- "$(dirname -- "$0")" && pwd)
APP_ROOT=${APP_ROOT:-$(cd -- "${script_dir}/../.." && pwd)}
cd "$APP_ROOT"

ENV_FILE=${ENV_FILE:-.env.$env_name}
DEPLOY_FILE=${DEPLOY_FILE:-.env.$env_name.deploy}
COMPOSE_FILE=${COMPOSE_FILE:-deployments/docker-compose.yml}
KEEP_DAYS=${KEEP_DAYS:-7}

[ -f "$ENV_FILE" ] || fail "env file not found: $ENV_FILE"
[ -f "$COMPOSE_FILE" ] || fail "compose file not found: $COMPOSE_FILE"

set -a
# shellcheck disable=SC1090
. "$ENV_FILE"
set +a

: "${POSTGRES_DB:?POSTGRES_DB is required}"
: "${POSTGRES_USER:?POSTGRES_USER is required}"
: "${POSTGRES_PASSWORD:?POSTGRES_PASSWORD is required}"

BACKUP_DIR=${BACKUP_DIR:-/opt/ellora-backups}
base="${BACKUP_DIR}/${env_name}"
ts=$(date +%Y%m%d-%H%M%S)
out="${base}/${ts}"
mkdir -p "$out"
chmod 700 "$base" "$out"

dc=(docker compose --env-file "$ENV_FILE")
if [ -f "$DEPLOY_FILE" ]; then
  dc+=(--env-file "$DEPLOY_FILE")
fi
dc+=(-f "$COMPOSE_FILE")

printf '==> backup %s -> %s\n' "$env_name" "$out"

"${dc[@]}" exec -T \
  -e PGPASSWORD="$POSTGRES_PASSWORD" \
  postgres pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  --no-owner --no-privileges |
  gzip -9 > "${out}/ellora.sql.gz"

(cd "$out" && sha256sum ellora.sql.gz > SHA256SUMS)

{
  printf 'env=%s\n' "$env_name"
  printf 'database=%s\n' "$POSTGRES_DB"
  printf 'created=%s\n' "$(date -Iseconds)"
} > "${out}/manifest.txt"

chmod 600 "${out}"/*.sql.gz "${out}/SHA256SUMS" "${out}/manifest.txt" 2>/dev/null || true

# Retention: chỉ xoá trong thư mục của env này.
find "$base" -mindepth 1 -maxdepth 1 -type d -mtime +"$KEEP_DAYS" -exec rm -rf {} + || true

printf '==> backup complete: %s\n' "$out"
