# Demo Bài 4 — Spring Boot & MongoDB + Thymeleaf (import, list, pagination, sort, edit)

Project demo cho syllabus `java_m3_bai4_MongoDB_Spring_2.md`.

Hoàn thiện một ứng dụng web đầy đủ quản lý `restaurants`, **dùng thuần `@Controller` + Thymeleaf**
(không `@RestController`):

- Import dữ liệu từ **file NDJSON** (mỗi dòng 1 object) theo **batch**
- Hiển thị danh sách + **phân trang** + **sắp xếp**
- **Xem & chỉnh sửa** chi tiết một restaurant

## Yêu cầu

- JDK 17+
- MongoDB đang chạy ở `localhost:27017` (cùng database với Bài 3: `db_java_t3h_module3`)

## Chạy project

```bash
cd demo-bai4-mongodb-spring/java-springboot-bai4
./mvnw spring-boot:run
```

Mở: **http://localhost:8080/restaurants** (trang `/` tự redirect sang đây)

## Nạp dữ liệu

**Cách 1 — Upload qua web:** vào `/restaurants/upload`, chọn file
[`sample-data/restaurants-sample.ndjson`](sample-data/restaurants-sample.ndjson) (12 dòng) rồi Upload.

**Cách 2 — `mongoimport`** (collection phải khớp `restaurants`):

```bash
mongoimport --db db_java_t3h_module3 --collection restaurants --file sample-data/restaurants-sample.ndjson
```

> Bộ dữ liệu đầy đủ (~25k dòng) tải tại: https://www.w3resource.com/mongodb-exercises/restaurants.zip
> — file `restaurants.json` đó ở định dạng **NDJSON** (mỗi dòng 1 object), khớp với parser của demo.

## Màn hình

| Màn hình | URL | Method |
|----------|-----|--------|
| Trang upload | `/restaurants/upload` | GET |
| Xử lý upload | `/restaurants/upload` | POST |
| Danh sách + phân trang + sort | `/restaurants?page=&sortBy=&dir=` | GET |
| Chi tiết | `/restaurants/detail/{restaurant_id}` | GET |
| Cập nhật | `/restaurants/detail/{restaurant_id}` | POST |

## Điểm kỹ thuật quan trọng

- **`@Controller`** trả về tên template; `handleUpload` đưa thông báo vào `Model` rồi trả
  `restaurants/upload` (không return chuỗi thông báo làm tên view).
- Model lồng nhau: `RestaurantModel` → `AddressModel`, `List<GradeModel>`; `@Field("restaurant_id")` +
  `@JsonProperty("restaurant_id")`.
- Phân biệt **`_id`** (MongoDB) và **`restaurant_id`** (id nghiệp vụ) — chi tiết tìm theo
  `findFirstByRestaurantId`.
- Phân trang **0-indexed** (`PageRequest.of(page, size, sort)`), hiển thị `page + 1`.
- Import theo **batch 500** + tái sử dụng 1 `ObjectMapper`.

## Cấu trúc

```
src/main/java/vn/demo/
├── DemoBai4MongoApplication.java
├── model/{RestaurantModel,AddressModel,GradeModel}.java
├── repository/RestaurantRepository.java
├── service/RestaurantService.java       ← import NDJSON, pagination, update
├── dto/ImportResultDto.java
└── controller/
    ├── HomeController.java              ← / → /restaurants
    └── RestaurantViewController.java    ← @Controller (upload/list/detail/update)

src/main/resources/
├── application.properties               ← multipart + page-size
├── static/css/restaurants.css
└── templates/
    ├── fragments/layout.html
    └── restaurants/{upload,list,detail,not-found}.html

sample-data/restaurants-sample.ndjson    ← 12 dòng để test nhanh
```

## Liên kết

- Syllabus: [`syllabus/module-3/java_m3_bai4_MongoDB_Spring_2.md`](../syllabus/module-3/java_m3_bai4_MongoDB_Spring_2.md)
- Bài 3 (REST API + Thymeleaf): [`demo-bai3-mongodb-spring`](../demo-bai3-mongodb-spring)
