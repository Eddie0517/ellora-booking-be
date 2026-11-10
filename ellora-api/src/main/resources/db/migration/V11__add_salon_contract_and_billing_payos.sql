-- =============================================================================
-- Migration: V11__add_salon_contract_and_billing_payos.sql
-- Purpose: Add contract details and overdue flag to salons, and create
--          platform_statement_payments table for automated PayOS fee settlement.
-- =============================================================================

-- 1. Add contract and overdue columns to salons
ALTER TABLE salons
    ADD COLUMN IF NOT EXISTS contract_url TEXT,
    ADD COLUMN IF NOT EXISTS contract_file_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS contract_file_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS contract_uploaded_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS is_overdue BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_salons_is_overdue ON salons(is_overdue);

-- 2. Create platform_statement_payments for PayOS billing transactions
CREATE TABLE IF NOT EXISTS platform_statement_payments (
    id BIGSERIAL PRIMARY KEY,
    salon_id BIGINT NOT NULL REFERENCES salons(id) ON DELETE CASCADE,
    billing_month VARCHAR(7) NOT NULL, -- Format: YYYY-MM
    amount NUMERIC(15, 0) NOT NULL,
    payos_order_code BIGINT NOT NULL,
    payos_payment_link_id VARCHAR(100),
    payos_checkout_url TEXT,
    payos_qr_code TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, PAID, CANCELLED
    payment_reference VARCHAR(255),
    payment_reported_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_statement_payments_salon_month ON platform_statement_payments(salon_id, billing_month);
CREATE INDEX IF NOT EXISTS idx_statement_payments_order_code ON platform_statement_payments(payos_order_code);
