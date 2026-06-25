# Demo Bài 2 — NoSQL & MongoDB

Demo kèm theo bài [`java_m3_bai2_NoSQL_MongoDB.md`](../syllabus/module-3/java_m3_bai2_NoSQL_MongoDB.md). Cung cấp **bộ dữ liệu mẫu** (`products`, 12 sản phẩm) để học viên nạp vào MongoDB và chạy thử các câu lệnh CRUD / truy vấn trong bài.

## Nội dung file

| File | Vai trò |
|------|---------|
| `sample-data.json` | **Bộ dữ liệu mẫu dạng JSON** — dùng để **import vào MongoDB Compass** |
| `01-create-sample-data.mongodb` | Script tạo dữ liệu mẫu — dùng cho **`mongosh`** (thay cho việc import JSON) |
| `02-exercise-solutions.mongodb` | Đáp án phần bài tập trong syllabus |

> Tất cả ví dụ chạy trên database **`shop_db`**, collection **`products`**.

## Cách 1 — Import bằng MongoDB Compass (khuyến nghị cho người mới)

1. Mở **MongoDB Compass**, kết nối tới server (mặc định `mongodb://localhost:27017`).
2. Tạo database **`shop_db`** và collection **`products`**
   (nút **Create Database** → nhập `shop_db` / `products`).
3. Mở collection `products` → tab **Documents** → **Add Data** → **Import JSON or CSV file**.
4. Chọn file **`sample-data.json`**, định dạng **JSON**, bấm **Import**.
5. Kiểm tra: collection có **12 document** (Compass tự sinh `_id` cho mỗi document).

> Sau khi có dữ liệu, có thể gõ trực tiếp điều kiện lọc trong tab **Documents → Filter**
> (ví dụ `{ category: "laptop" }`) hoặc mở **MongoSH** tích hợp ở đáy cửa sổ Compass.

## Cách 2 — Nạp bằng mongosh (dòng lệnh)

```bash
cd demo-bai2-mongodb

# Cách 2a: chạy script tạo dữ liệu
mongosh < 01-create-sample-data.mongodb

# Cách 2b: import trực tiếp file JSON bằng mongoimport
mongoimport --db shop_db --collection products --file sample-data.json --jsonArray
```

Sau đó mở `mongosh` và gõ thử các câu lệnh trong syllabus (mục §6–§7), hoặc chạy file đáp án bài tập:

```bash
mongosh shop_db < 02-exercise-solutions.mongodb
```

## Cách 3 — Chạy MongoDB bằng Docker (nếu chưa cài server)

```bash
# Khoi dong MongoDB container
docker run --name mongo-bai2 -p 27017:27017 -d mongo:7

# Nap du lieu mau
docker exec -i mongo-bai2 mongoimport --db shop_db --collection products --jsonArray < sample-data.json

# Mo shell de chay thu
docker exec -it mongo-bai2 mongosh shop_db

# Don dep khi xong
docker rm -f mongo-bai2
```

## Lưu ý

- Khi **update** luôn dùng `{ $set: { ... } }`, nếu không document sẽ bị thay thế toàn bộ.
- `deleteMany({})` (điều kiện rỗng) sẽ **xóa sạch** collection — cẩn thận như `DELETE` không có `WHERE`.
- Sau khi insert/update/delete, dữ liệu sẽ thay đổi. Muốn reset về 12 document gốc, chạy lại `01-create-sample-data.mongodb` hoặc import lại `sample-data.json`.
- `_id` mặc định là `ObjectId` (không tự tăng như `AUTO_INCREMENT` của SQL).
