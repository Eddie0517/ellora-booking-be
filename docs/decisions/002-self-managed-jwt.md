# ADR 002: Self-Managed JWT Authentication with HS256, Rotating Refresh Tokens, and Fail-Open Redis

- **Status**: Accepted
- **Date**: 2026-11-06
- **Authors**: Eddie0517
- **Deciders**: Architecture Team

---

## Context and Problem Statement

Authentication and authorization in the Ellora salon booking system must authenticate multiple user roles (`ADMIN`, `OWNER`, `STAFF`, `CUSTOMER`), provide secure session lifetimes on mobile/web clients, support immediate token revocation when suspicious activity occurs or when a user logs out, and maintain high performance for high-frequency requests.

We evaluated whether to rely on third-party identity providers (e.g., AWS Cognito, Auth0, Firebase) or implement a self-managed cryptographic token system. Furthermore, for self-managed tokens, we needed to decide on the signing algorithm (asymmetric ES256 vs symmetric HS256), the refresh token persistence model, and the resilience strategy for distributed cache lookups (Redis).

---

## Decision Drivers

- **Zero Third-Party Vendor Lock-in & Cost**: Elimination of external auth service subscriptions and vendor runtime dependencies.
- **Low Latency & High Throughput**: JWT access token validation must run entirely in-process without outbound HTTP network hops on every request.
- **Secure Session Management & Revocation**: Short-lived access tokens combined with rotating refresh tokens stored in the database.
- **Operational Simplicity**: Avoid the operational overhead of asymmetric public/private key-pair management and certificate stores for a single monolithic backend.
- **High Availability & Fault Tolerance**: Temporary downtime or network partitions to Redis must not bring down core authentication and business APIs.

---

## Considered Options

1. **Option 1: External Identity Provider (AWS Cognito / Auth0)**
   - *Pros*: Out-of-the-box user management UI, built-in MFA.
   - *Cons*: Vendor lock-in, recurring operational cost, network latency on token verification, complex local Docker offline development.
2. **Option 2: Asymmetric Cryptographic Signing (ECDSA / ES256)**
   - *Pros*: Public key can be distributed to external verifiers without exposing signing capability.
   - *Cons*: For a single deployable monolith that both issues and verifies its own tokens, managing elliptic curve PEM key-pairs, JWKS endpoints, and `kid` headers adds unnecessary complexity.
3. **Option 3: Symmetric HS256 with Database Refresh Tokens and Fail-Open Redis** (Chosen)
   - *Pros*: Extremely fast HMAC-SHA256 verification, single secret string configuration via environment variable (`JWT_SECRET`), deterministic rotation and revocation in PostgreSQL, resilient fallbacks when Redis is degraded.
   - *Cons*: Secret must be kept strictly confidential and shared across any replicas.

---

## Decision Outcome

Chosen Option: **Option 3 — Symmetric HS256 with Database Refresh Tokens and Fail-Open Redis**.

### Key Technical Specifications:

1. **Token Signing Algorithm**:
   - HMAC-SHA256 (`HS256`) using a cryptographically secure shared secret (`JWT_SECRET`) of at least 256 bits (32 bytes), generated via `scripts/jwt-keys.ps1` or `scripts/jwt-keys.sh`.
   - Access tokens have a short lifespan (default: 15 minutes / 900 seconds).
   - Token payload contains claims: `sub` (User ID / UUID), `email`, `role`, `iss` (`ellora-booking-be`), `iat`, and `exp`.
2. **Rotating Refresh Tokens in Database**:
   - Long-lived refresh tokens (default: 7 days / 604,800 seconds) are persisted in the PostgreSQL `refresh_tokens` table.
   - On every refresh operation, the used refresh token is invalidated and a brand new refresh token + access token pair is issued (token rotation).
   - If a compromised or already-revoked refresh token is presented, all refresh tokens belonging to the user family are immediately revoked (reuse detection).
3. **Password Security**:
   - Passwords are encrypted using modern `Argon2id` (or BCrypt with cost factor 12) via `Argon2PasswordHasher`.
4. **Redis Fail-Open Strategy**:
   - Redis is utilized for access token blacklisting (e.g., immediate logout), rate limiting (via Bucket4j), and caching.
   - **Fail-Open Policy**: If Redis becomes temporarily unreachable or times out, the authentication filter and rate limiting logic catch `RedisConnectionException` / `QueryTimeoutException` and log a warning, allowing valid cryptographic JWT tokens to proceed rather than failing requests with HTTP 500 errors.

---

## Consequences

### Positive:
- **Zero Host & Cloud Dependencies**: Fully functional offline and locally inside Docker without external API keys or cloud accounts.
- **Sub-millisecond Token Validation**: In-memory HMAC validation without external network calls.
- **Robust Session Revocation**: Immediate user logout via blacklist and secure token rotation in PostgreSQL.
- **High Resilience**: Fail-open Redis guarantees that transient Redis restarts or flushes do not cause system-wide authentication outages.

### Negative / Trade-offs:
- **Secret Management Responsibility**: All backend replicas must securely receive the identical `JWT_SECRET` via environment variables.
- **Database Load on Refresh**: Refresh token rotations require database I/O, which is acceptable given the 15-minute access token window.
