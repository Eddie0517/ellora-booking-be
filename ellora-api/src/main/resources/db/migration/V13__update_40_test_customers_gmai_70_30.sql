-- Update 40 Customer test accounts to @gmai.com with 70% Female (28) / 30% Male (12) names
-- Preserves existing non-test accounts and prevents duplicate email conflicts.

UPDATE users
SET full_name = 'Nguyễn Minh Anh',
    email = 'minhanh01@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000001'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'minhanh01@gmai.com');

UPDATE users
SET full_name = 'Phạm Ngọc Hà',
    email = 'ngocha02@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000002'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'ngocha02@gmai.com');

UPDATE users
SET full_name = 'Võ Thanh Tâm',
    email = 'thanhtam03@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000003'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'thanhtam03@gmai.com');

UPDATE users
SET full_name = 'Đặng Gia Hân',
    email = 'giahan04@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000004'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'giahan04@gmai.com');

UPDATE users
SET full_name = 'Bùi Khánh Linh',
    email = 'khanhlinh05@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000005'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'khanhlinh05@gmai.com');

UPDATE users
SET full_name = 'Phan Thảo Vy',
    email = 'thaovy06@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000006'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'thaovy06@gmai.com');

UPDATE users
SET full_name = 'Huỳnh Bảo Trân',
    email = 'baotran07@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000007'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'baotran07@gmai.com');

UPDATE users
SET full_name = 'Dương Thùy Dương',
    email = 'thuyduong08@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000008'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'thuyduong08@gmai.com');

UPDATE users
SET full_name = 'Vũ Phương Nhi',
    email = 'phuongnhi09@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000009'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'phuongnhi09@gmai.com');

UPDATE users
SET full_name = 'Đinh Ngọc Lan',
    email = 'ngoclan10@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000010'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'ngoclan10@gmai.com');

UPDATE users
SET full_name = 'Hồ Tường Vi',
    email = 'tuongvi11@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000011'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'tuongvi11@gmai.com');

UPDATE users
SET full_name = 'Cao Mỹ Duyên',
    email = 'myduyen12@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000012'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'myduyen12@gmai.com');

UPDATE users
SET full_name = 'Lưu Kim Ngân',
    email = 'kimngan13@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000013'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'kimngan13@gmai.com');

UPDATE users
SET full_name = 'Vương Yến Nhi',
    email = 'yennhi14@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000014'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'yennhi14@gmai.com');

UPDATE users
SET full_name = 'Tô Kiều Oanh',
    email = 'kieuoanh15@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000015'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'kieuoanh15@gmai.com');

UPDATE users
SET full_name = 'Trần Bảo Ngọc',
    email = 'baongoc16@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000016'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'baongoc16@gmai.com');

UPDATE users
SET full_name = 'Phạm Quỳnh Anh',
    email = 'quynhanh17@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000017'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'quynhanh17@gmai.com');

UPDATE users
SET full_name = 'Đặng Diễm My',
    email = 'diemmy18@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000018'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'diemmy18@gmai.com');

UPDATE users
SET full_name = 'Hoàng Thanh Trúc',
    email = 'thanhtruc19@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000019'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'thanhtruc19@gmai.com');

UPDATE users
SET full_name = 'Đỗ Cẩm Tú',
    email = 'camtu20@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000020'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'camtu20@gmai.com');

UPDATE users
SET full_name = 'Ngô Xuân Mai',
    email = 'xuanmai21@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000021'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'xuanmai21@gmai.com');

UPDATE users
SET full_name = 'Nguyễn Thùy Linh',
    email = 'thuylinh22@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000022'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'thuylinh22@gmai.com');

UPDATE users
SET full_name = 'Lê Hồng Nhung',
    email = 'hongnhung23@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000023'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'hongnhung23@gmai.com');

UPDATE users
SET full_name = 'Trịnh Phương Thảo',
    email = 'phuongthao24@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000024'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'phuongthao24@gmai.com');

UPDATE users
SET full_name = 'Mai Lan Hương',
    email = 'lanhuong25@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000025'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'lanhuong25@gmai.com');

UPDATE users
SET full_name = 'Lý Thanh Huyền',
    email = 'thanhhuyen26@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000026'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'thanhhuyen26@gmai.com');

UPDATE users
SET full_name = 'Châu Ngọc Diệp',
    email = 'ngocdiep27@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000027'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'ngocdiep27@gmai.com');

UPDATE users
SET full_name = 'Hà Minh Thư',
    email = 'minhthu28@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000028'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'minhthu28@gmai.com');

UPDATE users
SET full_name = 'Trần Quốc Bảo',
    email = 'quocbao29@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000029'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'quocbao29@gmai.com');

UPDATE users
SET full_name = 'Lê Hoàng Phúc',
    email = 'hoangphuc30@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000030'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'hoangphuc30@gmai.com');

UPDATE users
SET full_name = 'Hoàng Tuấn Kiệt',
    email = 'tuankiet31@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000031'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'tuankiet31@gmai.com');

UPDATE users
SET full_name = 'Đỗ Nhật Nam',
    email = 'nhatnam32@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000032'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'nhatnam32@gmai.com');

UPDATE users
SET full_name = 'Ngô Đức Thịnh',
    email = 'ducthinh33@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000033'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'ducthinh33@gmai.com');

UPDATE users
SET full_name = 'Lý Gia Huy',
    email = 'giahuy34@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000034'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'giahuy34@gmai.com');

UPDATE users
SET full_name = 'Trịnh Quang Vinh',
    email = 'quangvinh35@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000035'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'quangvinh35@gmai.com');

UPDATE users
SET full_name = 'Mai Trung Hiếu',
    email = 'trunghieu36@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000036'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'trunghieu36@gmai.com');

UPDATE users
SET full_name = 'Lâm Hải Đăng',
    email = 'haidang37@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000037'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'haidang37@gmai.com');

UPDATE users
SET full_name = 'Châu Minh Quân',
    email = 'minhquan38@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000038'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'minhquan38@gmai.com');

UPDATE users
SET full_name = 'Nguyễn Thành Đạt',
    email = 'thanhdat39@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000039'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'thanhdat39@gmai.com');

UPDATE users
SET full_name = 'Dương Minh Khang',
    email = 'minhkhang40@gmai.com',
    updated_at = CURRENT_TIMESTAMP
WHERE phone = '0901000040'
  AND role = 'CUSTOMER'
  AND email LIKE '%@ellora.vn'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'minhkhang40@gmai.com');

