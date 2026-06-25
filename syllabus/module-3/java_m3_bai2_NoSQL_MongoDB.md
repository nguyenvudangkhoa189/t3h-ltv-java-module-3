# Bài 2: NoSQL & MongoDB

## Mục tiêu bài học

Sau bài này, học viên có thể:

- Giải thích **NoSQL** là gì, vì sao ra đời, và phân biệt **4 kiểu** NoSQL: document, key-value, wide-column, graph
- So sánh **SQL (relational)** và **NoSQL** về cấu trúc, độ nhất quán, độ linh động và cách mở rộng
- Mô tả mô hình dữ liệu của MongoDB: **Database → Collection → Document → Field** và vai trò của **`_id` (`ObjectId`)**
- Cài đặt **MongoDB Community Server**, dùng **`mongosh`** (shell) và **MongoDB Compass** (GUI)
- Thực hiện **CRUD** bằng cú pháp hiện đại: `insertOne` / `insertMany`, `find`, `updateOne` / `updateMany`, `deleteOne` / `deleteMany`, `countDocuments`
- Viết **điều kiện truy vấn** với toán tử: `$gt`, `$lt`, `$ne`, `$eq`, `$in`, `$nin`, `$and`, `$or`, `$exists`, `$regex`
- Dùng **projection**, **`sort` / `limit` / `skip`** để chọn cột và phân trang
- Đối chiếu mỗi lệnh MongoDB với lệnh **SQL tương ứng** đã học ở Bài 1

## Điều kiện tiên quyết

- Đã học **Bài 1 — Cơ sở dữ liệu cơ bản** (khái niệm database, SQL CRUD, PK/FK, structured vs semi-structured data)
- Hiểu cú pháp **JSON** ở mức cơ bản (object `{}`, array `[]`, cặp `key: value`)
- Máy tính đã cài hoặc sẵn sàng cài đặt **MongoDB**

> **Ghi chú:** Đây là bài **nhập môn NoSQL & MongoDB** ở mức thao tác bằng dòng lệnh (`mongosh`). Việc lập trình MongoDB bằng **Java / Spring Data MongoDB** sẽ được học ở **các bài sau** của Module 3 — bài này chỉ tập trung vào tư duy dữ liệu document và cú pháp truy vấn.

### Thời lượng gợi ý

| Phần | Thời gian |
|------|-----------|
| Lý thuyết §1–4 (NoSQL & MongoDB) | ~30 phút |
| Cài đặt §5 | ~15 phút |
| CRUD + truy vấn §6–7 (có demo) | ~35 phút |
| Đối chiếu SQL §8 + bài tập | ~20 phút |

## Nội dung

| # | Chủ đề |
|---|--------|
| 1 | NoSQL là gì? |
| 2 | Bốn kiểu cơ sở dữ liệu NoSQL |
| 3 | Sự khác biệt giữa SQL và NoSQL |
| 4 | MongoDB & mô hình Collection / Document / Field |
| 5 | Cài đặt MongoDB + bộ dữ liệu mẫu |
| 6 | Các lệnh CRUD cơ bản |
| 7 | Truy vấn nâng cao (toán tử, `$regex`, `$exists`, projection, sort) |
| 8 | Đối chiếu MongoDB ↔ SQL |
| 9 | Lỗi thường gặp |
| Phụ lục | Demo & dữ liệu mẫu · Bài tập + đáp án · Câu hỏi ôn tập · Liên kết tham khảo |

---

## 1. NoSQL là gì?

- **NoSQL** được hiểu đơn giản là **"not only SQL"** hoặc **"non-relational"** — một hệ cơ sở dữ liệu **không dựa trên mô hình hàng–cột** truyền thống của SQL.
- NoSQL lưu trữ dữ liệu theo các **định dạng linh động hơn**, tùy vào yêu cầu của hệ thống hoặc nghiệp vụ.
- NoSQL **không phải là sự thay thế** cho SQL. Nó **khắc phục điểm yếu** của SQL truyền thống khi cần xử lý:
  - **Dữ liệu phi cấu trúc** (unstructured data)
  - **Dữ liệu lớn** (big data)
  - **Thời gian thực** (real-time)
- Vì cấu trúc linh động, NoSQL **dễ mở rộng (scalability)** trong các hệ thống phân tán (distributed architectures).

> **Ẩn dụ:** SQL giống một **tủ hồ sơ với mẫu in sẵn** — mọi tờ khai phải cùng một form (cùng số cột, cùng kiểu). NoSQL (document) giống một **hộp đựng tài liệu linh hoạt** — mỗi tờ có thể có cấu trúc khác nhau, miễn là vẫn tìm và đọc được. Khi nghiệp vụ thay đổi (thêm thông tin mới), bạn không phải "làm lại toàn bộ form".

