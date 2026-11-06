-- =============================================================================
-- V1__baseline_schema.sql
-- Ellora Booking Monolith - Database Schema Baseline
-- Consolidates all 21 core relational tables, constraints, foreign keys, and indexes.
-- =============================================================================

-- Enable pgcrypto for gen_random_uuid() support
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- -----------------------------------------------------------------------------
-- 1. Identity & Access Management: users
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    avatar_url TEXT,
    role VARCHAR(30) NOT NULL,
    email_verified_at TIMESTAMP WITH TIME ZONE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- -----------------------------------------------------------------------------
-- 2. Token Revocation: revoked_access_tokens
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS revoked_access_tokens (
    token_hash VARCHAR(64) PRIMARY KEY,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_revoked_access_tokens_expires_at ON revoked_access_tokens(expires_at);

-- -----------------------------------------------------------------------------
-- 3. Salon Management: salons
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS salons (
    id BIGSERIAL PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address TEXT NOT NULL,
    city VARCHAR(100),
    district VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(255),
    logo_url TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL',
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    subscription_tier VARCHAR(16),
    subscription_starts_at TIMESTAMP WITH TIME ZONE,
    subscription_expires_at TIMESTAMP WITH TIME ZONE,
    average_rating NUMERIC(3,2) NOT NULL DEFAULT 0.00,
    total_reviews INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_salon_subscription_period CHECK (
        (subscription_tier IS NULL AND subscription_starts_at IS NULL AND subscription_expires_at IS NULL)
        OR (subscription_tier IS NOT NULL AND subscription_tier IN ('STANDARD','VIP')
            AND subscription_starts_at IS NOT NULL AND subscription_expires_at IS NOT NULL
            AND subscription_expires_at > subscription_starts_at)
    )
);

CREATE INDEX IF NOT EXISTS idx_salons_owner ON salons(owner_id);
CREATE INDEX IF NOT EXISTS idx_salons_status ON salons(status);

-- -----------------------------------------------------------------------------
-- 4. Service Categories: service_categories
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS service_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    icon_url TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- 5. Services: nail_services
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS nail_services (
    id BIGSERIAL PRIMARY KEY,
    salon_id BIGINT NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES service_categories(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10, 0) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    image_url TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_nail_services_salon ON nail_services(salon_id);
CREATE INDEX IF NOT EXISTS idx_nail_services_category ON nail_services(category_id);

-- -----------------------------------------------------------------------------
-- 6. Employees / Staff: employees
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS employees (
    id BIGSERIAL PRIMARY KEY,
    salon_id BIGINT NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    avatar_url TEXT,
    bio TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_employees_salon ON employees(salon_id);

-- -----------------------------------------------------------------------------
-- 7. Staff Working Schedules: working_schedules
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS working_schedules (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    day_of_week VARCHAR(16) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_working_schedules_employee ON working_schedules(employee_id);

-- -----------------------------------------------------------------------------
-- 8. Salon Working Hours: salon_working_hours
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS salon_working_hours (
    id BIGSERIAL PRIMARY KEY,
    salon_id BIGINT NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    day_of_week VARCHAR(16) NOT NULL,
    open_time TIME,
    close_time TIME,
    closed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_salon_working_hours_day UNIQUE (salon_id, day_of_week),
    CONSTRAINT ck_salon_working_hours_interval CHECK (
        closed OR (open_time IS NOT NULL AND close_time IS NOT NULL AND open_time < close_time)
    )
);

CREATE INDEX IF NOT EXISTS idx_salon_working_hours_salon ON salon_working_hours(salon_id);

-- -----------------------------------------------------------------------------
-- 9. Salon Closures: salon_closures
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS salon_closures (
    id BIGSERIAL PRIMARY KEY,
    salon_id BIGINT NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    closed_date DATE NOT NULL,
    reason VARCHAR(500),
    CONSTRAINT uq_salon_closure_date UNIQUE (salon_id, closed_date)
);

CREATE INDEX IF NOT EXISTS idx_salon_closures_salon ON salon_closures(salon_id);

-- -----------------------------------------------------------------------------
-- 10. Salon Profile Changes: salon_profile_changes
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS salon_profile_changes (
    id BIGSERIAL PRIMARY KEY,
    salon_id BIGINT NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    proposed JSONB NOT NULL,
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('PROFILE', 'COVER', 'GALLERY')),
    previous_logo_url TEXT,
    proposed_logo_url TEXT,
    previous_image_urls JSONB,
    proposed_image_urls JSONB,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    reviewed_by UUID REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_salon_profile_changes_status ON salon_profile_changes(status, id);
CREATE INDEX IF NOT EXISTS idx_salon_profile_changes_salon ON salon_profile_changes(salon_id, id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_salon_profile_changes_pending ON salon_profile_changes(salon_id) WHERE status = 'PENDING';

-- -----------------------------------------------------------------------------
-- 11. Salon Subscription Plans: salon_subscription_plans
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS salon_subscription_plans (
    tier VARCHAR(16) PRIMARY KEY CHECK (tier IN ('STANDARD','VIP')),
    monthly_price NUMERIC(15,0) CHECK (monthly_price >= 0),
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by UUID REFERENCES users(id),
    CONSTRAINT ck_salon_subscription_plan_price CHECK (NOT enabled OR monthly_price IS NOT NULL)
);

INSERT INTO salon_subscription_plans (tier, monthly_price, enabled)
VALUES ('STANDARD', NULL, FALSE), ('VIP', NULL, FALSE)
ON CONFLICT (tier) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 12. Salon Subscription Requests: salon_subscription_requests
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS salon_subscription_requests (
    id BIGSERIAL PRIMARY KEY,
    salon_id BIGINT NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    tier VARCHAR(16) NOT NULL CHECK (tier IN ('STANDARD','VIP')),
    quoted_price NUMERIC(15,0) NOT NULL CHECK (quoted_price >= 0),
    status VARCHAR(16) NOT NULL CHECK (status IN ('PENDING','APPROVED','REJECTED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    review_note VARCHAR(1000),
    reviewed_by UUID REFERENCES users(id),
    starts_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    payment_qr_url TEXT,
    payment_reported_at TIMESTAMP WITH TIME ZONE,
    payment_reference VARCHAR(100),
    payment_confirmed_at TIMESTAMP WITH TIME ZONE,
    payment_confirmed_by UUID REFERENCES users(id),
    received_amount NUMERIC(15,0),
    CONSTRAINT ck_salon_subscription_request_decision CHECK (
        (status='PENDING' AND reviewed_at IS NULL AND reviewed_by IS NULL AND starts_at IS NULL AND expires_at IS NULL)
        OR (status='APPROVED' AND reviewed_at IS NOT NULL AND reviewed_by IS NOT NULL
            AND starts_at IS NOT NULL AND expires_at IS NOT NULL AND expires_at > starts_at)
        OR (status='REJECTED' AND reviewed_at IS NOT NULL AND reviewed_by IS NOT NULL
            AND review_note IS NOT NULL AND length(trim(review_note)) > 0 AND starts_at IS NULL AND expires_at IS NULL)
    ),
    CONSTRAINT ck_subscription_payment_report CHECK (
        payment_reported_at IS NULL OR (quoted_price > 0 AND payment_qr_url IS NOT NULL AND length(trim(payment_qr_url)) > 0)
    ),
    CONSTRAINT ck_subscription_payment_confirmation CHECK (
        (payment_confirmed_at IS NULL AND payment_confirmed_by IS NULL AND received_amount IS NULL)
        OR (payment_confirmed_at IS NOT NULL AND payment_confirmed_by IS NOT NULL AND received_amount IS NOT NULL
            AND received_amount > 0 AND quoted_price > 0 AND received_amount = quoted_price
            AND payment_reported_at IS NOT NULL AND payment_qr_url IS NOT NULL AND length(trim(payment_qr_url)) > 0
            AND status='APPROVED')
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_salon_subscription_pending ON salon_subscription_requests(salon_id) WHERE status='PENDING';
CREATE INDEX IF NOT EXISTS idx_salon_subscription_requests_status ON salon_subscription_requests(status, id);
CREATE INDEX IF NOT EXISTS idx_salon_subscription_requests_salon ON salon_subscription_requests(salon_id, id);

-- -----------------------------------------------------------------------------
-- 13. Bookings: bookings
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    customer_id UUID REFERENCES users(id) ON DELETE SET NULL,
    walk_in_name VARCHAR(255),
    guest_email VARCHAR(255),
    walk_in_phone VARCHAR(20),
    salon_id BIGINT NOT NULL REFERENCES salons(id) ON DELETE RESTRICT,
    service_id BIGINT NOT NULL REFERENCES nail_services(id) ON DELETE RESTRICT,
    employee_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_minutes INTEGER NOT NULL,
    service_price_snapshot NUMERIC(15,0),
    completed_at TIMESTAMP WITH TIME ZONE,
    completion_confirmation VARCHAR(32),
    completion_version BIGINT NOT NULL DEFAULT 0,
    completion_responded_at TIMESTAMP WITH TIME ZONE,
    completion_customer_note VARCHAR(1000),
    completion_admin_note VARCHAR(2000),
    completion_resolved_at TIMESTAMP WITH TIME ZONE,
    revenue_estimated BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    customer_note TEXT,
    salon_note TEXT,
    cancellation_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bookings_customer ON bookings(customer_id);
CREATE INDEX IF NOT EXISTS idx_bookings_salon ON bookings(salon_id);
CREATE INDEX IF NOT EXISTS idx_bookings_scheduled_at ON bookings(scheduled_at);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX IF NOT EXISTS bookings_revenue_idx ON bookings(salon_id, completed_at) WHERE status = 'COMPLETED';

-- -----------------------------------------------------------------------------
-- 14. Booking Completion Disputes: booking_completion_disputes
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS booking_completion_disputes (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    completion_version BIGINT NOT NULL CHECK (completion_version >= 0),
    reason VARCHAR(1000) NOT NULL,
    status VARCHAR(16) NOT NULL CHECK (status IN ('OPEN', 'UPHELD', 'REOPENED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by UUID REFERENCES users(id),
    admin_note VARCHAR(2000),
    CONSTRAINT uq_booking_completion_dispute UNIQUE (booking_id, completion_version)
);

CREATE INDEX IF NOT EXISTS booking_completion_disputes_queue_idx ON booking_completion_disputes(status, created_at DESC, id DESC);

-- -----------------------------------------------------------------------------
-- 15. Booking Emails: booking_emails
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS booking_emails (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    kind VARCHAR(255) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    attempts INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_booking_emails_booking_kind UNIQUE (booking_id, kind)
);

CREATE INDEX IF NOT EXISTS booking_emails_due_idx ON booking_emails(next_attempt_at) WHERE sent_at IS NULL;

-- -----------------------------------------------------------------------------
-- 16. Reviews: reviews
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    salon_id BIGINT NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    booking_id BIGINT NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    salon_reply TEXT,
    salon_replied_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reviews_salon ON reviews(salon_id);
CREATE INDEX IF NOT EXISTS idx_reviews_customer ON reviews(customer_id);

-- -----------------------------------------------------------------------------
-- 17. Payments: payments
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE,
    amount NUMERIC(15,0) NOT NULL,
    deposit_amount NUMERIC(15,0),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    transaction_id VARCHAR(100),
    paid_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payments_booking ON payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);

-- -----------------------------------------------------------------------------
-- 18. Platform Billing Settings: platform_billing_settings
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS platform_billing_settings (
    id BIGINT PRIMARY KEY CHECK (id = 1),
    qr_url VARCHAR(255)
);

INSERT INTO platform_billing_settings (id, qr_url)
VALUES (1, NULL)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 19. Platform Fee Policies: platform_fee_policies
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS platform_fee_policies (
    effective_month DATE PRIMARY KEY,
    percent NUMERIC(5,2) NOT NULL CHECK (percent >= 0 AND percent <= 100)
);

-- -----------------------------------------------------------------------------
-- 20. Platform Statements: platform_statements
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS platform_statements (
    id BIGSERIAL PRIMARY KEY,
    salon_id BIGINT NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    billing_month DATE NOT NULL,
    paid_amount NUMERIC(15,0) NOT NULL DEFAULT 0,
    paid_at TIMESTAMP WITH TIME ZONE,
    confirmed_by UUID REFERENCES users(id),
    reminded_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_platform_statements_salon_month UNIQUE (salon_id, billing_month)
);

CREATE INDEX IF NOT EXISTS idx_platform_statements_salon ON platform_statements(salon_id);

-- -----------------------------------------------------------------------------
-- 21. Platform Support Contacts: platform_support_contacts
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS platform_support_contacts (
    id BIGINT PRIMARY KEY CHECK (id = 1),
    email VARCHAR(254) NOT NULL DEFAULT '',
    phone VARCHAR(30) NOT NULL DEFAULT '',
    facebook_url VARCHAR(500) NOT NULL DEFAULT ''
);

INSERT INTO platform_support_contacts (id, email, phone, facebook_url)
VALUES (1, '', '', '')
ON CONFLICT (id) DO NOTHING;
