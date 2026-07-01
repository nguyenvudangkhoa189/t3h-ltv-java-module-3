# Demo Bài 5 — Mối quan hệ collections MongoDB: `$lookup`, Transaction, Thymeleaf

Project demo cho syllabus [`java_m3_bai5_Relationship_in_MongoDB.md`](../syllabus/module-3/java_m3_bai5_Relationship_in_MongoDB.md).

Minh họa quan hệ **1-n** giữa 2 collection `restaurants` (cha) và `items` (con):

- Truy vấn liên kết bằng **`$lookup`** (mongosh + Spring `MongoTemplate`/`Aggregation`)
- Trả kết quả bằng **DTO** (không `HashMap`)
- Cập nhật nhiều collection bằng **Transaction** + tối ưu **`updateMulti`**
- Giao diện **Thymeleaf** quản lý nhà hàng & món ăn

## Yêu cầu

- JDK 17+
- MongoDB đang chạy ở `localhost:27017`
- **Transaction (đổi ID):** MongoDB phải bật **Replica Set** (xem §8.2 syllabus)

## Chạy project

```bash
cd demo-bai5-mongodb-spring/java-springboot-bai5
./mvnw spring-boot:run
```

Mở: **http://localhost:8080/restaurants**

## Nạp dữ liệu mẫu

**Cách 1 — Tự động:** khi cả 2 collection `restaurants` và `items` đều rỗng, `DataSeeder` nạp 3 nhà hàng + 7 món.

**Cách 2 — mongosh:** chạy script [`01-create-sample-data.mongodb`](01-create-sample-data.mongodb).

```bash
mongosh db_java_t3h_module3 < 01-create-sample-data.mongodb
```

> Nếu đã có dữ liệu Bài 4 trong `restaurants` nhưng chưa có `items`, xóa collection cũ hoặc chạy script thủ công:
> `db.restaurants.deleteMany({})` và `db.items.deleteMany({})` rồi khởi động lại app.

## Replica Set (cho Transaction)

```bash
mongod --dbpath /your/data/path --replSet rs0
# trong mongosh (1 lần):
rs.initiate()
```

Đổi `application.properties`:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/db_java_t3h_module3?replicaSet=rs0
```

## REST API

| Method | URL | Mô tả |
|--------|-----|-------|
| GET | `/api/restaurants/{id}/with-items` | Nhà hàng kèm món (`$lookup`) |
| GET | `/api/restaurants/{id}/items-with-restaurant` | Món kèm thông tin nhà hàng |
| GET | `/api/items/by-category?restaurantId=&category=` | Lọc món theo loại |
| PUT | `/api/restaurants/change-id?oldId=&newId=&mode=optimized` | Đổi ID (transaction) |

Ví dụ:

```bash
curl http://localhost:8080/api/restaurants/30075445/with-items
curl "http://localhost:8080/api/items/by-category?restaurantId=30075445&category=Main"
```

## Giao diện Thymeleaf

| Màn hình | URL |
|----------|-----|
| Danh sách nhà hàng | `/restaurants` |
| Chi tiết + bảng món | `/restaurants/{restaurant_id}` |
| Thêm món | `/restaurants/{id}/items/new` |
| Sửa món | `/items/{itemId}/edit` |
| Đổi ID (transaction) | Form trên trang chi tiết |

## Cấu trúc

```
src/main/java/vn/demo/
├── DemoBai5MongoApplication.java
├── config/{MongoConfig,DataSeeder}.java
├── model/{RestaurantModel,ItemModel}.java
├── dto/{RestaurantWithItemsDto,ItemWithRestaurantDto,ItemFormDto}.java
├── repository/{RestaurantRepository,ItemRepository,ItemRepositoryCustom,ItemRepositoryImpl}.java
├── service/{RestaurantService,ItemService}.java
├── exception/ResourceNotFoundException.java
└── controller/
    ├── api/{RestaurantRestController,ItemRestController,RestExceptionHandler}.java
    └── web/{HomeController,RestaurantViewController,ItemViewController}.java

src/main/resources/
├── application.properties
├── static/css/restaurants.css
└── templates/
    ├── fragments/layout.html
    ├── restaurants/{list,detail,not-found}.html
    └── items/form.html
```

## Liên kết

- Syllabus: [`java_m3_bai5_Relationship_in_MongoDB.md`](../syllabus/module-3/java_m3_bai5_Relationship_in_MongoDB.md)
- Bài 3: [`demo-bai3-mongodb-spring`](../demo-bai3-mongodb-spring)
- Bài 4: [`demo-bai4-mongodb-spring`](../demo-bai4-mongodb-spring)
