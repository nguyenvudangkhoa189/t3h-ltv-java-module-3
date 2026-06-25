-- =====================================================================
-- Bài 1: Cơ sở dữ liệu cơ bản - SCHEMA (DDL) - MySQL
-- Tạo cấu trúc 2 bảng: province_names (bảng cha) và patients (bảng con)
-- Minh họa các constraint: PRIMARY KEY, NOT NULL, UNIQUE, DEFAULT,
-- CHECK, FOREIGN KEY, AUTO_INCREMENT (syllabus mục 4.5)
--
-- Thực hành nhanh không cần cài đặt: https://www.sql-practice.com/ (chế độ MySQL)
-- =====================================================================

DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS province_names;

-- Bảng cha: danh sách tỉnh/bang
CREATE TABLE province_names (
    province_id   VARCHAR(2)  NOT NULL,
    province_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (province_id)
) ENGINE=InnoDB;

-- Bảng con: bệnh nhân
-- province_id là FOREIGN KEY trỏ tới province_names(province_id)
CREATE TABLE patients (
    patient_id   INT          NOT NULL AUTO_INCREMENT,
    first_name   VARCHAR(30)  NOT NULL,
    last_name    VARCHAR(30)  NOT NULL,
    gender       CHAR(1)      NOT NULL,
    birth_date   DATE,
    city         VARCHAR(30),
    province_id  VARCHAR(2),
    allergies    VARCHAR(50),
    height       INT,
    weight       INT,
    is_active    BOOLEAN      DEFAULT TRUE,
    PRIMARY KEY (patient_id),
    CONSTRAINT chk_gender CHECK (gender IN ('M', 'F')),
    CONSTRAINT chk_height CHECK (height IS NULL OR height > 0),
    CONSTRAINT fk_province FOREIGN KEY (province_id)
        REFERENCES province_names (province_id)
) ENGINE=InnoDB;
