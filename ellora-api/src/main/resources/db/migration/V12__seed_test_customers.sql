-- Seed 40 Customer test accounts (idempotent: does not overwrite existing emails)
-- Password for all test accounts: Ellora@2026 (BCrypt cost 12 via pgcrypto)

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('minhanh01@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Nguyễn Minh Anh', '0901000001', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('quocbao02@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Trần Quốc Bảo', '0901000002', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('hoangphuc03@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Lê Hoàng Phúc', '0901000003', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('ngocha04@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Phạm Ngọc Hà', '0901000004', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('thanhtam05@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Võ Thanh Tâm', '0901000005', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('giahan06@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Đặng Gia Hân', '0901000006', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('khanhlinh07@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Bùi Khánh Linh', '0901000007', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('tuankiet08@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Hoàng Tuấn Kiệt', '0901000008', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('thaovy09@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Phan Thảo Vy', '0901000009', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('nhatnam10@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Đỗ Nhật Nam', '0901000010', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('baotran11@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Huỳnh Bảo Trân', '0901000011', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('ducthinh12@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Ngô Đức Thịnh', '0901000012', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('thuyduong13@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Dương Thùy Dương', '0901000013', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('giahuy14@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Lý Gia Huy', '0901000014', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('phuongnhi15@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Vũ Phương Nhi', '0901000015', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('quangvinh16@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Trịnh Quang Vinh', '0901000016', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('ngoclan17@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Đinh Ngọc Lan', '0901000017', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('trunghieu18@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Mai Trung Hiếu', '0901000018', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('tuongvi19@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Hồ Tường Vi', '0901000019', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('haidang20@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Lâm Hải Đăng', '0901000020', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('myduyen21@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Cao Mỹ Duyên', '0901000021', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('congdanh22@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Tạ Công Danh', '0901000022', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('kimngan23@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Lưu Kim Ngân', '0901000023', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('minhquan24@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Châu Minh Quân', '0901000024', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('yennhi25@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Vương Yến Nhi', '0901000025', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('trongnghia26@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Hà Trọng Nghĩa', '0901000026', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('kieuoanh27@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Tô Kiều Oanh', '0901000027', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('thanhdat28@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Nguyễn Thành Đạt', '0901000028', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('baongoc29@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Trần Bảo Ngọc', '0901000029', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('huuphuoc30@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Lê Hữu Phước', '0901000030', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('quynhanh31@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Phạm Quỳnh Anh', '0901000031', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('anhkhoa32@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Võ Anh Khoa', '0901000032', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('diemmy33@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Đặng Diễm My', '0901000033', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('tanphat34@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Bùi Tấn Phát', '0901000034', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('thanhtruc35@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Hoàng Thanh Trúc', '0901000035', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('khaihoan36@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Phan Khải Hoàn', '0901000036', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('camtu37@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Đỗ Cẩm Tú', '0901000037', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('thienan38@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Huỳnh Thiên Ân', '0901000038', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('xuanmai39@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Ngô Xuân Mai', '0901000039', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, phone, role, email_verified_at, enabled, locked)
VALUES ('minhkhang40@ellora.vn', crypt('Ellora@2026', gen_salt('bf', 12)), 'Dương Minh Khang', '0901000040', 'CUSTOMER', CURRENT_TIMESTAMP, TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

