# ==============================================================================
# Script: scripts/jwt-keys.ps1
# Description: Generates a cryptographically secure HMAC-SHA256 (HS256) secret
#              for JWT token signing (512-bit / 64-byte random Base64).
# Usage: powershell -ExecutionPolicy Bypass -File scripts/jwt-keys.ps1
# ==============================================================================

[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"

try {
    # Generate 64 cryptographically secure random bytes (512 bits)
    $buffer = New-Object byte[] 64
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $rng.GetBytes($buffer)

    # Convert to standard Base64 string
    $secret = [Convert]::ToBase64String($buffer)

    # Output directly to stdout without writing any files
    Write-Output "JWT_SECRET=$secret"
}
finally {
    if ($null -ne $rng) {
        $rng.Dispose()
    }
}
