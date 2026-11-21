#!/usr/bin/env bash
# Deploy/rollout image lên một môi trường, chạy từ VPS.
#
# usage:
#   deploy-release.sh <compose-file> <env-file> <deploy-env-file> \
#     <service=image-ref> [<service=image-ref> ...]
#
# Env tuỳ chỉnh:
#   APP_SERVICES       danh sách service hợp lệ, CSV (mặc định: các service truyền vào)
#   INFRA_SERVICES     service hạ tầng, CSV (mặc định: postgres)
#   INFRA_PROFILE      profile của hạ tầng (mặc định: rỗng)
#   DB_INIT_SERVICE    tên service bootstrap DB (mặc định: db-init; rỗng = bỏ qua)
#   DB_INIT_PROFILE    profile của db-init (mặc định: init)
#   MIGRATE_PREFIX     tiền tố service migration (mặc định: migrate-)
#   MIGRATE_PROFILE    profile của migration (mặc định: migrate)
#   MIGRATE_SERVICES   CSV service có migration (mặc định: tất cả service deploy)
#   PROXY_SERVICE      service proxy recreate cuối (mặc định: proxy; rỗng = bỏ qua)
#   WAIT_TIMEOUT       giây chờ health (mặc định: 240)
set -Eeuo pipefail

usage() {
  cat >&2 <<'EOF'
usage: deploy-release.sh <compose-file> <env-file> <deploy-env-file> <service=image-ref> [...]
EOF
  exit 64
}

log() { printf '==> %s\n' "$*"; }
fail() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

[ "$#" -ge 4 ] || usage

compose_file=$1
env_file=$2
deploy_file=$3
shift 3

[ -f "$compose_file" ] || fail "compose file not found: $compose_file"
[ -f "$env_file" ] || fail "env file not found: $env_file"

APP_SERVICES=${APP_SERVICES:-}
INFRA_SERVICES=${INFRA_SERVICES:-postgres}
INFRA_PROFILE=${INFRA_PROFILE:-}
DB_INIT_SERVICE=${DB_INIT_SERVICE:-db-init}
DB_INIT_PROFILE=${DB_INIT_PROFILE:-init}
MIGRATE_PREFIX=${MIGRATE_PREFIX:-migrate-}
MIGRATE_PROFILE=${MIGRATE_PROFILE:-migrate}
MIGRATE_SERVICES=${MIGRATE_SERVICES:-}
PROXY_SERVICE=${PROXY_SERVICE:-proxy}
WAIT_TIMEOUT=${WAIT_TIMEOUT:-240}

services=()
declare -A images=()
for pair in "$@"; do
  [ "$pair" != "${pair#*=}" ] || fail "expected service=image-ref, got: $pair"
  service=${pair%%=*}
  image=${pair#*=}
  [ -n "$service" ] || fail "empty service name in: $pair"
  [ -n "$image" ] || fail "missing image ref for service: $service"
  if [ -n "$APP_SERVICES" ]; then
    case ",${APP_SERVICES// /}," in
      *",${service},"*) ;;
      *) fail "unsupported service: $service" ;;
    esac
  fi
  services+=("$service")
  images["$service"]=$image
done
[ "${#services[@]}" -gt 0 ] || usage

# Một deploy tại một thời điểm cho mỗi deploy env file.
if command -v flock >/dev/null 2>&1; then
  exec 9>"${deploy_file}.lock"
  flock -n 9 || fail "another deploy is already running for ${deploy_file}"
else
  printf 'WARN: flock not available; skipping deploy lock\n' >&2
fi

backup="${deploy_file}.prev"
if [ -f "$deploy_file" ]; then
  cp -a "$deploy_file" "$backup"
else
  : > "$deploy_file"
fi
rm -f "${deploy_file}".bak.*
chmod 600 "$deploy_file" 2>/dev/null || true
chmod 600 "$backup" 2>/dev/null || true

tmp=$(mktemp "${deploy_file}.XXXXXX")
cleanup() { rm -f "$tmp" "${tmp}.new"; }
trap cleanup EXIT

