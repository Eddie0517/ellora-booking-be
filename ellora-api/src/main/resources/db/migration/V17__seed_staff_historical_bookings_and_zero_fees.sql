-- V17: Set platform fee policies to 0%, add 3 staff per salon (Salons 2, 3, 4, 5),
-- and seed 16 historical bookings (2026-11-10 to 2026-11-25) for customers 25..40.

-- 1. Default platform fee = 0% and clear overdue flag on partner salons
UPDATE platform_fee_policies SET percent = 0;
INSERT INTO platform_fee_policies (effective_month, percent)
VALUES ('2026-10-01', 0), ('2026-11-01', 0), ('2026-12-01', 0)
ON CONFLICT (effective_month) DO UPDATE SET percent = 0;
UPDATE salons SET is_overdue = FALSE WHERE id IN (2, 3, 4, 5);

-- 2. Seed 3 staff members per salon (Salons 2, 3, 4, 5)
INSERT INTO employees (salon_id, full_name, bio, active)
SELECT 2, 'Nguyễn Thảo My', 'Chuyên viên Sơn Gel & Nail Art', TRUE
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE salon_id = 2 AND full_name = 'Nguyễn Thảo My');
INSERT INTO employees (salon_id, full_name, bio, active)
SELECT 2, 'Trần Ngọc Ánh', 'Chuyên viên Đắp Gel & Nối móng', TRUE
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE salon_id = 2 AND full_name = 'Trần Ngọc Ánh');
INSERT INTO employees (salon_id, full_name, bio, active)
SELECT 2, 'Lê Bảo Châu', 'Chuyên viên Chăm sóc móng & Cắt da', TRUE
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE salon_id = 2 AND full_name = 'Lê Bảo Châu');
INSERT INTO employees (salon_id, full_name, bio, active)
SELECT 3, 'Phạm Phương Linh', 'Chuyên viên Design Ombre & Mắt mèo', TRUE
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE salon_id = 3 AND full_name = 'Phạm Phương Linh');
INSERT INTO employees (salon_id, full_name, bio, active)
SELECT 3, 'Võ Khả Hân', 'Chuyên viên Đắp Gel & Dual Form', TRUE
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE salon_id = 3 AND full_name = 'Võ Khả Hân');
INSERT INTO employees (salon_id, full_name, bio, active)
SELECT 3, 'Đặng Minh Tâm', 'Chuyên viên Sơn Thạch & Chăm sóc móng', TRUE
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE salon_id = 3 AND full_name = 'Đặng Minh Tâm');
INSERT INTO employees (salon_id, full_name, bio, active)
SELECT 4, 'Bùi Thanh Nhàn', 'Chuyên viên Sơn Gel Hàn Quốc', TRUE
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE salon_id = 4 AND full_name = 'Bùi Thanh Nhàn');
INSERT INTO employees (salon_id, full_name, bio, active)
SELECT 4, 'Hoàng Ngọc Duyên', 'Chuyên viên Nail Design & Đắp Gel', TRUE
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE salon_id = 4 AND full_name = 'Hoàng Ngọc Duyên');
INSERT INTO employees (salon_id, full_name, bio, active)
SELECT 4, 'Phan Gia Nghi', 'Chuyên viên Chăm sóc móng & Spa chân', TRUE
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE salon_id = 4 AND full_name = 'Phan Gia Nghi');
INSERT INTO employees (salon_id, full_name, bio, active)
SELECT 5, 'Đỗ Tuyết Nhi', 'Chuyên viên Sơn Gel & Mắt mèo', TRUE
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE salon_id = 5 AND full_name = 'Đỗ Tuyết Nhi');
INSERT INTO employees (salon_id, full_name, bio, active)
SELECT 5, 'Huỳnh Khánh Vy', 'Chuyên viên Nối móng & Dual Form', TRUE
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE salon_id = 5 AND full_name = 'Huỳnh Khánh Vy');
INSERT INTO employees (salon_id, full_name, bio, active)
SELECT 5, 'Ngô Phương Uyên', 'Chuyên viên Tráng gương & Cứng móng', TRUE
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE salon_id = 5 AND full_name = 'Ngô Phương Uyên');

