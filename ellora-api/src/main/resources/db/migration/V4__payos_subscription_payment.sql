CREATE SEQUENCE payos_subscription_order_code START WITH 1000000000 MAXVALUE 2147483647 NO CYCLE;

ALTER TABLE salon_subscription_requests
    ADD COLUMN payos_order_code BIGINT UNIQUE,
    ADD COLUMN payos_payment_link_id TEXT,
    ADD COLUMN payos_checkout_url TEXT,
    ADD COLUMN payos_qr_code TEXT;

ALTER TABLE salon_subscription_requests DROP CONSTRAINT ck_subscription_payment_report;
ALTER TABLE salon_subscription_requests ADD CONSTRAINT ck_subscription_payment_report CHECK (
    payment_reported_at IS NULL OR (quoted_price > 0 AND ((payos_order_code IS NOT NULL AND payos_payment_link_id IS NOT NULL)
        OR (payment_qr_url IS NOT NULL AND length(trim(payment_qr_url)) > 0)))
);
ALTER TABLE salon_subscription_requests DROP CONSTRAINT ck_subscription_payment_confirmation;
ALTER TABLE salon_subscription_requests ADD CONSTRAINT ck_subscription_payment_confirmation CHECK (
    (payment_confirmed_at IS NULL AND payment_confirmed_by IS NULL)
    OR (payment_confirmed_at IS NOT NULL AND payment_confirmed_by IS NOT NULL AND received_amount = quoted_price
        AND payment_reported_at IS NOT NULL AND (payos_order_code IS NOT NULL OR payment_qr_url IS NOT NULL)
        AND status = 'APPROVED')
);
