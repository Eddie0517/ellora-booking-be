-- Fix email domain of the 40 test customer accounts from @gmai.com to @gmail.com
-- Preserves existing non-test accounts and prevents duplicate email conflicts.

UPDATE users
SET email = 'minhanh01@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'minhanh01@gmai.com'
  AND phone = '0901000001'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'minhanh01@gmail.com');

UPDATE users
SET email = 'ngocha02@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'ngocha02@gmai.com'
  AND phone = '0901000002'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'ngocha02@gmail.com');

UPDATE users
SET email = 'thanhtam03@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'thanhtam03@gmai.com'
  AND phone = '0901000003'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'thanhtam03@gmail.com');

UPDATE users
SET email = 'giahan04@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'giahan04@gmai.com'
  AND phone = '0901000004'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'giahan04@gmail.com');

UPDATE users
SET email = 'khanhlinh05@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'khanhlinh05@gmai.com'
  AND phone = '0901000005'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'khanhlinh05@gmail.com');

UPDATE users
SET email = 'thaovy06@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'thaovy06@gmai.com'
  AND phone = '0901000006'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'thaovy06@gmail.com');

UPDATE users
SET email = 'baotran07@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'baotran07@gmai.com'
  AND phone = '0901000007'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'baotran07@gmail.com');

UPDATE users
SET email = 'thuyduong08@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'thuyduong08@gmai.com'
  AND phone = '0901000008'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'thuyduong08@gmail.com');

UPDATE users
SET email = 'phuongnhi09@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'phuongnhi09@gmai.com'
  AND phone = '0901000009'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'phuongnhi09@gmail.com');

UPDATE users
SET email = 'ngoclan10@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'ngoclan10@gmai.com'
  AND phone = '0901000010'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'ngoclan10@gmail.com');

UPDATE users
SET email = 'tuongvi11@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'tuongvi11@gmai.com'
  AND phone = '0901000011'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'tuongvi11@gmail.com');

UPDATE users
SET email = 'myduyen12@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'myduyen12@gmai.com'
  AND phone = '0901000012'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'myduyen12@gmail.com');

UPDATE users
SET email = 'kimngan13@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'kimngan13@gmai.com'
  AND phone = '0901000013'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'kimngan13@gmail.com');

UPDATE users
SET email = 'yennhi14@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'yennhi14@gmai.com'
  AND phone = '0901000014'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'yennhi14@gmail.com');

UPDATE users
SET email = 'kieuoanh15@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'kieuoanh15@gmai.com'
  AND phone = '0901000015'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'kieuoanh15@gmail.com');

UPDATE users
SET email = 'baongoc16@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'baongoc16@gmai.com'
  AND phone = '0901000016'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'baongoc16@gmail.com');

UPDATE users
SET email = 'quynhanh17@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'quynhanh17@gmai.com'
  AND phone = '0901000017'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'quynhanh17@gmail.com');

UPDATE users
SET email = 'diemmy18@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'diemmy18@gmai.com'
  AND phone = '0901000018'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'diemmy18@gmail.com');

UPDATE users
SET email = 'thanhtruc19@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'thanhtruc19@gmai.com'
  AND phone = '0901000019'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'thanhtruc19@gmail.com');

UPDATE users
SET email = 'camtu20@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'camtu20@gmai.com'
  AND phone = '0901000020'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'camtu20@gmail.com');

UPDATE users
SET email = 'xuanmai21@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'xuanmai21@gmai.com'
  AND phone = '0901000021'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'xuanmai21@gmail.com');

UPDATE users
SET email = 'thuylinh22@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'thuylinh22@gmai.com'
  AND phone = '0901000022'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'thuylinh22@gmail.com');

UPDATE users
SET email = 'hongnhung23@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'hongnhung23@gmai.com'
  AND phone = '0901000023'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'hongnhung23@gmail.com');

UPDATE users
SET email = 'phuongthao24@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'phuongthao24@gmai.com'
  AND phone = '0901000024'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'phuongthao24@gmail.com');

UPDATE users
SET email = 'lanhuong25@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'lanhuong25@gmai.com'
  AND phone = '0901000025'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'lanhuong25@gmail.com');

UPDATE users
SET email = 'thanhhuyen26@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'thanhhuyen26@gmai.com'
  AND phone = '0901000026'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'thanhhuyen26@gmail.com');

UPDATE users
SET email = 'ngocdiep27@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'ngocdiep27@gmai.com'
  AND phone = '0901000027'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'ngocdiep27@gmail.com');

UPDATE users
SET email = 'minhthu28@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'minhthu28@gmai.com'
  AND phone = '0901000028'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'minhthu28@gmail.com');

UPDATE users
SET email = 'quocbao29@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'quocbao29@gmai.com'
  AND phone = '0901000029'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'quocbao29@gmail.com');

UPDATE users
SET email = 'hoangphuc30@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'hoangphuc30@gmai.com'
  AND phone = '0901000030'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'hoangphuc30@gmail.com');

UPDATE users
SET email = 'tuankiet31@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'tuankiet31@gmai.com'
  AND phone = '0901000031'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'tuankiet31@gmail.com');

UPDATE users
SET email = 'nhatnam32@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'nhatnam32@gmai.com'
  AND phone = '0901000032'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'nhatnam32@gmail.com');

UPDATE users
SET email = 'ducthinh33@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'ducthinh33@gmai.com'
  AND phone = '0901000033'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'ducthinh33@gmail.com');

UPDATE users
SET email = 'giahuy34@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'giahuy34@gmai.com'
  AND phone = '0901000034'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'giahuy34@gmail.com');

UPDATE users
SET email = 'quangvinh35@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'quangvinh35@gmai.com'
  AND phone = '0901000035'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'quangvinh35@gmail.com');

UPDATE users
SET email = 'trunghieu36@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'trunghieu36@gmai.com'
  AND phone = '0901000036'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'trunghieu36@gmail.com');

UPDATE users
SET email = 'haidang37@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'haidang37@gmai.com'
  AND phone = '0901000037'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'haidang37@gmail.com');

UPDATE users
SET email = 'minhquan38@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'minhquan38@gmai.com'
  AND phone = '0901000038'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'minhquan38@gmail.com');

UPDATE users
SET email = 'thanhdat39@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'thanhdat39@gmai.com'
  AND phone = '0901000039'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'thanhdat39@gmail.com');

UPDATE users
SET email = 'minhkhang40@gmail.com',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'minhkhang40@gmai.com'
  AND phone = '0901000040'
  AND role = 'CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM users u2 WHERE u2.email = 'minhkhang40@gmail.com');