-- 3. Seed 16 historical bookings (Nhóm A: 2026-11-10 .. 2026-11-25)
INSERT INTO bookings (
    customer_id, salon_id, service_id, employee_id, scheduled_at,
    duration_minutes, service_price_snapshot, completed_at,
    completion_confirmation, completion_responded_at, revenue_estimated,
    status, customer_note, cancellation_reason, created_at, updated_at
)
SELECT
    u.id, 2, ns.id, e.id, TIMESTAMPTZ '2026-11-10T09:30:00+07:00',
    ns.duration_minutes, ns.price, (TIMESTAMPTZ '2026-11-10T09:30:00+07:00' + (ns.duration_minutes || ' minutes')::interval),
    'CONFIRMED', (TIMESTAMPTZ '2026-11-10T09:30:00+07:00' + (ns.duration_minutes || ' minutes')::interval + interval '1 hour'), FALSE,
    'COMPLETED', 'Làm màu gel tự nhiên nhẹ nhàng', NULL,
    TIMESTAMPTZ '2026-11-10T09:30:00+07:00' - interval '2 days', TIMESTAMPTZ '2026-11-10T09:30:00+07:00'
FROM users u
JOIN nail_services ns ON ns.id = 5 AND ns.salon_id = 2
JOIN employees e ON e.salon_id = 2 AND e.full_name = 'Nguyễn Thảo My'
WHERE u.email = 'lanhuong25@gmail.com'
  AND NOT EXISTS (
      SELECT 1 FROM bookings b
      WHERE b.customer_id = u.id AND b.salon_id = 2 AND b.scheduled_at = TIMESTAMPTZ '2026-11-10T09:30:00+07:00'
  );

INSERT INTO bookings (
    customer_id, salon_id, service_id, employee_id, scheduled_at,
    duration_minutes, service_price_snapshot, completed_at,
    completion_confirmation, completion_responded_at, revenue_estimated,
    status, customer_note, cancellation_reason, created_at, updated_at
)
SELECT
    u.id, 2, ns.id, e.id, TIMESTAMPTZ '2026-11-14T14:00:00+07:00',
    ns.duration_minutes, ns.price, (TIMESTAMPTZ '2026-11-14T14:00:00+07:00' + (ns.duration_minutes || ' minutes')::interval),
    'CONFIRMED', (TIMESTAMPTZ '2026-11-14T14:00:00+07:00' + (ns.duration_minutes || ' minutes')::interval + interval '1 hour'), FALSE,
    'COMPLETED', 'Úp móng Gel-X form ngắn', NULL,
    TIMESTAMPTZ '2026-11-14T14:00:00+07:00' - interval '2 days', TIMESTAMPTZ '2026-11-14T14:00:00+07:00'
FROM users u
JOIN nail_services ns ON ns.id = 10 AND ns.salon_id = 2
JOIN employees e ON e.salon_id = 2 AND e.full_name = 'Trần Ngọc Ánh'
WHERE u.email = 'thanhhuyen26@gmail.com'
  AND NOT EXISTS (
      SELECT 1 FROM bookings b
      WHERE b.customer_id = u.id AND b.salon_id = 2 AND b.scheduled_at = TIMESTAMPTZ '2026-11-14T14:00:00+07:00'
  );

INSERT INTO bookings (
    customer_id, salon_id, service_id, employee_id, scheduled_at,
    duration_minutes, service_price_snapshot, completed_at,
    completion_confirmation, completion_responded_at, revenue_estimated,
    status, customer_note, cancellation_reason, created_at, updated_at
)
SELECT
    u.id, 2, ns.id, e.id, TIMESTAMPTZ '2026-11-19T17:00:00+07:00',
    ns.duration_minutes, ns.price, (TIMESTAMPTZ '2026-11-19T17:00:00+07:00' + (ns.duration_minutes || ' minutes')::interval),
    'CONFIRMED', (TIMESTAMPTZ '2026-11-19T17:00:00+07:00' + (ns.duration_minutes || ' minutes')::interval + interval '1 hour'), FALSE,
    'COMPLETED', 'Vẽ hoạt hình ngón áp út', NULL,
    TIMESTAMPTZ '2026-11-19T17:00:00+07:00' - interval '2 days', TIMESTAMPTZ '2026-11-19T17:00:00+07:00'