---

## 2. Bốn kiểu cơ sở dữ liệu NoSQL

| Kiểu | Cách lưu trữ | DBMS tiêu biểu | Ứng dụng tiêu biểu |
|------|--------------|----------------|--------------------|
| **Document** | Dữ liệu theo chuẩn **JSON / BSON** | MongoDB, CouchDB | Dữ liệu sản phẩm, blog, thông tin khách hàng |
| **Key-Value** | Cặp `<khóa> : <giá trị>` (vd `name: "Michael"`, `age: 39`) | Redis, DynamoDB | Cache, thông tin đăng nhập (session), giỏ hàng |
| **Wide-column** | Theo hàng và cột, nhưng **kiểu giá trị các cột không cố định** | Cassandra, HBase | IoT, dữ liệu theo thời gian (giá chứng khoán...) |
| **Graph** | Các đối tượng **liên kết với nhau** | Neo4j, Neptune | Mạng xã hội, chuỗi cung ứng |

> **Trọng tâm:** Module 3 tập trung vào **Document database — MongoDB**, vì đây là kiểu NoSQL phổ biến nhất trong phát triển ứng dụng web và gần gũi nhất với tư duy "đối tượng" của lập trình viên (1 document ≈ 1 object JSON).

---

## 3. Sự khác biệt giữa SQL và NoSQL

| Tiêu chí | SQL (Relational) | NoSQL |
|----------|------------------|-------|
| **Cập nhật dữ liệu** | Đồng bộ | Bất đồng bộ |
| **Dữ liệu** | Có cấu trúc (schema cố định) | Bán cấu trúc (schema linh hoạt) |
| **Độ linh động** | Ít | Nhiều |
| **Độ nhất quán** | Mạnh | Trung bình |
| **Cách mở rộng** | Theo **chiều dọc** (nâng cấp phần cứng 1 máy) | Theo **chiều ngang** (thêm nhiều máy/node) |
| **Mô hình giao dịch** | **ACID** (chặt chẽ) | **BASE** / nhất quán cuối (eventual consistency) |

> **Nối tiếp Bài 1:** Ở Bài 1 ta đã gặp cặp khái niệm **ACID vs BASE**. SQL ưu tiên **đúng tuyệt đối ngay lập tức** (phù hợp ngân hàng, giao dịch tiền). NoSQL chấp nhận **"đúng sau một khoảng rất ngắn"** (eventual consistency) để đổi lấy tốc độ và khả năng mở rộng — phù hợp mạng xã hội, đếm lượt xem, log...

> **Lưu ý không hiểu sai:** "NoSQL nhất quán trung bình" **không có nghĩa là kém**. Đó là **đánh đổi có chủ đích**: hi sinh một phần tính nhất quán tức thời để có hiệu năng và khả năng scale ngang. Chọn SQL hay NoSQL tùy **bài toán**, không phải cái nào "tốt hơn".

---

## 4. MongoDB & mô hình Collection / Document / Field

- **MongoDB** là một hệ quản trị cơ sở dữ liệu **mã nguồn mở (open-source)** thuộc nhóm **Document NoSQL**.
- Dữ liệu được lưu dạng **JSON** (thực chất bên trong là **BSON** — Binary JSON), thay cho table truyền thống.

### 4.1. Ánh xạ khái niệm với SQL

| MongoDB | Tương đương SQL (Bài 1) | Ý nghĩa |
|---------|--------------------------|---------|
| **Database** | Database | Tập hợp các collection |
| **Collection** | Table (bảng) | Nhóm các document cùng loại |
| **Document** | Row (dòng) | Một bản ghi dạng JSON |
| **Field** | Column (cột) | Một thuộc tính của document |
| **`_id`** | Primary Key | Khóa định danh duy nhất của document |

### 4.2. `_id` và `ObjectId`

- Mỗi document **tự động** có một field **`_id`** làm **khóa chính** (duy nhất trong collection).
- Nếu không tự gán, MongoDB sinh giá trị kiểu **`ObjectId`** — chuỗi **12 byte** chứa sẵn **timestamp** thời điểm tạo.

> **Khác biệt quan trọng với SQL:** `_id` **không phải số tự tăng** như `AUTO_INCREMENT` ở Bài 1. Nó là `ObjectId` ngẫu nhiên-có-thứ-tự-thời-gian, ví dụ `ObjectId("66a3f1c8e13b2a0012ab34cd")`. Vì vậy đừng mong `_id` chạy 1, 2, 3... như `patient_id` trong SQL.

### 4.3. Document linh hoạt: nested & array

Khác với bảng SQL **phẳng** (mỗi ô 1 giá trị đơn), một document MongoDB có thể chứa **object lồng nhau (nested)** và **mảng (array)**:

