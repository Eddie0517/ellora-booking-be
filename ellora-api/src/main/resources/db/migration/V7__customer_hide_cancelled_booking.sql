ALTER TABLE bookings ADD COLUMN customer_hidden_at TIMESTAMPTZ;

CREATE INDEX idx_bookings_customer_visible_scheduled
    ON bookings (customer_id, scheduled_at DESC)
    WHERE customer_hidden_at IS NULL;