FROM users u
JOIN nail_services ns ON ns.id = 15 AND ns.salon_id = 2
JOIN employees e ON e.salon_id = 2 AND e.full_name = 'Lê Bảo Châu'
WHERE u.email = 'ngocdiep27@gmail.com'
  AND NOT EXISTS (
      SELECT 1 FROM bookings b
      WHERE b.customer_id = u.id AND b.salon_id = 2 AND b.scheduled_at = TIMESTAMPTZ '2026-11-19T17:00:00+07:00'
  );

INSERT INTO bookings (
    customer_id, salon_id, service_id, employee_id, scheduled_at,
    duration_minutes, service_price_snapshot, completed_at,
    completion_confirmation, completion_responded_at, revenue_estimated,
    status, customer_note, cancellation_reason, created_at, updated_at
)
SELECT
    u.id, 2, ns.id, e.id, TIMESTAMPTZ '2026-11-23T11:00:00+07:00',
    ns.duration_minutes, ns.price, NULL,
    NULL, NULL, FALSE,
    'CANCELLED', 'Sơn mắt mèo', 'Khách hàng bận việc đột xuất',
    TIMESTAMPTZ '2026-11-23T11:00:00+07:00' - interval '2 days', TIMESTAMPTZ '2026-11-23T11:00:00+07:00'
FROM users u
JOIN nail_services ns ON ns.id = 8 AND ns.salon_id = 2
JOIN employees e ON e.salon_id = 2 AND e.full_name = 'Nguyễn Thảo My'
WHERE u.email = 'minhthu28@gmail.com'
  AND NOT EXISTS (
      SELECT 1 FROM bookings b
      WHERE b.customer_id = u.id AND b.salon_id = 2 AND b.scheduled_at = TIMESTAMPTZ '2026-11-23T11:00:00+07:00'
  );

INSERT INTO bookings (
    customer_id, salon_id, service_id, employee_id, scheduled_at,
    duration_minutes, service_price_snapshot, completed_at,
    completion_confirmation, completion_responded_at, revenue_estimated,
    status, customer_note, cancellation_reason, created_at, updated_at
)
SELECT
    u.id, 3, ns.id, e.id, TIMESTAMPTZ '2026-11-11T10:00:00+07:00',
    ns.duration_minutes, ns.price, (TIMESTAMPTZ '2026-11-11T10:00:00+07:00' + (ns.duration_minutes || ' minutes')::interval),
    'CONFIRMED', (TIMESTAMPTZ '2026-11-11T10:00:00+07:00' + (ns.duration_minutes || ' minutes')::interval + interval '1 hour'), FALSE,
    'COMPLETED', 'Cắt da dũa form sạch sẽ', NULL,
    TIMESTAMPTZ '2026-11-11T10:00:00+07:00' - interval '2 days', TIMESTAMPTZ '2026-11-11T10:00:00+07:00'
FROM users u
JOIN nail_services ns ON ns.id = 20 AND ns.salon_id = 3
JOIN employees e ON e.salon_id = 3 AND e.full_name = 'Đặng Minh Tâm'
WHERE u.email = 'quocbao29@gmail.com'
  AND NOT EXISTS (
      SELECT 1 FROM bookings b
      WHERE b.customer_id = u.id AND b.salon_id = 3 AND b.scheduled_at = TIMESTAMPTZ '2026-11-11T10:00:00+07:00'
  );

