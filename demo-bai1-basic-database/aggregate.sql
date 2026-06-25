-- =====================================================================
-- Bài 1: DISTINCT / Hàm tổng hợp / GROUP BY / HAVING / Subquery
-- Tương ứng syllabus mục 6.3 - 6.5
-- Chạy sau khi đã nạp schema.sql và seed.sql
-- =====================================================================

-- ---------- 6.3 DISTINCT ----------

-- Các mã tỉnh khác nhau (không lặp)
SELECT DISTINCT province_id FROM patients;

-- Các thành phố khác nhau, sắp xếp A->Z
SELECT DISTINCT city FROM patients ORDER BY city;

-- ---------- 6.4 Hàm tổng hợp ----------

-- Tổng số bệnh nhân
SELECT COUNT(*) AS tong_so FROM patients;

-- Chiều cao trung bình / cao nhất / thấp nhất
SELECT AVG(height) AS tb, MAX(height) AS cao_nhat, MIN(height) AS thap_nhat
FROM patients;

-- ---------- 6.4 GROUP BY ----------

-- Đếm số bệnh nhân theo từng tỉnh
SELECT province_id, COUNT(*) AS so_benh_nhan
FROM patients
GROUP BY province_id;

-- Chiều cao trung bình theo giới tính
SELECT gender, AVG(height) AS chieu_cao_tb
FROM patients
GROUP BY gender;

-- ---------- 6.4 HAVING (lọc sau khi gom nhóm) ----------

-- Chỉ lấy các tỉnh có nhiều hơn 1 bệnh nhân
SELECT province_id, COUNT(*) AS so_benh_nhan
FROM patients
GROUP BY province_id
HAVING COUNT(*) > 1;

-- ---------- 6.5 Subquery ----------

-- Bệnh nhân cao hơn chiều cao trung bình
SELECT first_name, last_name, height
FROM patients
WHERE height > (SELECT AVG(height) FROM patients);

-- Bệnh nhân sống ở tỉnh Ontario (subquery + IN)
SELECT first_name, last_name
FROM patients
WHERE province_id IN (
    SELECT province_id FROM province_names WHERE province_name = 'Ontario'
);
