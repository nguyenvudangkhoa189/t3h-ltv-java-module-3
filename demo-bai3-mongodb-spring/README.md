# Demo Bài 3 — Spring Boot & MongoDB (CRUD qua REST API + Thymeleaf)

Project demo cho syllabus `java_m3_bai3_MongoDB_Spring_1.md`.

Bài này gồm **2 phần dùng chung 1 project** (cùng `MovieService` + `MovieRepository`):

- **Phần 1 — REST API** (`@RestController`, trả JSON): `MovieRestController` tại `/api/movies`
- **Phần 2 — Bài tập Thymeleaf** (`@Controller`, trả view): `MovieViewController` tại `/movies`

Mục tiêu: cùng một nghiệp vụ CRUD được thể hiện qua **API JSON** và **trang web**, giúp học viên
phân biệt rõ `@RestController` vs `@Controller`, và thấy lợi ích của việc **tách tầng**
(chỉ thay tầng trình bày, không viết lại Service/Repository).

## Yêu cầu

- JDK 17+
- MongoDB đang chạy ở `localhost:27017` (xem `application.properties`)

## Chạy project

```bash
cd demo-bai3-mongodb-spring/java-springboot-bai3
./mvnw spring-boot:run
```

- Giao diện web: **http://localhost:8080/movies**
- REST API: **http://localhost:8080/api/movies**

> Khi collection `movies` rỗng, app tự nạp 8 phim mẫu (`DataSeeder`). Tắt bằng
> `app.movies.seed-on-startup=false`.

## Phần 1 — REST API (`/api/movies`)

| Method | URL | Mô tả |
|--------|-----|-------|
| GET | `/api/movies` | Lấy tất cả phim |
| GET | `/api/movies/{id}` | Lấy 1 phim (404 nếu không có) |
| GET | `/api/movies/search?keyword=incept` | Tìm theo từ khóa title |
| GET | `/api/movies/good?rating=7&year=2015` | Lọc rating ≥ & năm ≥ |
| POST | `/api/movies` | Tạo mới (201 + object) |
| PUT | `/api/movies/{id}` | Cập nhật partial |
| DELETE | `/api/movies/{id}` | Xóa (204) |

Ví dụ test bằng `curl`:

```bash
curl http://localhost:8080/api/movies
curl -X POST http://localhost:8080/api/movies \
  -H "Content-Type: application/json" \
  -d '{"title":"Tenet","year":2020,"genre":["Action","Sci-Fi"],"director":"Christopher Nolan","rating":7.4}'
```

## Phần 2 — Thymeleaf (`/movies`)

| Màn hình | URL | Method |
|----------|-----|--------|
| Danh sách + tìm kiếm + phân trang | `/movies?keyword=&page=` | GET |
| Form tạo | `/movies/new` | GET |
| Tạo mới | `/movies` | POST |
| Chi tiết | `/movies/{id}` | GET |
| Form sửa | `/movies/{id}/edit` | GET |
| Cập nhật | `/movies/{id}` | POST |
| Xóa | `/movies/{id}/delete` | POST |

## Dependencies

| Dependency | Mục đích |
|------------|----------|
| `spring-boot-starter-web` | REST API + MVC |
| `spring-boot-starter-data-mongodb` | `MongoRepository`, `@Document` |
| `spring-boot-starter-thymeleaf` | Template HTML (Phần 2) |
| `spring-boot-starter-validation` | `@Valid`, `@NotBlank`, `@Min` |
| `lombok` | `@Getter/@Setter`, `@RequiredArgsConstructor`, `@Slf4j` |

## Cấu trúc

```
src/main/java/vn/demo/
├── DemoBai3MongoApplication.java
├── config/DataSeeder.java                   ← nạp phim mẫu khi rỗng
├── model/MovieModel.java                    ← @Document("movies"), @Id, @Indexed
├── repository/MovieRepository.java          ← MongoRepository + derived queries
├── service/MovieService.java                ← nghiệp vụ dùng chung 2 phần
├── exception/ResourceNotFoundException.java
├── dto/MovieFormDto.java                    ← form Thymeleaf (genre dạng text)
└── controller/
    ├── api/                                 ← PHẦN 1: REST API (trả JSON)
    │   ├── MovieRestController.java         ←   @RestController /api/movies
    │   └── RestExceptionHandler.java        ←   @RestControllerAdvice (404/400)
    └── web/                                 ← PHẦN 2: Thymeleaf (trả view)
        ├── HomeController.java              ←   / → /movies
        └── MovieViewController.java         ←   @Controller /movies

src/main/resources/
├── application.properties
├── static/css/movies.css
└── templates/
    ├── fragments/layout.html
    └── movies/{list,form,detail,not-found}.html
```

## Liên kết

- Syllabus: [`syllabus/module-3/java_m3_bai3_MongoDB_Spring_1.md`](../syllabus/module-3/java_m3_bai3_MongoDB_Spring_1.md)
- Bài 2 (MongoDB shell): [`demo-bai2-mongodb`](../demo-bai2-mongodb)