INSERT INTO bookings (
    customer_id, salon_id, service_id, employee_id, scheduled_at,
    duration_minutes, service_price_snapshot, completed_at,
    completion_confirmation, completion_responded_at, revenue_estimated,
    status, customer_note, cancellation_reason, created_at, updated_at
)
SELECT
    u.id, 3, ns.id, e.id, TIMESTAMPTZ '2026-11-15T13:30:00+07:00',
    ns.duration_minutes, ns.price, (TIMESTAMPTZ '2026-11-15T13:30:00+07:00' + (ns.duration_minutes || ' minutes')::interval),
    'CONFIRMED', (TIMESTAMPTZ '2026-11-15T13:30:00+07:00' + (ns.duration_minutes || ' minutes')::interval + interval '1 hour'), FALSE,
    'COMPLETED', 'Combo cắt da sơn gel', NULL,
    TIMESTAMPTZ '2026-11-15T13:30:00+07:00' - interval '2 days', TIMESTAMPTZ '2026-11-15T13:30:00+07:00'
FROM users u
JOIN nail_services ns ON ns.id = 16 AND ns.salon_id = 3
JOIN employees e ON e.salon_id = 3 AND e.full_name = 'Phạm Phương Linh'
WHERE u.email = 'hoangphuc30@gmail.com'
  AND NOT EXISTS (
      SELECT 1 FROM bookings b
      WHERE b.customer_id = u.id AND b.salon_id = 3 AND b.scheduled_at = TIMESTAMPTZ '2026-11-15T13:30:00+07:00'
  );

INSERT INTO bookings (
    customer_id, salon_id, service_id, employee_id, scheduled_at,
    duration_minutes, service_price_snapshot, completed_at,
    completion_confirmation, completion_responded_at, revenue_estimated,
    status, customer_note, cancellation_reason, created_at, updated_at
)
SELECT
    u.id, 3, ns.id, e.id, TIMESTAMPTZ '2026-11-20T16:00:00+07:00',
    ns.duration_minutes, ns.price, (TIMESTAMPTZ '2026-11-20T16:00:00+07:00' + (ns.duration_minutes || ' minutes')::interval),
    'CONFIRMED', (TIMESTAMPTZ '2026-11-20T16:00:00+07:00' + (ns.duration_minutes || ' minutes')::interval + interval '1 hour'), FALSE,
    'COMPLETED', 'Đắp gel tự nhiên', NULL,
    TIMESTAMPTZ '2026-11-20T16:00:00+07:00' - interval '2 days', TIMESTAMPTZ '2026-11-20T16:00:00+07:00'
FROM users u
JOIN nail_services ns ON ns.id = 27 AND ns.salon_id = 3
JOIN employees e ON e.salon_id = 3 AND e.full_name = 'Võ Khả Hân'
WHERE u.email = 'tuankiet31@gmail.com'
  AND NOT EXISTS (
      SELECT 1 FROM bookings b
      WHERE b.customer_id = u.id AND b.salon_id = 3 AND b.scheduled_at = TIMESTAMPTZ '2026-11-20T16:00:00+07:00'
  );

INSERT INTO bookings (
    customer_id, salon_id, service_id, employee_id, scheduled_at,
    duration_minutes, service_price_snapshot, completed_at,
    completion_confirmation, completion_responded_at, revenue_estimated,
    status, customer_note, cancellation_reason, created_at, updated_at
)
SELECT
    u.id, 3, ns.id, e.id, TIMESTAMPTZ '2026-11-24T15:00:00+07:00',
    ns.duration_minutes, ns.price, NULL,
    NULL, NULL, FALSE,
    'CANCELLED', 'Design Ombre', 'Khách hàng đổi lịch sang tuần sau',
    TIMESTAMPTZ '2026-11-24T15:00:00+07:00' - interval '2 days', TIMESTAMPTZ '2026-11-24T15:00:00+07:00'
FROM users u
JOIN nail_services ns ON ns.id = 29 AND ns.salon_id = 3
JOIN employees e ON e.salon_id = 3 AND e.full_name = 'Phạm Phương Linh'
WHERE u.email = 'nhatnam32@gmail.com'
  AND NOT EXISTS (
      SELECT 1 FROM bookings b
      WHERE b.customer_id = u.id AND b.salon_id = 3 AND b.scheduled_at = TIMESTAMPTZ '2026-11-24T15:00:00+07:00'
  );