```json
{
  "_id": ObjectId("66a3f1c8e13b2a0012ab34cd"),
  "name": "Laptop Dell XPS 13",
  "price": 1500,
  "inStock": true,
  "tags": ["laptop", "dell", "ultrabook"],
  "specs": { "ram": "16GB", "cpu": "i7", "ssd": "512GB" }
}
```

> Ở SQL, để biểu diễn `tags` (nhiều giá trị) và `specs` (nhiều thuộc tính con) bạn thường phải tách thêm bảng và JOIN. Trong MongoDB, tất cả **đi cùng nhau trong một document** → đọc nhanh, đúng "hình dạng" của object trong code. Đây chính là nền cho khái niệm **embedding (nhúng) vs referencing (tham chiếu)** sẽ học sâu ở bài sau.

---

## 5. Cài đặt MongoDB

1. **MongoDB Community Server** (bản miễn phí):
   <https://www.mongodb.com/docs/manual/administration/install-community/>
2. **`mongosh`** — MongoDB Shell, công cụ dòng lệnh để chạy truy vấn:
   <https://www.mongodb.com/docs/mongodb-shell/install/>
3. **MongoDB Compass** — giao diện đồ họa (GUI) chính chủ, miễn phí để xem/sửa dữ liệu trực quan:
   <https://www.mongodb.com/products/tools/compass>

> **Ghi chú công cụ:** Một số tài liệu cũ giới thiệu *Robomongo / Robo 3T* hoặc *Studio 3T*. Bài này khuyến nghị **MongoDB Compass** (chính chủ) kết hợp **`mongosh`** vì luôn cập nhật theo phiên bản mới nhất và miễn phí. Studio 3T là tùy chọn nâng cao (có bản trả phí).

### 5.1. Các lệnh môi trường cơ bản trong `mongosh`

```javascript
show dbs                       // liệt kê các database hiện có
use shop_db                    // chuyển sang (tạo) database "shop_db"
db.createCollection("products")  // tạo collection mới
show collections               // liệt kê collection trong db hiện tại
db                             // xem db đang làm việc
```

> **Lưu ý đặt tên:** Tên database và collection **không chứa khoảng trắng**. MongoDB chỉ thực sự tạo database/collection khi có **dữ liệu đầu tiên** được ghi vào.

### 5.2. Chuẩn bị bộ dữ liệu mẫu (dùng cho toàn bài)

Tất cả ví dụ trong §6–§7 và phần bài tập đều chạy trên database **`shop_db`**, collection **`products`** (12 sản phẩm). Bộ dữ liệu và script được đặt sẵn trong thư mục demo:

📁 **[`demo-bai2-mongodb/`](../../demo-bai2-mongodb/)**

| File | Dùng để | Cách nạp |
|------|---------|----------|
| [`sample-data.json`](../../demo-bai2-mongodb/sample-data.json) | **Import vào MongoDB Compass** | Compass → collection `products` → **Add Data → Import JSON** |
| [`01-create-sample-data.mongodb`](../../demo-bai2-mongodb/01-create-sample-data.mongodb) | Nạp bằng shell | `mongosh < 01-create-sample-data.mongodb` |
| [`02-exercise-solutions.mongodb`](../../demo-bai2-mongodb/02-exercise-solutions.mongodb) | Đáp án bài tập | — |

**Nạp nhanh bằng Compass (khuyến nghị cho người mới):**

1. Mở Compass → tạo database `shop_db`, collection `products`.
2. Mở collection → **Add Data → Import JSON or CSV file** → chọn `sample-data.json` → **Import**.
3. Kiểm tra: collection có **12 document** (Compass tự sinh `_id`). Xem hướng dẫn chi tiết trong [README của demo](../../demo-bai2-mongodb/README.md).

**Một document mẫu trông như sau** (xem đầy đủ 12 sản phẩm trong [`sample-data.json`](../../demo-bai2-mongodb/sample-data.json)):

```json
{
  "name": "Laptop Dell XPS 13",
  "price": 1500,
  "category": "laptop",
  "inStock": true,
  "tags": ["laptop", "dell", "ultrabook"],
  "specs": { "ram": "16GB", "cpu": "i7" }
}
```

**Đặc điểm bộ dữ liệu (cố ý thiết kế để minh họa nhiều tình huống):**

- **12 document** — đủ để minh họa `sort` / `limit` / `skip` (phân trang).
- Nhiều `category`: `laptop`, `phone`, `tablet`, `accessory`.
- Có sản phẩm **hết hàng** (`inStock: false`) để luyện lọc và xóa.
- Một số sản phẩm **không có `specs`**, riêng `USB-C Cable` **không có cả `tags` lẫn `specs`** — dùng để minh họa toán tử `$exists` (§7.7). Đây chính là tính **schema linh hoạt** của MongoDB: các document trong cùng collection **không bắt buộc cùng số field**.

