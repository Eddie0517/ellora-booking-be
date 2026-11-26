-- Randomize created_at (and email_verified_at/updated_at) for the 40 test customer accounts
-- Date range: 2026-10-12 to 2026-11-26 (+07:00)

UPDATE users
SET created_at = TIMESTAMPTZ '2026-11-04T09:02:34+07:00',
    email_verified_at = TIMESTAMPTZ '2026-11-04T09:10:34+07:00',
    updated_at = TIMESTAMPTZ '2026-11-04T09:10:34+07:00'
WHERE email = 'minhanh01@gmai.com'
  AND phone = '0901000001'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-13T20:54:44+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-13T21:04:44+07:00',
    updated_at = TIMESTAMPTZ '2026-10-13T21:04:44+07:00'
WHERE email = 'ngocha02@gmai.com'
  AND phone = '0901000002'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-11-13T21:59:18+07:00',
    email_verified_at = TIMESTAMPTZ '2026-11-13T22:01:18+07:00',
    updated_at = TIMESTAMPTZ '2026-11-13T22:01:18+07:00'
WHERE email = 'thanhtam03@gmai.com'
  AND phone = '0901000003'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-11-22T21:03:35+07:00',
    email_verified_at = TIMESTAMPTZ '2026-11-22T21:08:35+07:00',
    updated_at = TIMESTAMPTZ '2026-11-22T21:08:35+07:00'
WHERE email = 'giahan04@gmai.com'
  AND phone = '0901000004'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-28T02:22:09+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-28T02:33:09+07:00',
    updated_at = TIMESTAMPTZ '2026-10-28T02:33:09+07:00'
WHERE email = 'khanhlinh05@gmai.com'
  AND phone = '0901000005'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-23T06:03:06+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-23T06:14:06+07:00',
    updated_at = TIMESTAMPTZ '2026-10-23T06:14:06+07:00'
WHERE email = 'thaovy06@gmai.com'
  AND phone = '0901000006'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-11-11T17:54:51+07:00',
    email_verified_at = TIMESTAMPTZ '2026-11-11T17:59:51+07:00',
    updated_at = TIMESTAMPTZ '2026-11-11T17:59:51+07:00'
WHERE email = 'baotran07@gmai.com'
  AND phone = '0901000007'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-17T09:16:53+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-17T09:23:53+07:00',
    updated_at = TIMESTAMPTZ '2026-10-17T09:23:53+07:00'
WHERE email = 'thuyduong08@gmai.com'
  AND phone = '0901000008'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-29T02:02:24+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-29T02:06:24+07:00',
    updated_at = TIMESTAMPTZ '2026-10-29T02:06:24+07:00'
WHERE email = 'phuongnhi09@gmai.com'
  AND phone = '0901000009'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-11-07T17:38:13+07:00',
    email_verified_at = TIMESTAMPTZ '2026-11-07T17:51:13+07:00',
    updated_at = TIMESTAMPTZ '2026-11-07T17:51:13+07:00'
WHERE email = 'ngoclan10@gmai.com'
  AND phone = '0901000010'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-18T13:34:12+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-18T13:43:12+07:00',
    updated_at = TIMESTAMPTZ '2026-10-18T13:43:12+07:00'
WHERE email = 'tuongvi11@gmai.com'
  AND phone = '0901000011'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-11-13T00:37:35+07:00',
    email_verified_at = TIMESTAMPTZ '2026-11-13T00:47:35+07:00',
    updated_at = TIMESTAMPTZ '2026-11-13T00:47:35+07:00'
WHERE email = 'myduyen12@gmai.com'
  AND phone = '0901000012'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-11-22T18:25:44+07:00',
    email_verified_at = TIMESTAMPTZ '2026-11-22T18:32:44+07:00',
    updated_at = TIMESTAMPTZ '2026-11-22T18:32:44+07:00'
WHERE email = 'kimngan13@gmai.com'
  AND phone = '0901000013'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-31T10:45:25+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-31T10:57:25+07:00',
    updated_at = TIMESTAMPTZ '2026-10-31T10:57:25+07:00'
WHERE email = 'yennhi14@gmai.com'
  AND phone = '0901000014'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-30T05:12:28+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-30T05:18:28+07:00',
    updated_at = TIMESTAMPTZ '2026-10-30T05:18:28+07:00'
WHERE email = 'kieuoanh15@gmai.com'
  AND phone = '0901000015'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-22T06:01:37+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-22T06:09:37+07:00',
    updated_at = TIMESTAMPTZ '2026-10-22T06:09:37+07:00'
WHERE email = 'baongoc16@gmai.com'
  AND phone = '0901000016'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-11-21T19:07:30+07:00',
    email_verified_at = TIMESTAMPTZ '2026-11-21T19:09:30+07:00',
    updated_at = TIMESTAMPTZ '2026-11-21T19:09:30+07:00'
WHERE email = 'quynhanh17@gmai.com'
  AND phone = '0901000017'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-11-09T03:16:17+07:00',
    email_verified_at = TIMESTAMPTZ '2026-11-09T03:28:17+07:00',
    updated_at = TIMESTAMPTZ '2026-11-09T03:28:17+07:00'
WHERE email = 'diemmy18@gmai.com'
  AND phone = '0901000018'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-23T19:26:38+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-23T19:38:38+07:00',
    updated_at = TIMESTAMPTZ '2026-10-23T19:38:38+07:00'
WHERE email = 'thanhtruc19@gmai.com'
  AND phone = '0901000019'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-11-18T20:35:10+07:00',
    email_verified_at = TIMESTAMPTZ '2026-11-18T20:48:10+07:00',
    updated_at = TIMESTAMPTZ '2026-11-18T20:48:10+07:00'
