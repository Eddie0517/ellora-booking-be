-- V18: Confirm 12 selected future bookings (3 per salon for Salons 2, 3, 4, 5)
-- Matches Booking.confirm() domain transition (PENDING -> CONFIRMED).

UPDATE bookings
SET status = 'CONFIRMED',
    updated_at = CURRENT_TIMESTAMP
WHERE id IN (21, 22, 23, 27, 28, 29, 33, 34, 35, 39, 40, 41)
  AND status = 'PENDING';