INSERT INTO bookings (
    customer_id, salon_id, service_id, employee_id, scheduled_at,
    duration_minutes, service_price_snapshot, completed_at,
    completion_confirmation, completion_responded_at, revenue_estimated,
    status, customer_note, cancellation_reason, created_at, updated_at
)
SELECT
    u.id, 4, ns.id, e.id, TIMESTAMPTZ '2026-11-12T10:30:00+07:00',
    ns.duration_minutes, ns.price, (TIMESTAMPTZ '2026-11-12T10:30:00+07:00' + (ns.duration_minutes || ' minutes')::interval),
    'CONFIRMED', (TIMESTAMPTZ '2026-11-12T10:30:00+07:00' + (ns.duration_minutes || ' minutes')::interval + interval '1 hour'), FALSE,
    'COMPLETED', 'Ngâm chân thảo dược thư giãn', NULL,
    TIMESTAMPTZ '2026-11-12T10:30:00+07:00' - interval '2 days', TIMESTAMPTZ '2026-11-12T10:30:00+07:00'
FROM users u
JOIN nail_services ns ON ns.id = 43 AND ns.salon_id = 4
JOIN employees e ON e.salon_id = 4 AND e.full_name = 'Phan Gia Nghi'
WHERE u.email = 'ducthinh33@gmail.com'
  AND NOT EXISTS (
      SELECT 1 FROM bookings b
      WHERE b.customer_id = u.id AND b.salon_id = 4 AND b.scheduled_at = TIMESTAMPTZ '2026-11-12T10:30:00+07:00'
  );

INSERT INTO bookings (
    customer_id, salon_id, service_id, employee_id, scheduled_at,
    duration_minutes, service_price_snapshot, completed_at,
    completion_confirmation, completion_responded_at, revenue_estimated,
    status, customer_note, cancellation_reason, created_at, updated_at
)
SELECT
    u.id, 4, ns.id, e.id, TIMESTAMPTZ '2026-11-16T14:30:00+07:00',
    ns.duration_minutes, ns.price, (TIMESTAMPTZ '2026-11-16T14:30:00+07:00' + (ns.duration_minutes || ' minutes')::interval),
    'CONFIRMED', (TIMESTAMPTZ '2026-11-16T14:30:00+07:00' + (ns.duration_minutes || ' minutes')::interval + interval '1 hour'), FALSE,
    'COMPLETED', 'Sơn gel bóng bền màu', NULL,
    TIMESTAMPTZ '2026-11-16T14:30:00+07:00' - interval '2 days', TIMESTAMPTZ '2026-11-16T14:30:00+07:00'
FROM users u
JOIN nail_services ns ON ns.id = 33 AND ns.salon_id = 4
JOIN employees e ON e.salon_id = 4 AND e.full_name = 'Bùi Thanh Nhàn'
WHERE u.email = 'giahuy34@gmail.com'
  AND NOT EXISTS (
      SELECT 1 FROM bookings b
      WHERE b.customer_id = u.id AND b.salon_id = 4 AND b.scheduled_at = TIMESTAMPTZ '2026-11-16T14:30:00+07:00'
  );

INSERT INTO bookings (
    customer_id, salon_id, service_id, employee_id, scheduled_at,
    duration_minutes, service_price_snapshot, completed_at,
    completion_confirmation, completion_responded_at, revenue_estimated,
    status, customer_note, cancellation_reason, created_at, updated_at
)
SELECT
    u.id, 4, ns.id, e.id, TIMESTAMPTZ '2026-11-21T17:30:00+07:00',
    ns.duration_minutes, ns.price, (TIMESTAMPTZ '2026-11-21T17:30:00+07:00' + (ns.duration_minutes || ' minutes')::interval),
    'CONFIRMED', (TIMESTAMPTZ '2026-11-21T17:30:00+07:00' + (ns.duration_minutes || ' minutes')::interval + interval '1 hour'), FALSE,
    'COMPLETED', 'Đắp gel IBD', NULL,
    TIMESTAMPTZ '2026-11-21T17:30:00+07:00' - interval '2 days', TIMESTAMPTZ '2026-11-21T17:30:00+07:00'