WHERE email = 'camtu20@gmai.com'
  AND phone = '0901000020'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-17T11:04:54+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-17T11:10:54+07:00',
    updated_at = TIMESTAMPTZ '2026-10-17T11:10:54+07:00'
WHERE email = 'xuanmai21@gmai.com'
  AND phone = '0901000021'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-11-10T13:45:51+07:00',
    email_verified_at = TIMESTAMPTZ '2026-11-10T13:52:51+07:00',
    updated_at = TIMESTAMPTZ '2026-11-10T13:52:51+07:00'
WHERE email = 'thuylinh22@gmai.com'
  AND phone = '0901000022'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-24T18:25:33+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-24T18:34:33+07:00',
    updated_at = TIMESTAMPTZ '2026-10-24T18:34:33+07:00'
WHERE email = 'hongnhung23@gmai.com'
  AND phone = '0901000023'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-30T02:08:16+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-30T02:13:16+07:00',
    updated_at = TIMESTAMPTZ '2026-10-30T02:13:16+07:00'
WHERE email = 'phuongthao24@gmai.com'
  AND phone = '0901000024'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-14T02:27:33+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-14T02:34:33+07:00',
    updated_at = TIMESTAMPTZ '2026-10-14T02:34:33+07:00'
WHERE email = 'lanhuong25@gmai.com'
  AND phone = '0901000025'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-26T17:00:40+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-26T17:01:40+07:00',
    updated_at = TIMESTAMPTZ '2026-10-26T17:01:40+07:00'
WHERE email = 'thanhhuyen26@gmai.com'
  AND phone = '0901000026'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-20T22:17:16+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-20T22:25:16+07:00',
    updated_at = TIMESTAMPTZ '2026-10-20T22:25:16+07:00'
WHERE email = 'ngocdiep27@gmai.com'
  AND phone = '0901000027'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-28T18:57:45+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-28T19:00:45+07:00',
    updated_at = TIMESTAMPTZ '2026-10-28T19:00:45+07:00'
WHERE email = 'minhthu28@gmai.com'
  AND phone = '0901000028'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-18T18:48:41+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-18T18:51:41+07:00',
    updated_at = TIMESTAMPTZ '2026-10-18T18:51:41+07:00'
WHERE email = 'quocbao29@gmai.com'
  AND phone = '0901000029'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-22T14:23:39+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-22T14:33:39+07:00',
    updated_at = TIMESTAMPTZ '2026-10-22T14:33:39+07:00'
WHERE email = 'hoangphuc30@gmai.com'
  AND phone = '0901000030'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-28T22:13:51+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-28T22:26:51+07:00',
    updated_at = TIMESTAMPTZ '2026-10-28T22:26:51+07:00'
WHERE email = 'tuankiet31@gmai.com'
  AND phone = '0901000031'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-11-24T10:45:47+07:00',
    email_verified_at = TIMESTAMPTZ '2026-11-24T10:59:47+07:00',
    updated_at = TIMESTAMPTZ '2026-11-24T10:59:47+07:00'
WHERE email = 'nhatnam32@gmai.com'
  AND phone = '0901000032'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-11-19T04:49:18+07:00',
    email_verified_at = TIMESTAMPTZ '2026-11-19T04:57:18+07:00',
    updated_at = TIMESTAMPTZ '2026-11-19T04:57:18+07:00'
WHERE email = 'ducthinh33@gmai.com'
  AND phone = '0901000033'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-11-11T10:58:43+07:00',
    email_verified_at = TIMESTAMPTZ '2026-11-11T11:11:43+07:00',
    updated_at = TIMESTAMPTZ '2026-11-11T11:11:43+07:00'
WHERE email = 'giahuy34@gmai.com'
  AND phone = '0901000034'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-28T18:00:57+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-28T18:07:57+07:00',
    updated_at = TIMESTAMPTZ '2026-10-28T18:07:57+07:00'
WHERE email = 'quangvinh35@gmai.com'
  AND phone = '0901000035'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-11-10T17:10:48+07:00',
    email_verified_at = TIMESTAMPTZ '2026-11-10T17:23:48+07:00',
    updated_at = TIMESTAMPTZ '2026-11-10T17:23:48+07:00'
WHERE email = 'trunghieu36@gmai.com'
  AND phone = '0901000036'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-11-09T04:12:05+07:00',
    email_verified_at = TIMESTAMPTZ '2026-11-09T04:25:05+07:00',
    updated_at = TIMESTAMPTZ '2026-11-09T04:25:05+07:00'
WHERE email = 'haidang37@gmai.com'
  AND phone = '0901000037'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-11-03T07:26:15+07:00',
    email_verified_at = TIMESTAMPTZ '2026-11-03T07:33:15+07:00',
    updated_at = TIMESTAMPTZ '2026-11-03T07:33:15+07:00'
WHERE email = 'minhquan38@gmai.com'
  AND phone = '0901000038'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-10-16T20:23:29+07:00',
    email_verified_at = TIMESTAMPTZ '2026-10-16T20:37:29+07:00',
    updated_at = TIMESTAMPTZ '2026-10-16T20:37:29+07:00'
WHERE email = 'thanhdat39@gmai.com'
  AND phone = '0901000039'
  AND role = 'CUSTOMER';

UPDATE users
SET created_at = TIMESTAMPTZ '2026-11-02T13:39:29+07:00',
    email_verified_at = TIMESTAMPTZ '2026-11-02T13:51:29+07:00',
    updated_at = TIMESTAMPTZ '2026-11-02T13:51:29+07:00'
WHERE email = 'minhkhang40@gmai.com'
  AND phone = '0901000040'
  AND role = 'CUSTOMER';

