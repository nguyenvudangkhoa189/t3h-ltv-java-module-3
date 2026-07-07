# Demo Bài 6 — Tối ưu truy vấn MongoDB với Spring Boot

Project demo cho syllabus [`java_m3_bai6_Query_Optimization.md`](../syllabus/module-3/java_m3_bai6_Query_Optimization.md).

Minh họa các kỹ thuật tối ưu cơ bản trên collection `restaurants` + `items`:

- **Index** (`@Indexed`) + **text index** cho `$text`
- **Projection** (`@Query` fields)
- **Cursor pagination** — `CursorPageDto<T>` generic (syllabus §7.0), không trả `List<RestaurantModel>` ra API
- **Regex prefix** và **`$text` search**
- **`updateMulti`** + **`BulkOperations`**
- Tránh **N+1** (`findByRestaurantIdIn`)
- **Aggregation** (`$match` sớm + `$lookup` + `$project`)
- Lab **`explain`** trong `mongosh`

## Yêu cầu

- JDK 17+
- MongoDB đang chạy ở `localhost:27017`

## Chạy project

```bash
cd demo-bai6-query-optimization/java-springboot-bai6
./mvnw spring-boot:run
```

Mở: **http://localhost:8080/**

## Nạp dữ liệu mẫu

**Cách 1 — Tự động:** khi cả 2 collection `restaurants` và `items` đều rỗng, `DataSeeder` nạp 12 nhà hàng + 16 món. `IndexConfig` tạo text index cho `$text`.

**Cách 2 — mongosh:**

```bash
mongosh db_java_t3h_module3 < 01-create-sample-data.mongodb
```

> Nếu đang dùng dữ liệu Bài 5 (chỉ 3 nhà hàng), xóa 2 collection rồi seed lại để demo feed rõ hơn:
> `db.restaurants.deleteMany({})` và `db.items.deleteMany({})` rồi khởi động lại app.

## Lab `explain` (syllabus §3)

```bash
mongosh db_java_t3h_module3 < 02-explain-lab.mongodb
```

Quan sát `COLLSCAN` vs `IXSCAN`, `totalDocsExamined` vs `nReturned`.

## REST API

| Method | URL | Syllabus | Mô tả |
|--------|-----|----------|--------|
| GET | `/api/restaurants/summary` | §4.2, §10.1 | Projection: name, borough, restaurant_id |
| GET | `/api/restaurants/feed?limit=5&afterId=` | §7, §10.2 | Cursor pagination |
| GET | `/api/restaurants/search?prefix=Mor` | §6, §10.3 | Regex prefix `^Mor` |
| GET | `/api/restaurants/search/text?q=chinese` | §4.3 | Full-text `$text` |
| GET | `/api/restaurants/with-items?mode=optimized` | §8 | `findByRestaurantIdIn` |
| GET | `/api/restaurants/with-items?mode=n1` | §8 | N+1 (so sánh) |
| POST | `/api/restaurants/bulk-demo` | §5.2 | `BulkOperations` |
| PUT | `/api/items/reassign?oldId=&newId=` | Bài 5 §9 / Bài 6 §4.4 | `updateMulti` (tóm tắt) |
| GET | `/api/items/with-restaurant?category=Main` | §9 | Aggregation tối ưu |
| GET | `/api/items?restaurantId=` | §10.5 | `ItemSummaryDto` — kiểm tra sau reassign |

### Ví dụ `curl`

```bash
# Projection
curl http://localhost:8080/api/restaurants/summary

# Cursor feed — trang 1
curl "http://localhost:8080/api/restaurants/feed?limit=5"
# Trang 2: lấy lastSeenId từ response trên
curl "http://localhost:8080/api/restaurants/feed?limit=5&afterId=<lastSeenId>"

# Prefix search
curl "http://localhost:8080/api/restaurants/search?prefix=Mor"

# Text search
curl "http://localhost:8080/api/restaurants/search/text?q=chinese"

# N+1 vs optimized
curl "http://localhost:8080/api/restaurants/with-items?mode=n1"
curl "http://localhost:8080/api/restaurants/with-items?mode=optimized"

# Bulk demo (đổi Wild East borough + Bakery → Pastry Shop)
curl -X POST http://localhost:8080/api/restaurants/bulk-demo

# updateMulti — đổi restaurant_id của items
curl -X PUT "http://localhost:8080/api/items/reassign?oldId=30075445&newId=777888"
curl "http://localhost:8080/api/items?restaurantId=777888"

# Aggregation
curl "http://localhost:8080/api/items/with-restaurant?category=Main"
```

## Cấu trúc

```
src/main/java/vn/demo/
├── DemoBai6MongoApplication.java
├── config/{IndexConfig,DataSeeder}.java
├── model/{RestaurantModel,ItemModel}.java
├── dto/{RestaurantSummaryDto,ItemSummaryDto,CursorPageDto,RestaurantWithItemsDto,
│        ItemWithRestaurantDto,BulkUpdateResultDto}.java
├── repository/{RestaurantRepository,ItemRepository}.java
├── service/{RestaurantQueryService,ItemQueryService}.java
├── exception/ResourceNotFoundException.java
└── controller/api/{HomeController,RestaurantQueryController,
                    ItemQueryController,RestExceptionHandler}.java
```

## Liên kết

- Syllabus: [`java_m3_bai6_Query_Optimization.md`](../syllabus/module-3/java_m3_bai6_Query_Optimization.md)
- Bài 5: [`demo-bai5-mongodb-spring`](../demo-bai5-mongodb-spring)
- Bài 4: [`demo-bai4-mongodb-spring`](../demo-bai4-mongodb-spring)