FROM users u
JOIN nail_services ns ON ns.id = 38 AND ns.salon_id = 4
JOIN employees e ON e.salon_id = 4 AND e.full_name = 'Hoàng Ngọc Duyên'
WHERE u.email = 'quangvinh35@gmail.com'
  AND NOT EXISTS (
      SELECT 1 FROM bookings b
      WHERE b.customer_id = u.id AND b.salon_id = 4 AND b.scheduled_at = TIMESTAMPTZ '2026-11-21T17:30:00+07:00'
  );

INSERT INTO bookings (
    customer_id, salon_id, service_id, employee_id, scheduled_at,
    duration_minutes, service_price_snapshot, completed_at,
    completion_confirmation, completion_responded_at, revenue_estimated,
    status, customer_note, cancellation_reason, created_at, updated_at
)
SELECT
    u.id, 4, ns.id, e.id, TIMESTAMPTZ '2026-11-25T11:30:00+07:00',
    ns.duration_minutes, ns.price, NULL,
    NULL, NULL, FALSE,
    'CANCELLED', 'Nail Design loang', 'Khách hàng có việc cá nhân',
    TIMESTAMPTZ '2026-11-25T11:30:00+07:00' - interval '2 days', TIMESTAMPTZ '2026-11-25T11:30:00+07:00'
FROM users u
JOIN nail_services ns ON ns.id = 41 AND ns.salon_id = 4
JOIN employees e ON e.salon_id = 4 AND e.full_name = 'Hoàng Ngọc Duyên'
WHERE u.email = 'trunghieu36@gmail.com'
  AND NOT EXISTS (
      SELECT 1 FROM bookings b
      WHERE b.customer_id = u.id AND b.salon_id = 4 AND b.scheduled_at = TIMESTAMPTZ '2026-11-25T11:30:00+07:00'
  );

INSERT INTO bookings (
    customer_id, salon_id, service_id, employee_id, scheduled_at,
    duration_minutes, service_price_snapshot, completed_at,
    completion_confirmation, completion_responded_at, revenue_estimated,
    status, customer_note, cancellation_reason, created_at, updated_at
)
SELECT
    u.id, 5, ns.id, e.id, TIMESTAMPTZ '2026-11-13T09:30:00+07:00',
    ns.duration_minutes, ns.price, (TIMESTAMPTZ '2026-11-13T09:30:00+07:00' + (ns.duration_minutes || ' minutes')::interval),
    'CONFIRMED', (TIMESTAMPTZ '2026-11-13T09:30:00+07:00' + (ns.duration_minutes || ' minutes')::interval + interval '1 hour'), FALSE,
    'COMPLETED', 'Nhặt da sơn thạch', NULL,
    TIMESTAMPTZ '2026-11-13T09:30:00+07:00' - interval '2 days', TIMESTAMPTZ '2026-11-13T09:30:00+07:00'
FROM users u
JOIN nail_services ns ON ns.id = 44 AND ns.salon_id = 5
JOIN employees e ON e.salon_id = 5 AND e.full_name = 'Đỗ Tuyết Nhi'
WHERE u.email = 'haidang37@gmail.com'
  AND NOT EXISTS (
      SELECT 1 FROM bookings b
      WHERE b.customer_id = u.id AND b.salon_id = 5 AND b.scheduled_at = TIMESTAMPTZ '2026-11-13T09:30:00+07:00'
  );

