# Demo Bài 8 — Unit / Slice Testing với Spring Boot

Project demo cho syllabus [`java_m3_bai8_Unit_Testing.md`](../syllabus/module-3/java_m3_bai8_Unit_Testing.md).

App **gọn** (REST Movie, không Thymeleaf) để học viên tập trung 3 lớp test:

| Lớp | Class | Syllabus | Cần Mongo? |
|-----|-------|----------|------------|
| **Unit Service** ★ | `MovieServiceTest` | §4 | Không |
| **Slice Repository** | `MovieRepositoryTest` | §5 | Có (`testdb`) |
| **Slice Controller** ★ | `MovieRestControllerTest` | §6 | Không |
| Nâng cao (đọc, `@Disabled`) | `advanced/*OverviewTest` | §9 | — |

> **Không có** unit test Model (anemic) — đúng chiến lược syllabus §2.2.

## Yêu cầu

- JDK 17+
- MongoDB ở `localhost:27017` (**bắt buộc** khi chạy `MovieRepositoryTest` hoặc `spring-boot:run`)
- Máy lab module-3 thường **bật auth** — URI mặc định trong `application*.properties` đã có user `root` (giống demo bài 4/6/7). Nếu Mongo không auth: bỏ comment dòng URI không password.

## Chạy test

```bash
cd demo-bai8-unit-testing/java-springboot-bai8

# Toàn bộ (cần Mongo cho @DataMongoTest)
./mvnw test

# Chỉ unit Service — không cần Mongo
./mvnw test -Dtest=MovieServiceTest

# Chỉ MockMvc
./mvnw test -Dtest=MovieRestControllerTest

# Chỉ Repository slice
./mvnw test -Dtest=MovieRepositoryTest
```

## Chạy app (gọi API bằng tay)

```bash
./mvnw spring-boot:run
```

| Method | URL | Mô tả |
|--------|-----|-------|
| GET | `/api/movies` | Tất cả phim |
| GET | `/api/movies/highly-rated?min=8.0` | Rating ≥ min (lab MockMvc) |
| GET | `/api/movies/search?keyword=incept` | Tìm title |
| GET | `/api/movies/{id}` | Chi tiết / 404 |
| POST | `/api/movies` | Tạo mới (201) / thiếu title → 400 |
| PUT | `/api/movies/{id}` | Cập nhật (200) / 404 |
| DELETE | `/api/movies/{id}` | Xóa (204) / 404 |

```bash
curl "http://localhost:8080/api/movies/highly-rated?min=8.0"
```

Khi collection `movies` rỗng, `DataSeeder` nạp 4 phim mẫu. DB app: `db_java_t3h_module3`.  
DB test (`application-test.properties`): **`testdb`**.

## Cấu trúc

```
src/main/java/vn/demo/
├── DemoBai8UnitTestingApplication.java
├── config/DataSeeder.java
├── model/MovieModel.java              ← anemic — không unit test
├── repository/MovieRepository.java
├── service/MovieService.java          ← Mockito test ★
├── dto/MovieDto.java
├── exception/ResourceNotFoundException.java
└── controller/api/
    ├── MovieRestController.java       ← MockMvc test ★
    └── RestExceptionHandler.java

src/test/java/vn/demo/
├── service/MovieServiceTest.java
├── repository/MovieRepositoryTest.java
├── controller/MovieRestControllerTest.java
└── advanced/                          ← §9 đọc hiểu (@Disabled)
    ├── TestcontainersOverviewTest.java
    └── SpringBootTestOverviewTest.java

src/test/resources/
└── application-test.properties        ← mongodb://…/testdb
```

## Map syllabus → code

| Syllabus | File demo |
|----------|-----------|
| §4 Mockito + `@CsvSource` / `@MethodSource` | `MovieServiceTest` (`isValidMinRating` + `findHighlyRated`) |
| §5 `@DataMongoTest` | `MovieRepositoryTest` + `application-test.properties` |
| §6 `@WebMvcTest` + MockMvc | `MovieRestControllerTest` (GET/POST/PUT/DELETE) |
| §6 case 404 / 400 | GET/PUT/DELETE 404; POST `@Valid` 400 |
| §9 Testcontainers / `@SpringBootTest` | `advanced/*` (javadoc, disabled) |
| §10 Postman / JMeter | Chỉ đọc syllabus — không lab trong repo |

## Ghi chú kỹ thuật

- Endpoint `/highly-rated` khai báo **trước** `/{id}` để tránh path variable nuốt chuỗi.
- Controller test dùng **`@MockitoBean`** (Spring Boot 3.4+; thay `@MockBean` deprecated). Syllabus có ghi chú tương đương.
- Class `advanced/*` mang mục đích **tài liệu sống** — không làm fail `mvn test`.