# Merge image refs atomically: một writer, không read-modify-write race.
cp -a "$deploy_file" "$tmp"
for service in "${services[@]}"; do
  upper=$(printf '%s' "$service" | tr '[:lower:]' '[:upper:]')
  key="${upper}_IMAGE"
  value=${images[$service]}
  awk -v key="$key" -v value="$value" '
    BEGIN { done = 0 }
    index($0, key "=") == 1 { print key "=" value; done = 1; next }
    { print }
    END { if (!done) print key "=" value }
  ' "$tmp" > "${tmp}.new"
  mv "${tmp}.new" "$tmp"
done
mv "$tmp" "$deploy_file"
chmod 600 "$deploy_file"
trap - EXIT

dc=(docker compose --env-file "$env_file" --env-file "$deploy_file" -f "$compose_file")

app_state=0 # 0=old, 1=rolling out, 2=new active
restore_env() {
  if [ -f "$backup" ]; then
    cp -a "$backup" "$deploy_file"
    chmod 600 "$deploy_file"
  fi
}

rollback() {
  printf 'ROLLBACK: restoring previous image refs from %s\n' "$backup" >&2
  restore_env
  "${dc[@]}" up -d --wait --wait-timeout "$WAIT_TIMEOUT" --no-deps "${services[@]}" || true
  if [ -n "$PROXY_SERVICE" ]; then
    "${dc[@]}" up -d --force-recreate --no-deps "$PROXY_SERVICE" || true
  fi
}

on_error() {
  local code=$?
  printf 'deploy failed (exit %s) at line %s\n' "$code" "${1:-?}" >&2
  if [ "$app_state" -eq 1 ]; then
    rollback
  elif [ "$app_state" -eq 0 ]; then
    restore_env
  fi
  exit "$code"
}
trap 'on_error $LINENO' ERR

log "validating compose"
"${dc[@]}" config >/dev/null

log "pulling images: ${services[*]}"
"${dc[@]}" pull "${services[@]}"

if [ -n "$INFRA_SERVICES" ]; then
  read -ra infra <<< "$INFRA_SERVICES"
  log "ensuring infrastructure: ${infra[*]}"
  if [ -n "$INFRA_PROFILE" ]; then
    "${dc[@]}" --profile "$INFRA_PROFILE" up -d --wait --wait-timeout 180 "${infra[@]}"
  else
    "${dc[@]}" up -d --wait --wait-timeout 180 "${infra[@]}"
  fi
fi

if [ -n "$DB_INIT_SERVICE" ]; then
  log "running database bootstrap: $DB_INIT_SERVICE"
  "${dc[@]}" --profile "$DB_INIT_PROFILE" run --rm "$DB_INIT_SERVICE"
fi

log "running migrations: ${services[*]}"
migration_services=()
for service in "${services[@]}"; do
  if [ -n "$MIGRATE_SERVICES" ]; then
    case ",${MIGRATE_SERVICES// /}," in
      *",${service},"*) migration_services+=("$service") ;;
      *) ;;
    esac
  else
    migration_services+=("$service")
  fi
done

migration_failed=0
if [ "${#migration_services[@]}" -gt 0 ]; then
  declare -A pids=()
  for service in "${migration_services[@]}"; do
    migration_log="/tmp/deploy-migrate-${service}.log"
    "${dc[@]}" --profile "$MIGRATE_PROFILE" run --rm "${MIGRATE_PREFIX}${service}" \
      >"$migration_log" 2>&1 &
    pids["$service"]=$!
  done

  for service in "${migration_services[@]}"; do
    if ! wait "${pids[$service]}"; then
      printf 'migration failed: %s\n' "$service" >&2
      tail -n 80 "/tmp/deploy-migrate-${service}.log" >&2 || true
      migration_failed=1
    fi
  done
  [ "$migration_failed" -eq 0 ] || fail "aborting before application rollout"
else
  log "no migration services configured"
fi

log "rolling out services: ${services[*]}"
app_state=1
"${dc[@]}" up -d --wait --wait-timeout "$WAIT_TIMEOUT" --no-deps "${services[@]}"
app_state=2

if [ -n "$PROXY_SERVICE" ]; then
  log "recreating proxy: $PROXY_SERVICE"
  "${dc[@]}" up -d --force-recreate --no-deps "$PROXY_SERVICE"
fi

log "deployment status"
"${dc[@]}" ps

log "image refs"
for service in "${services[@]}"; do
  upper=$(printf '%s' "$service" | tr '[:lower:]' '[:upper:]')
  grep -E "^${upper}_IMAGE=" "$deploy_file" || true
done

log "deployment complete"
