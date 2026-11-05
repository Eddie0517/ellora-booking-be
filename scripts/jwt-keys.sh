#!/usr/bin/env bash
# ==============================================================================
# Script: scripts/jwt-keys.sh
# Description: Generates a cryptographically secure HMAC-SHA256 (HS256) secret
#              for JWT token signing (512-bit / 64-byte random Base64).
# Usage: bash scripts/jwt-keys.sh
# ==============================================================================

set -euo pipefail

if command -v openssl >/dev/null 2>&1; then
    SECRET=$(openssl rand 64 | openssl base64 -A)
else
    SECRET=$(head -c 64 /dev/urandom | base64 | tr -d '\r\n ')
fi

echo "JWT_SECRET=${SECRET}"
