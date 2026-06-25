-- =====================================================================
-- Bài 1: ĐÁP ÁN phần thực hành (syllabus mục 6.10)
-- Chạy sau khi đã nạp schema.sql và seed.sql
-- =====================================================================

-- 1. Tất cả các dòng có gender là 'M'
SELECT * FROM patients WHERE gender = 'M';

-- 2. Tất cả các dòng có height lớn hơn 100
SELECT * FROM patients WHERE height > 100;

-- 3. 10 dòng có first_name 'John' và city 'Toronto', sắp xếp theo weight
SELECT * FROM patients
WHERE first_name = 'John' AND city = 'Toronto'
ORDER BY weight
LIMIT 10;

-- 4. Tất cả phụ nữ sống ở thành phố 'Ajax'
SELECT * FROM patients
WHERE gender = 'F' AND city = 'Ajax';

-- 5. Tất cả đàn ông sống ở tỉnh Ontario, hiển thị kèm tên tỉnh
SELECT p.first_name, p.last_name, pn.province_name
FROM patients p
JOIN province_names pn ON p.province_id = pn.province_id
WHERE p.gender = 'M' AND pn.province_name = 'Ontario';

-- 6. Đếm số bệnh nhân theo từng province_id (GROUP BY)
SELECT province_id, COUNT(*) AS so_benh_nhan
FROM patients
GROUP BY province_id;

-- 7. Các thành phố có nhiều hơn 1 bệnh nhân (GROUP BY + HAVING)
SELECT city, COUNT(*) AS so_benh_nhan
FROM patients
GROUP BY city
HAVING COUNT(*) > 1;

-- 8. Bệnh nhân có weight lớn hơn cân nặng trung bình (subquery)
SELECT first_name, last_name, weight
FROM patients
WHERE weight > (SELECT AVG(weight) FROM patients);
