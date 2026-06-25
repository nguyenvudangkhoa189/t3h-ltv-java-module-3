# Demo Bài 1 — Cơ sở dữ liệu cơ bản (SQL)

Demo chạy được kèm theo bài [`java_m3_bai1_Basic_Database.md`](../syllabus/module-3/java_m3_bai1_Basic_Database.md). Minh họa các câu lệnh SQL nền tảng trên 2 bảng `patients` và `province_names` (nguồn dữ liệu: [sql-practice.com](https://www.sql-practice.com/)).

> **Thực hành trên MySQL.** Script `schema.sql` viết theo cú pháp MySQL (`AUTO_INCREMENT`, `CHECK`, `ENGINE=InnoDB`).

## Nội dung file

| File | Vai trò |
|------|---------|
| `schema.sql` | DDL — tạo bảng `province_names` (cha) và `patients` (con) kèm các constraint |
| `seed.sql` | DML — chèn dữ liệu mẫu (chèn bảng cha trước) |
| `queries.sql` | Ví dụ `SELECT`/`JOIN`/`INSERT`/`UPDATE`/`DELETE` (§6.2, 6.6–6.9) |
| `aggregate.sql` | `DISTINCT` / hàm tổng hợp / `GROUP BY` / `HAVING` / subquery (§6.3–6.5) |
| `exercises.sql` | Đáp án 8 câu thực hành §6.10 |

## Cách 1 — Thực hành nhanh trên web (không cài đặt)

1. Mở [https://www.sql-practice.com/](https://www.sql-practice.com/)
2. Chọn chế độ **MySQL** (góc trên), bộ dữ liệu `patients` / `province_names` đã có sẵn
3. Copy từng câu trong `queries.sql` / `aggregate.sql` / `exercises.sql` vào ô query và chạy

> Trên web không cần chạy `schema.sql` và `seed.sql` (dữ liệu đã dựng sẵn).

## Cách 2 — Chạy bằng MySQL (local)

```bash
cd demo-bai1-basic-database

mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS demo_bai1;"
mysql -u root -p demo_bai1 < schema.sql
mysql -u root -p demo_bai1 < seed.sql

mysql -u root -p demo_bai1 < queries.sql
mysql -u root -p demo_bai1 < aggregate.sql
mysql -u root -p demo_bai1 < exercises.sql
```

## Cách 3 — Chạy bằng MySQL trên Docker

```bash
# Khoi dong MySQL container
docker run --name mysql-bai1 -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=demo_bai1 -p 3306:3306 -d mysql:8

# Nap script (doi vai giay cho container san sang)
docker exec -i mysql-bai1 mysql -uroot -proot demo_bai1 < schema.sql
docker exec -i mysql-bai1 mysql -uroot -proot demo_bai1 < seed.sql
docker exec -i mysql-bai1 mysql -uroot -proot demo_bai1 < aggregate.sql

# Don dep khi xong
docker rm -f mysql-bai1
```

## Lưu ý

- Luôn dùng **dấu nháy thẳng** `'...'` cho chuỗi (không dùng `’ ’`).
- `UPDATE` / `DELETE` luôn kèm `WHERE` — xem cảnh báo trong syllabus §6.8–6.9.
- Chèn dữ liệu **bảng cha trước** bảng con để không vi phạm khóa ngoại.
- MySQL **không có `FULL JOIN`** — mô phỏng bằng `UNION` của `LEFT JOIN` và `RIGHT JOIN`.
- So sánh với `NULL` phải dùng `IS NULL` / `IS NOT NULL`, không dùng `= NULL`.
