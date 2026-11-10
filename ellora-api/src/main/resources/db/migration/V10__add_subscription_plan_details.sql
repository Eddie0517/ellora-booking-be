-- V10__add_subscription_plan_details.sql
-- Add description and features columns to salon_subscription_plans for customizable benefits

ALTER TABLE salon_subscription_plans
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS features TEXT;

-- Seed default descriptions and benefits
UPDATE salon_subscription_plans
SET description = 'Dành cho các cơ sở salon bắt đầu tiếp cận khách hàng trực tuyến với đầy đủ tính năng vận hành cơ bản.',
    features = 'Quản lý thông tin và hồ sơ salon trên hệ thống;Tiếp nhận và quản trị lịch hẹn khách hàng trực tuyến;Quản lý danh sách dịch vụ làm móng và giá niêm yết;Quản lý danh sách nhân sự và thợ làm móng;Hiển thị vị trí trên bản đồ tìm kiếm;Hỗ trợ kỹ thuật tiêu chuẩn từ đội ngũ Ellora'
WHERE tier = 'STANDARD' AND (features IS NULL OR features = '');

UPDATE salon_subscription_plans
SET description = 'Gói cao cấp giúp salon bứt phá doanh thu, ưu tiên hiển thị hàng đầu và tiếp cận tối đa lượng khách hàng tiềm năng.',
    features = 'Tất cả quyền lợi của gói Standard;Ghim nổi bật (Featured) trên trang khám phá và tìm kiếm;Huy hiệu VIP Nổi bật sang trọng trên bản đồ và hồ sơ;Ưu tiên hiển thị khi khách hàng tìm kiếm địa điểm gần nhất;Báo cáo phân tích doanh thu, hiệu suất thợ và lịch hẹn chi tiết;Tạo chương trình ưu đãi và mã giảm giá thu hút khách;Hỗ trợ chăm sóc khách hàng ưu tiên 24/7 từ chuyên viên Ellora'
WHERE tier = 'VIP' AND (features IS NULL OR features = '');
