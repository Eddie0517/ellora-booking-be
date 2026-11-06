-- Seed the local Ellora administrator when the account does not already exist.
-- pgcrypto's bcrypt output is compatible with Spring's BCryptPasswordEncoder.
INSERT INTO users (email, password_hash, full_name, role, email_verified_at)
VALUES (
    'admin@ellora.vn',
    crypt('admin', gen_salt('bf', 12)),
    'Ellora Admin',
    'ADMIN',
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;
