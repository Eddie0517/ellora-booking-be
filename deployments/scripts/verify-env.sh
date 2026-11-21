#!/usr/bin/env bash
# Kiểm tra file env thật có đủ key so với file example (hợp đồng key).
#
# usage: verify-env.sh <env-file> [example-file]
#
# Mặc định example-file: .env.production.example
# Không in giá trị secret, chỉ in TÊN key thiếu.
set -Eeuo pipefail

usage() {
  cat >&2 <<'EOF'
usage: verify-env.sh <env-file> [example-file]
EOF
  exit 64
}

fail() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

[ "$#" -ge 1 ] && [ "$#" -le 2 ] || usage

env_file=$1
example_file=${2:-.env.production.example}

[ -f "$env_file" ] || fail "env file not found: $env_file"
[ -f "$example_file" ] || fail "example file not found: $example_file"

# Key hợp lệ: dòng KEY=VALUE, bỏ comment và dòng trống.
keys() {
  sed -e 's/\r$//' "$1" \
    | grep -E '^[A-Za-z_][A-Za-z0-9_]*=' \
    | cut -d= -f1 \
    | sort -u
}

missing=$(comm -23 <(keys "$example_file") <(keys "$env_file") || true)
extra=$(comm -13 <(keys "$example_file") <(keys "$env_file") || true)

if [ -n "$missing" ]; then
  printf 'MISSING KEYS:\n%s\n' "$missing" >&2
fi
if [ -n "$extra" ]; then
  printf 'EXTRA KEYS (allowed, review them):\n%s\n' "$extra" >&2
fi

# Key bắt buộc phải có giá trị khác rỗng.
required=(
  SPRING_PROFILES_ACTIVE
  POSTGRES_DB
  POSTGRES_USER
  POSTGRES_PASSWORD
  JWT_SECRET
  CORS_ALLOWED_ORIGINS
  CLOUDFLARE_TUNNEL_TOKEN
  MAIL_USERNAME
  MAIL_PASSWORD
  CLOUDINARY_CLOUD_NAME
  CLOUDINARY_API_KEY
  CLOUDINARY_API_SECRET
  PAYOS_CLIENT_ID
  PAYOS_API_KEY
  PAYOS_CHECKSUM_KEY
)

# shellcheck disable=SC1090
set -a
. "$env_file"
set +a

empty=()
for key in "${required[@]}"; do
  value=$(printf '%s' "${!key:-}")
  if [ -z "$value" ]; then
    empty+=("$key")
  fi
done

if [ -n "$missing" ] || [ "${#empty[@]}" -gt 0 ]; then
  if [ "${#empty[@]}" -gt 0 ]; then
    printf 'EMPTY REQUIRED KEYS:\n' >&2
    printf '%s\n' "${empty[@]}" >&2
  fi
  fail "environment is incomplete: $env_file"
fi

printf 'OK: %s contains every required key with a value\n' "$env_file"
