# ==============================================================================
# Script: scripts/export-openapi.ps1
# Description: Automated OpenAPI specification export script.
#              Executes OpenApiExportTest to fetch live /v3/api-docs and write
#              the formatted schema directly to contracts/openapi.json.
# Usage: powershell -ExecutionPolicy Bypass -File scripts/export-openapi.ps1
# ==============================================================================

[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"

$RepoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $RepoRoot

Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host "Exporting OpenAPI Contract to contracts/openapi.json..." -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan

if (Get-Command mvn -ErrorAction SilentlyContinue) {
    Write-Host "Executing OpenApiExportTest via host Maven..." -ForegroundColor Green
    mvn test -Dtest=OpenApiExportTest "-Dsurefire.failIfNoSpecifiedTests=false" -pl ellora-api -am
} else {
    Write-Host "Executing OpenApiExportTest via containerized Maven (backend)..." -ForegroundColor Yellow
    docker compose run --rm -u root backend mvn test -Dtest=OpenApiExportTest "-Dsurefire.failIfNoSpecifiedTests=false" -pl ellora-api -am
}

if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to export OpenAPI specification! Exit code: $LASTEXITCODE"
    exit $LASTEXITCODE
}

Write-Host "OpenAPI specification successfully exported to contracts/openapi.json" -ForegroundColor Green