INSERT INTO bookings (
    customer_id, salon_id, service_id, employee_id, scheduled_at,
    duration_minutes, service_price_snapshot, completed_at,
    completion_confirmation, completion_responded_at, revenue_estimated,
    status, customer_note, cancellation_reason, created_at, updated_at
)
SELECT
    u.id, 5, ns.id, e.id, TIMESTAMPTZ '2026-11-17T15:30:00+07:00',
    ns.duration_minutes, ns.price, (TIMESTAMPTZ '2026-11-17T15:30:00+07:00' + (ns.duration_minutes || ' minutes')::interval),
    'CONFIRMED', (TIMESTAMPTZ '2026-11-17T15:30:00+07:00' + (ns.duration_minutes || ' minutes')::interval + interval '1 hour'), FALSE,
    'COMPLETED', 'Dual form sơn gel', NULL,
    TIMESTAMPTZ '2026-11-17T15:30:00+07:00' - interval '2 days', TIMESTAMPTZ '2026-11-17T15:30:00+07:00'
FROM users u
JOIN nail_services ns ON ns.id = 46 AND ns.salon_id = 5
JOIN employees e ON e.salon_id = 5 AND e.full_name = 'Huỳnh Khánh Vy'
WHERE u.email = 'minhquan38@gmail.com'
  AND NOT EXISTS (
      SELECT 1 FROM bookings b
      WHERE b.customer_id = u.id AND b.salon_id = 5 AND b.scheduled_at = TIMESTAMPTZ '2026-11-17T15:30:00+07:00'
  );

INSERT INTO bookings (
    customer_id, salon_id, service_id, employee_id, scheduled_at,
    duration_minutes, service_price_snapshot, completed_at,
    completion_confirmation, completion_responded_at, revenue_estimated,
    status, customer_note, cancellation_reason, created_at, updated_at
)
SELECT
    u.id, 5, ns.id, e.id, TIMESTAMPTZ '2026-11-22T18:30:00+07:00',
    ns.duration_minutes, ns.price, (TIMESTAMPTZ '2026-11-22T18:30:00+07:00' + (ns.duration_minutes || ' minutes')::interval),
    'CONFIRMED', (TIMESTAMPTZ '2026-11-22T18:30:00+07:00' + (ns.duration_minutes || ' minutes')::interval + interval '1 hour'), FALSE,
    'COMPLETED', 'Tráng gương thời thượng', NULL,
    TIMESTAMPTZ '2026-11-22T18:30:00+07:00' - interval '2 days', TIMESTAMPTZ '2026-11-22T18:30:00+07:00'
FROM users u
JOIN nail_services ns ON ns.id = 48 AND ns.salon_id = 5
JOIN employees e ON e.salon_id = 5 AND e.full_name = 'Ngô Phương Uyên'
WHERE u.email = 'thanhdat39@gmail.com'
  AND NOT EXISTS (
      SELECT 1 FROM bookings b
      WHERE b.customer_id = u.id AND b.salon_id = 5 AND b.scheduled_at = TIMESTAMPTZ '2026-11-22T18:30:00+07:00'
  );

INSERT INTO bookings (
    customer_id, salon_id, service_id, employee_id, scheduled_at,
    duration_minutes, service_price_snapshot, completed_at,
    completion_confirmation, completion_responded_at, revenue_estimated,
    status, customer_note, cancellation_reason, created_at, updated_at
)
SELECT
    u.id, 5, ns.id, e.id, TIMESTAMPTZ '2026-11-25T19:00:00+07:00',
    ns.duration_minutes, ns.price, NULL,
    NULL, NULL, FALSE,
    'CANCELLED', 'Úp móng sơn gel', 'Khách hàng thay đổi thời gian hẹn',
    TIMESTAMPTZ '2026-11-25T19:00:00+07:00' - interval '2 days', TIMESTAMPTZ '2026-11-25T19:00:00+07:00'
FROM users u
JOIN nail_services ns ON ns.id = 45 AND ns.salon_id = 5
JOIN employees e ON e.salon_id = 5 AND e.full_name = 'Đỗ Tuyết Nhi'
WHERE u.email = 'minhkhang40@gmail.com'
  AND NOT EXISTS (
      SELECT 1 FROM bookings b
      WHERE b.customer_id = u.id AND b.salon_id = 5 AND b.scheduled_at = TIMESTAMPTZ '2026-11-25T19:00:00+07:00'
  );

