-- =====================================================================
-- Bài 1: Ví dụ các câu lệnh SQL cơ bản (theo syllabus mục 6)
-- Chạy sau khi đã nạp schema.sql và seed.sql
-- =====================================================================

-- ---------- 6.2 SELECT ----------

-- Tất cả cột
SELECT * FROM patients;

-- Chọn vài cột
SELECT first_name, last_name, gender FROM patients;

-- Một điều kiện
SELECT first_name, last_name FROM patients
WHERE first_name = 'Rick';

-- Nhiều điều kiện
SELECT first_name, last_name FROM patients
WHERE first_name = 'Rick' AND last_name = 'Bennett';

-- Sắp xếp + giới hạn
SELECT first_name, weight FROM patients
ORDER BY weight DESC
LIMIT 10;

-- ---------- 6.6 JOIN (JOIN ... ON TRƯỚC WHERE) ----------

SELECT patients.patient_id, patients.first_name,
       patients.city, patients.province_id,
       province_names.province_name
FROM patients
JOIN province_names
  ON patients.province_id = province_names.province_id;

-- Dùng alias cho gọn
SELECT p.first_name, p.city, pn.province_name
FROM patients p
JOIN province_names pn ON p.province_id = pn.province_id
WHERE pn.province_name = 'Ontario';

-- ---------- 6.7 INSERT ----------

INSERT INTO patients (patient_id, first_name, last_name, gender, city, province_id)
VALUES (11, 'Maria', 'Nguyen', 'F', 'Toronto', 'ON');

-- ---------- 6.8 UPDATE (LUÔN có WHERE) ----------

UPDATE patients
SET last_name = 'Trump'
WHERE first_name = 'Donald';

-- ---------- 6.9 DELETE (LUÔN có WHERE) ----------

DELETE FROM patients
WHERE patient_id = 11;
