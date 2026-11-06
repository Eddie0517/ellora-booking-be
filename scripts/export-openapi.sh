#!/usr/bin/env bash
# ==============================================================================
# Script: scripts/export-openapi.sh
# Description: Automated OpenAPI specification export script.
#              Executes OpenApiExportTest to fetch live /v3/api-docs and write
#              the formatted schema directly to contracts/openapi.json.
# Usage: bash scripts/export-openapi.sh
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${REPO_ROOT}"

echo "================================================================================"
echo "Exporting OpenAPI Contract to contracts/openapi.json..."
echo "================================================================================"

if command -v mvn >/dev/null 2>&1; then
    echo "Executing OpenApiExportTest via host Maven..."
    mvn test -Dtest=OpenApiExportTest -Dsurefire.failIfNoSpecifiedTests=false -pl ellora-api -am
else
    echo "Executing OpenApiExportTest via containerized Maven (backend)..."
    docker compose run --rm backend mvn test -Dtest=OpenApiExportTest -Dsurefire.failIfNoSpecifiedTests=false -pl ellora-api -am
fi

echo "OpenAPI specification successfully exported to contracts/openapi.json"
