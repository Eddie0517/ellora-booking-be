#!/usr/bin/env bash
# Restore database của Ellora từ một file backup .sql.gz.
#
# usage: restore-db.sh <env> <file.sql.gz>
#
# CẢNH BÁO: ghi đè dữ liệu hiện tại của database trong compose của env đó.
# Chỉ chạy khi đã có backup mới và có approval của người sở hữu dữ liệu.
set -Eeuo pipefail

usage() {
  cat >&2 <<'EOF'
usage: restore-db.sh <env> <file.sql.gz>
EOF
  exit 64
}

fail() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }
confirm() {
  printf 'Type RESTORE to continue: ' >&2
  read -r answer
  [ "$answer" = "RESTORE" ] || fail "aborted by user"
}

[ "$#" -eq 2 ] || usage
env_name=$1
dump=$2

[ -f "$dump" ] || fail "dump file not found: $dump"

script_dir=$(unset CDPATH; cd -- "$(dirname -- "$0")" && pwd)
APP_ROOT=${APP_ROOT:-$(cd -- "${script_dir}/../.." && pwd)}
cd "$APP_ROOT"

ENV_FILE=${ENV_FILE:-.env.$env_name}
DEPLOY_FILE=${DEPLOY_FILE:-.env.$env_name.deploy}
COMPOSE_FILE=${COMPOSE_FILE:-deployments/docker-compose.yml}

[ -f "$ENV_FILE" ] || fail "env file not found: $ENV_FILE"
[ -f "$COMPOSE_FILE" ] || fail "compose file not found: $COMPOSE_FILE"

# Đọc giá trị env mà không source file (an toàn với space / ký tự đặc biệt).
env_value() {
  local key=$1
  local raw
  raw=$(awk -v key="$key" '
    { sub(/\r$/, "") }
    index($0, key "=") == 1 { v = substr($0, length(key) + 2) }
    END { print v }
  ' "$ENV_FILE")
  case $raw in
    \"*\") raw=${raw#\"}; raw=${raw%\"} ;;
    \'*\') raw=${raw#\'}; raw=${raw%\'} ;;
  esac
  printf '%s' "$raw"
}

POSTGRES_DB=$(env_value POSTGRES_DB)
POSTGRES_USER=$(env_value POSTGRES_USER)
POSTGRES_PASSWORD=$(env_value POSTGRES_PASSWORD)

[ -n "$POSTGRES_DB" ] || fail "POSTGRES_DB is required"
[ -n "$POSTGRES_USER" ] || fail "POSTGRES_USER is required"
[ -n "$POSTGRES_PASSWORD" ] || fail "POSTGRES_PASSWORD is required"

dc=(docker compose --env-file "$ENV_FILE")
if [ -f "$DEPLOY_FILE" ]; then
  dc+=(--env-file "$DEPLOY_FILE")
fi
dc+=(-f "$COMPOSE_FILE")

printf 'About to restore %s into %s (env=%s).\n' "$dump" "$POSTGRES_DB" "$env_name" >&2
confirm

printf '==> dropping and recreating schema\n'
"${dc[@]}" exec -T \
  -e PGPASSWORD="$POSTGRES_PASSWORD" \
  postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -v ON_ERROR_STOP=1 \
  -c 'DROP SCHEMA public CASCADE;' \
  -c 'CREATE SCHEMA public;'

printf '==> restoring dump\n'
gunzip -c "$dump" | "${dc[@]}" exec -T \
  -e PGPASSWORD="$POSTGRES_PASSWORD" \
  postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -v ON_ERROR_STOP=1

printf '==> restarting api\n'
"${dc[@]}" up -d --no-deps api

printf '==> restore complete\n'
