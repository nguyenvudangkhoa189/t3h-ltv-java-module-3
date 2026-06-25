-- =====================================================================
-- Bài 1: Dữ liệu mẫu (DML - INSERT)
-- Chèn bảng cha (province_names) TRƯỚC bảng con (patients)
-- để không vi phạm ràng buộc khóa ngoại.
-- =====================================================================

INSERT INTO province_names (province_id, province_name) VALUES
    ('ON', 'Ontario'),
    ('QC', 'Quebec'),
    ('BC', 'British Columbia'),
    ('AB', 'Alberta');

INSERT INTO patients
    (patient_id, first_name, last_name, gender, birth_date, city, province_id, allergies, height, weight)
VALUES
    (1,  'Donald', 'Waterfield', 'M', '1963-02-12', 'Barrie',   'ON', NULL,        156, 65),
    (2,  'Mickey', 'Baasha',     'M', '1981-05-28', 'Dundas',   'ON', 'Sulfa',     185, 79),
    (3,  'Jiji',   'Sharma',     'M', '1957-01-11', 'Hamilton', 'ON', NULL,        171, 64),
    (4,  'Blair',  'Diaz',       'M', '1967-01-03', 'Hamilton', 'ON', 'Penicillin',168, 92),
    (5,  'Charles','Wolfe',      'M', '2017-07-26', 'Ajax',     'ON', NULL,        102, 18),
    (6,  'Sue',    'Falla',      'F', '1973-09-02', 'Ajax',     'ON', NULL,        165, 60),
    (7,  'John',   'Dolan',      'M', '1992-11-15', 'Toronto',  'ON', NULL,        177, 70),
    (8,  'John',   'Smith',      'M', '1988-03-04', 'Toronto',  'ON', 'Codeine',   180, 85),
    (9,  'Maria',  'Tremblay',   'F', '1995-06-19', 'Montreal', 'QC', NULL,        160, 55),
    (10, 'Rick',   'Bennett',    'M', '1979-12-30', 'Vancouver','BC', NULL,        175, 88);