> **Kiểm tra nhanh sau khi nạp:** `db.products.countDocuments({})` phải trả về **12**.

---

## 6. Các lệnh CRUD cơ bản

> Toàn bộ ví dụ trong bài dùng collection **`products`** với [bộ dữ liệu mẫu ở §5.2](#52-chuẩn-bị-bộ-dữ-liệu-mẫu-dùng-cho-toàn-bài). Cú pháp gọi thống nhất theo dạng `db.<collection>.<method>(...)`. Phần chú thích chỉ giải thích **ý nghĩa** câu lệnh; kết quả thực tế tùy thuộc dữ liệu trong collection của bạn.

### 6.1. Create — Tạo document

**Thêm một document:**

```javascript
db.products.insertOne({
  name: "Apple Watch Series 9",
  price: 399,
  category: "accessory",
  inStock: true
});
```

**Thêm nhiều document cùng lúc:**

```javascript
db.products.insertMany([
  { name: "Kindle Paperwhite", price: 139, category: "accessory", inStock: true },
  { name: "LG Monitor 27",     price: 320, category: "accessory", inStock: false }
]);
```

> **`_id` tự sinh:** Nếu không truyền `_id`, mỗi document được gán một `ObjectId` tự động. Bạn có thể tự đặt `_id` (vd `_id: "SP001"`) nhưng phải đảm bảo **duy nhất**, nếu trùng sẽ báo lỗi `duplicate key`.

### 6.2. Read — Tìm document

**Cú pháp:**

```javascript
db.products.find(<điều kiện tìm>, <các field cần lấy>);
```

**Lấy tất cả document:**

```javascript
db.products.find({});
```

**Tìm theo điều kiện (lọc bằng giá trị chính xác):**

```javascript
// Lấy tất cả sản phẩm có category = "laptop"
db.products.find({ category: "laptop" });
```

**Lấy đúng một document đầu tiên khớp điều kiện:**

```javascript
// find trả về danh sách (con trỏ); findOne trả về 1 object đầu tiên khớp
db.products.findOne({ name: "iPhone 15" });
```

> Chi tiết về projection (chọn field), sort, limit và các toán tử điều kiện xem [§7](#7-truy-vấn-nâng-cao).

### 6.3. Update — Cập nhật document

**Cú pháp (luôn dùng toán tử `$set`):**

```javascript
db.products.updateOne(<điều kiện tìm>, { $set: <dữ liệu mới> });
```

**Cập nhật document đầu tiên khớp điều kiện:**

```javascript
// Giảm giá iPhone 15 xuống 899
db.products.updateOne(
  { name: "iPhone 15" },
  { $set: { price: 899 } }
);
```

**Cập nhật nhiều document khớp điều kiện:**

```javascript
// Đánh dấu hết hàng cho mọi sản phẩm category "accessory"
db.products.updateMany(
  { category: "accessory" },
  { $set: { inStock: false } }
);
```

> ⚠️ **CẢNH BÁO — luôn dùng `$set`:** Nếu viết `updateOne({name:"iPhone 15"}, {price: 899})` (thiếu `$set`), MongoDB sẽ **thay thế TOÀN BỘ document** chỉ còn lại field `price` — mất hết `name`, `category`... Đây là lỗi nghiêm trọng tương đương "quên `WHERE` khi `UPDATE`" ở SQL (Bài 1). Hãy luôn bọc dữ liệu mới trong `{ $set: { ... } }`.

### 6.4. Delete — Xóa document

**Xóa document đầu tiên khớp điều kiện:**

```javascript
db.products.deleteOne({ name: "AirPods Pro" });
```

**Xóa nhiều document khớp điều kiện:**

```javascript
db.products.deleteMany({ inStock: false });
```

> ⚠️ **CẢNH BÁO:** `db.products.deleteMany({})` (điều kiện rỗng) sẽ **xóa SẠCH collection** — giống `DELETE FROM ...` không có `WHERE`. Mẹo an toàn: chạy `find()` với cùng điều kiện trước để xem mình sắp xóa những gì.

### 6.5. Count — Đếm số lượng document

```javascript
// Đếm tổng số document trong collection
db.products.countDocuments({});

// Đếm số document thỏa điều kiện (giá lớn hơn 100)
db.products.countDocuments({ price: { $gt: 100 } });
```

### 6.6. Lưu ý về cú pháp cũ (legacy)

Một số tài liệu cũ dùng `db.collection.update(...)`, `db.collection.remove(...)` và `.count()`. Các hàm này **đã bị loại bỏ / không khuyến nghị** trong `mongosh` và driver hiện đại. **Hãy luôn dùng** bộ hàm mới:

| Cũ (không dùng) | Mới (dùng cái này) |
|-----------------|--------------------|
| `update()` | `updateOne()` / `updateMany()` |
| `remove()` | `deleteOne()` / `deleteMany()` |
| `find().count()` | `countDocuments()` |
| `insert()` | `insertOne()` / `insertMany()` |

---

## 7. Truy vấn nâng cao

### 7.1. Toán tử so sánh

| Toán tử | Ý nghĩa | SQL tương đương |
|---------|---------|-----------------|
| `$eq` | Bằng | `=` |
| `$ne` | Không bằng | `<>` / `!=` |
| `$gt` | Lớn hơn | `>` |
| `$gte` | Lớn hơn hoặc bằng | `>=` |
| `$lt` | Nhỏ hơn | `<` |
| `$lte` | Nhỏ hơn hoặc bằng | `<=` |
| `$in` | Thuộc một trong các giá trị | `IN (...)` |
| `$nin` | Không thuộc các giá trị | `NOT IN (...)` |

```javascript
// Sản phẩm có giá lớn hơn 100
db.products.find({ price: { $gt: 100 } });

// Giá nằm trong khoảng [200, 1000] — kết hợp $gte và $lte trên cùng 1 field
db.products.find({ price: { $gte: 200, $lte: 1000 } });

// category thuộc danh sách (giống IN của SQL)
db.products.find({ category: { $in: ["laptop", "phone"] } });

// category KHÁC "accessory"
db.products.find({ category: { $ne: "accessory" } });

// category KHÔNG thuộc danh sách (giống NOT IN của SQL)
db.products.find({ category: { $nin: ["laptop", "phone"] } });
```

### 7.2. Toán tử logic

| Toán tử | Ý nghĩa | SQL tương đương |
|---------|---------|-----------------|
| `$and` | Thỏa **tất cả** điều kiện | `AND` |
| `$or` | Thỏa **ít nhất một** điều kiện | `OR` |
| `$not` | Phủ định | `NOT` |
| `$nor` | Không thỏa điều kiện nào | `NOT (... OR ...)` |

```javascript
// AND ngầm định: nhiều field trong cùng object là AND
// (vừa là laptop, vừa còn hàng)
db.products.find({ category: "laptop", inStock: true });

// OR: là laptop HOẶC có giá dưới 300
db.products.find({
  $or: [
    { category: "laptop" },
    { price: { $lt: 300 } }
  ]
});
```

> **Mẹo:** Khi liệt kê nhiều field trong cùng một object `{ }`, MongoDB hiểu ngầm là **AND**. Chỉ cần `$and` tường minh khi có nhiều điều kiện **trên cùng một field** mà cú pháp object không gộp được.

### 7.3. Toán tử `$regex` — tương đương `LIKE` của SQL

SQL dùng `LIKE '%abc%'` để tìm chuỗi gần đúng. MongoDB **không có `LIKE`** mà dùng **`$regex`** (biểu thức chính quy — regular expression), mạnh hơn nhiều.

| Yêu cầu | SQL (`LIKE`) | MongoDB (`$regex`) |
|---------|--------------|--------------------|
| Chứa "Dell" | `name LIKE '%Dell%'` | `{ name: { $regex: "Dell" } }` |
| Bắt đầu bằng "Lap" | `name LIKE 'Lap%'` | `{ name: { $regex: "^Lap" } }` |
| Kết thúc bằng "Pro" | `name LIKE '%Pro'` | `{ name: { $regex: "Pro$" } }` |
| Không phân biệt hoa/thường | (tùy DBMS) | `{ name: { $regex: "dell", $options: "i" } }` |

```javascript
// Tên sản phẩm CHỨA "Dell" (bất kỳ vị trí nào)
db.products.find({ name: { $regex: "Dell" } });

// Tên BẮT ĐẦU bằng "Lap" — dấu ^ neo đầu chuỗi
db.products.find({ name: { $regex: "^Lap" } });

// Tên KẾT THÚC bằng "Pro" — dấu $ neo cuối chuỗi
// (lưu ý: "MacBook Pro 14" KHÔNG khớp vì kết thúc bằng "14", không phải "Pro")
db.products.find({ name: { $regex: "Pro$" } });

// Không phân biệt hoa/thường nhờ option "i" (insensitive)
db.products.find({ name: { $regex: "iphone", $options: "i" } });
```

**Giải thích ký hiệu regex hay dùng:**

| Ký hiệu | Ý nghĩa | Tương tự `LIKE` |
|---------|---------|-----------------|
| `^` | Neo **đầu** chuỗi | `'abc%'` |
| `$` | Neo **cuối** chuỗi | `'%abc'` |
| *(không neo)* | Khớp ở **bất kỳ đâu** | `'%abc%'` |
| `.` | Một ký tự bất kỳ | `_` (underscore) trong SQL |
| `$options: "i"` | Bỏ qua hoa/thường | — |

> **Lưu ý hiệu năng:** `$regex` không có neo `^` (tìm "chứa ở giữa") **không tận dụng được index** → chậm trên dữ liệu lớn, giống `LIKE '%abc%'` ở SQL. Với nhu cầu tìm kiếm văn bản nâng cao, MongoDB còn có **text index**, sẽ tìm hiểu sau.

### 7.4. Projection — chọn field cần lấy

Tham số thứ hai của `find()` quy định **field nào được trả về** (`1` = lấy, `0` = bỏ):

```javascript
// Chỉ lấy name và price, ẩn _id
db.products.find({}, { name: 1, price: 1, _id: 0 });
```

| Projection | SQL tương đương |
|------------|-----------------|
| `find({}, { name: 1, price: 1 })` | `SELECT name, price, _id ...` |
| `find({}, { name: 1, _id: 0 })` | `SELECT name ...` |

> **Quy tắc:** Trong một projection **không trộn** `1` và `0` (trừ trường hợp ẩn `_id`). Hoặc liệt kê field cần **lấy** (dùng `1`), hoặc liệt kê field cần **bỏ** (dùng `0`).

### 7.5. `sort`, `limit`, `skip` — sắp xếp & phân trang

```javascript
// Sắp xếp theo giá giảm dần (1 = tăng dần, -1 = giảm dần)
db.products.find({}).sort({ price: -1 });

// Trang 1: lấy 5 sản phẩm đắt nhất
db.products.find({}).sort({ price: -1 }).limit(5);

// Trang 2: bỏ qua 5, lấy 5 tiếp theo
db.products.find({}).sort({ price: -1 }).skip(5).limit(5);

// Trang 3: bỏ qua 10, lấy 5 tiếp theo
db.products.find({}).sort({ price: -1 }).skip(10).limit(5);
```

> **Công thức phân trang:** với cỡ trang `n`, để lấy **trang thứ `k`** dùng `.skip((k-1) * n).limit(n)`. Ví dụ trang 2, mỗi trang 5 → `.skip(5).limit(5)`.

| MongoDB | SQL tương đương |
|---------|-----------------|
| `.sort({ price: -1 })` | `ORDER BY price DESC` |
| `.limit(5)` | `LIMIT 5` |
| `.skip(10)` | `OFFSET 10` |

### 7.6. Truy vấn vào array & nested field

```javascript
// Tìm sản phẩm có "dell" trong mảng tags (MongoDB tự khớp phần tử trong array)
db.products.find({ tags: "dell" });

// Truy vấn field lồng nhau bằng "dấu chấm" (dot notation)
db.products.find({ "specs.ram": "16GB" });
```

> Đây là khả năng mà bảng SQL phẳng không làm trực tiếp được (phải tách bảng + JOIN). Ghi nhớ **dot notation** `"specs.ram"` để truy cập field con.

### 7.7. `$exists` — kiểm tra field có tồn tại hay không

Vì mỗi document có thể có số field khác nhau (schema linh hoạt), ta thường cần kiểm tra **một field có tồn tại hay không**. SQL không có khái niệm này (mọi dòng đều có đủ cột, chỉ có thể là `NULL`).

```javascript
// Sản phẩm KHÔNG có field "tags"
db.products.find({ tags: { $exists: false } });

// Sản phẩm CÓ field "specs"
db.products.find({ specs: { $exists: true } });
```

> **Phân biệt với `null`:** `{ field: { $exists: false } }` = field **không tồn tại** trong document; còn `{ field: null }` lại khớp cả document **không có field** lẫn document có `field: null`. Trong MongoDB hai khái niệm này khác nhau — đây là điểm dễ nhầm với người quen SQL.

---

## 8. Đối chiếu MongoDB ↔ SQL

Bảng tổng hợp giúp chuyển nhanh kiến thức SQL (Bài 1) sang MongoDB:

| Thao tác | SQL (Bài 1) | MongoDB (Bài 2) |
|----------|-------------|-----------------|
| Lấy tất cả | `SELECT * FROM products` | `db.products.find({})` |
| Chọn vài cột | `SELECT name, price FROM ...` | `db.products.find({}, {name:1, price:1, _id:0})` |
| Lọc điều kiện | `WHERE price > 100` | `{ price: { $gt: 100 } }` |
| Nhiều điều kiện AND | `WHERE a=1 AND b=2` | `{ a: 1, b: 2 }` |
| Điều kiện OR | `WHERE a=1 OR b=2` | `{ $or: [ {a:1}, {b:2} ] }` |
| Tìm gần đúng | `WHERE name LIKE '%Dell%'` | `{ name: { $regex: "Dell" } }` |
| Thuộc danh sách | `WHERE category IN ('a','b')` | `{ category: { $in: ["a","b"] } }` |
| Thêm | `INSERT INTO ... VALUES ...` | `db.products.insertOne({...})` |
| Sửa | `UPDATE ... SET ... WHERE` | `db.products.updateOne({đk}, {$set:{...}})` |
| Xóa | `DELETE FROM ... WHERE` | `db.products.deleteOne({đk})` |
| Đếm | `SELECT COUNT(*) ...` | `db.products.countDocuments({})` |
| Sắp xếp | `ORDER BY price DESC` | `.sort({ price: -1 })` |
| Giới hạn | `LIMIT 5` | `.limit(5)` |
| Phân trang | `LIMIT 5 OFFSET 10` | `.skip(10).limit(5)` |

---

## 9. Lỗi thường gặp

| Triệu chứng | Nguyên nhân | Cách xử lý |
|-------------|-------------|------------|
| `TypeError: db.col.update is not a function` | Dùng API legacy đã bị gỡ | Dùng `updateOne` / `updateMany` |
| Sau khi update document mất hết field, chỉ còn 1 field | Quên `$set` → bị thay thế toàn bộ | Luôn bọc `{ $set: { ... } }` |
| Lỡ xóa/sửa toàn bộ collection | Truyền điều kiện rỗng `{}` | Kiểm tra điều kiện; `find()` thử trước |
| Tìm không ra dù có dữ liệu | Sai **kiểu dữ liệu** | `{price: 100}` (số) khác `{price: "100"}` (chuỗi) |
| Tìm theo `_id` không khớp | So sánh chuỗi với `ObjectId` | Bọc đúng kiểu: `{ _id: ObjectId("...") }` |
| `$regex` chạy rất chậm | Regex không neo `^`, quét toàn bộ | Neo đầu `^` để dùng index, hoặc dùng text index |
| `E11000 duplicate key error` | Tự gán `_id` bị trùng | Đổi `_id` khác hoặc để MongoDB tự sinh |
| Dùng dấu nháy cong `" "` khi copy | Copy từ tài liệu định dạng | Dùng nháy thẳng `"..."` / `'...'` |

---

## Tóm tắt

| Khái niệm | Ý chính |
|-----------|---------|
| **NoSQL** | "Not only SQL" — schema linh hoạt, xử lý dữ liệu phi cấu trúc/big data/real-time, scale ngang |
| **4 kiểu NoSQL** | Document, Key-Value, Wide-column, Graph |
| **SQL vs NoSQL** | Schema cứng + ACID vs schema mềm + BASE; mở rộng dọc vs ngang |
| **Mô hình MongoDB** | Database → Collection (≈table) → Document (≈row, JSON) → Field (≈column) |
| **`_id`** | Khóa chính, mặc định `ObjectId` (12 byte, có timestamp) — **không** tự tăng như SQL |
| **CRUD hiện đại** | `insertOne/Many`, `find`, `updateOne/Many` (luôn `$set`), `deleteOne/Many`, `countDocuments` |
| **Toán tử** | So sánh (`$gt`,`$lt`,`$in`...), logic (`$and`,`$or`), `$regex` (≈`LIKE`), `$exists` |
| **Projection / sort / limit / skip** | Chọn field, sắp xếp, phân trang (≈ `SELECT cột` / `ORDER BY` / `LIMIT` / `OFFSET`) |
| **Document linh hoạt** | Nested object + array trong cùng 1 document; truy vấn bằng dot notation |

**Chuẩn bị cho bài sau:** Tìm hiểu cách **thiết kế mô hình dữ liệu document** (khi nào **nhúng — embed**, khi nào **tham chiếu — reference**) và lập trình MongoDB từ ứng dụng.

---

## Phụ lục

### Demo & dữ liệu mẫu

Toàn bộ file demo nằm trong [`demo-bai2-mongodb/`](../../demo-bai2-mongodb/):

| File | Nội dung |
|------|----------|
| [`sample-data.json`](../../demo-bai2-mongodb/sample-data.json) | Bộ dữ liệu mẫu (12 sản phẩm) để **import vào MongoDB Compass** |
| [`01-create-sample-data.mongodb`](../../demo-bai2-mongodb/01-create-sample-data.mongodb) | Script tạo dữ liệu mẫu bằng `mongosh` |
| [`02-exercise-solutions.mongodb`](../../demo-bai2-mongodb/02-exercise-solutions.mongodb) | Đáp án phần bài tập bên dưới |

Cách nạp dữ liệu xem [§5.2](#52-chuẩn-bị-bộ-dữ-liệu-mẫu-dùng-cho-toàn-bài) hoặc [README của demo](../../demo-bai2-mongodb/README.md).

> **Reset dữ liệu:** Vì các bài tập có sửa/xóa dữ liệu, nếu muốn làm lại từ đầu, chỉ cần chạy lại [`01-create-sample-data.mongodb`](../../demo-bai2-mongodb/01-create-sample-data.mongodb) (đã có `deleteMany({})` ở đầu để dọn sạch) hoặc import lại `sample-data.json`.

### Bài tập

Viết câu lệnh MongoDB cho từng yêu cầu (đáp án ở cuối). Thực hành trên `products` (nạp dữ liệu mẫu §5.2 trước):

1. Thêm một sản phẩm mới tùy ý vào collection.
2. Tìm tất cả sản phẩm thuộc `category` là `"phone"`.
3. Tìm tất cả sản phẩm có `price` lớn hơn 500.
4. Tìm tất cả sản phẩm có giá trong khoảng từ 200 đến 1000.
5. Tìm tất cả sản phẩm có tên **chứa** chữ `"Laptop"` (dùng `$regex`).
6. Chỉ lấy `name` và `price` của mọi sản phẩm, ẩn `_id`.
7. Lấy 3 sản phẩm đắt nhất (sắp xếp giảm dần theo `price`).
8. Tìm các sản phẩm có gắn tag `"apple"` (truy vấn vào array `tags`).
9. Tìm các sản phẩm **không có** field `specs` (dùng `$exists`).
10. Giảm giá sản phẩm `"iPhone 15"` xuống `899`.
11. Đánh dấu `inStock: false` cho tất cả sản phẩm `category` là `"accessory"`.
12. Xóa tất cả sản phẩm đang hết hàng (`inStock: false`).
13. Đếm số sản phẩm có giá lớn hơn 100.

<details>
<summary>Xem đáp án</summary>

```javascript
// 1.
db.products.insertOne({ name: "Apple Watch", price: 399, category: "accessory", inStock: true });

// 2.
db.products.find({ category: "phone" });

// 3.
db.products.find({ price: { $gt: 500 } });

// 4.
db.products.find({ price: { $gte: 200, $lte: 1000 } });

// 5.
db.products.find({ name: { $regex: "Laptop" } });

// 6.
db.products.find({}, { name: 1, price: 1, _id: 0 });

// 7.
db.products.find({}).sort({ price: -1 }).limit(3);

// 8.
db.products.find({ tags: "apple" });

// 9.
db.products.find({ specs: { $exists: false } });

// 10.
db.products.updateOne({ name: "iPhone 15" }, { $set: { price: 899 } });

// 11.
db.products.updateMany({ category: "accessory" }, { $set: { inStock: false } });

// 12.
db.products.deleteMany({ inStock: false });

// 13.
db.products.countDocuments({ price: { $gt: 100 } });
```

</details>

### Câu hỏi ôn tập

1. NoSQL nghĩa là gì? Nó thay thế hay bổ sung cho SQL?
2. Kể tên 4 kiểu cơ sở dữ liệu NoSQL và một DBMS tiêu biểu của mỗi kiểu.
3. MongoDB thuộc kiểu NoSQL nào? Vì sao gọi là "document database"?
4. Ánh xạ các khái niệm: Collection, Document, Field tương ứng với gì trong SQL?
5. `_id` mặc định có kiểu gì? Vì sao nó **không** giống `AUTO_INCREMENT` của SQL?
6. Vì sao **không nên** dùng `db.collection.update()` và `remove()` nữa? Thay bằng gì?
7. Điều gì xảy ra nếu `updateOne` mà quên `$set`?
8. Toán tử nào trong MongoDB tương đương `LIKE` của SQL? Viết câu tìm tên chứa "Pro".
9. Phân biệt `$in` và `$or`. Khi nào dùng cái nào?
10. Viết projection để chỉ lấy `name` và ẩn `_id`.
11. Làm thế nào để phân trang (lấy trang 2, mỗi trang 5 document)?
12. Một document MongoDB khác một row SQL ở điểm gì (gợi ý: nested, array)?

### Liên kết tham khảo

- [MongoDB CRUD Operations](https://www.mongodb.com/docs/manual/crud/)
- [MongoDB Query Operators](https://www.mongodb.com/docs/manual/reference/operator/query/)
- [`$regex` operator](https://www.mongodb.com/docs/manual/reference/operator/query/regex/)
- [MongoDB Shell (`mongosh`)](https://www.mongodb.com/docs/mongodb-shell/)
- [MongoDB Compass](https://www.mongodb.com/products/tools/compass)
- [Install MongoDB Community](https://www.mongodb.com/docs/manual/administration/install-community/)
