# Bài 3: Spring Boot & MongoDB (1) — CRUD qua REST API + Thymeleaf

## Mục tiêu bài học

Sau bài này, học viên có thể:

- Khởi tạo dự án **Spring Boot** có kết nối **MongoDB** bằng Spring Initializr
- Khai báo kết nối MongoDB qua `application.properties` (`spring.data.mongodb.uri`)
- Xây dựng kiến trúc phân tầng **Controller → Service → Repository → MongoDB**
- Tạo **model** ánh xạ collection bằng `@Document`, `@Id`, `@Indexed`
- Dùng `MongoRepository` và **derived query method** để truy vấn không cần viết code
- **PHẦN 1:** Làm CRUD đầy đủ qua **REST API** (`@RestController`, trả JSON): Create / Read / Update / Delete + tìm kiếm
- Xử lý lỗi tập trung bằng `@RestControllerAdvice` + custom exception, validate bằng `@Valid`
- **PHẦN 2 (bài tập):** Làm lại đúng các chức năng đó bằng **giao diện Thymeleaf** (`@Controller`, trả view) — dùng chung Service/Repository
- Phân biệt rõ **`@RestController`** (JSON) và **`@Controller`** (HTML view)

## Điều kiện tiên quyết

- Đã học **[Bài 2 — NoSQL & MongoDB](./java_m3_bai2_NoSQL_MongoDB.md)**: tư duy document, CRUD bằng `mongosh`, `_id`/`ObjectId`
- Đã học **Spring Boot MVC + Thymeleaf** ở Module 2 (Bài 4, 6, 8): `@Controller`, `Model`, `th:each`, `th:field`, `th:errors`, `@Valid`, PRG
- Máy đã cài và **đang chạy MongoDB** ở `localhost:27017`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

> **Ghi chú:** Bài này dùng ví dụ collection **`movies`**. Demo nạp sẵn 8 phim mẫu khi
> collection rỗng nên học viên mở app là có dữ liệu ngay. Các chủ đề **thiết kế dữ liệu
> NoSQL (embedded/reference)** sẽ học ở **Bài 5**; **Aggregation, `@Query`, `MongoTemplate`,
> `Criteria`** sẽ học ở **Bài 6**.

### Thời lượng gợi ý

| Phần | Thời gian |
|------|-----------|
| Khởi tạo & kết nối MongoDB §1–3 | ~20 phút |
| PHẦN 1 — CRUD REST API §4–8 (có demo) | ~45 phút |
| PHẦN 2 — Bài tập Thymeleaf §9 | ~35 phút |
| Lỗi thường gặp + bài tập | ~20 phút |

## Nội dung

| # | Chủ đề |
|---|--------|
| 1 | Quy trình tạo dự án Spring Boot + MongoDB |
| 2 | Kết nối MongoDB & `application.properties` |
| 3 | Kiến trúc phân tầng + luồng xử lý request |
| 4 | Model `MovieModel` & các annotation |
| 5 | Repository & derived query method |
| 6 | Service — tầng nghiệp vụ dùng chung |
| **7** | **PHẦN 1 — CRUD qua REST API (`@RestController`)** |
| 8 | Xử lý lỗi tập trung + validation |
| **9** | **PHẦN 2 — BÀI TẬP: làm lại bằng Thymeleaf (`@Controller`)** |
| 10 | Lỗi thường gặp |
| Phụ lục | Bài tập mở rộng · Checklist · Liên kết |

---

## 1. Quy trình tạo dự án Spring Boot + MongoDB

```mermaid
flowchart TD
    A["Khởi tạo project bằng Spring Initializr"] --> B["Khởi động MongoDB server"]
    B --> C["Khai báo kết nối trong application.properties"]
    C --> D["Tạo Model (@Document)"]
    D --> E["Tạo Repository (MongoRepository)"]
    E --> F["Tạo Service (nghiệp vụ)"]
    F --> G["Tạo Controller (REST API hoặc Thymeleaf)"]
    G --> H["Khởi động Spring Boot & kiểm tra"]
```

**Chọn dependency trên [Spring Initializr](https://start.spring.io):**

| Dependency | Vai trò |
|------------|---------|
| Spring Web | REST API + MVC |
| Spring Data MongoDB | `MongoRepository`, `@Document` |
| Thymeleaf | Render HTML (Phần 2) |
| Validation | `@Valid`, `@NotBlank`, `@Min` |
| Lombok | Sinh getter/setter, logger |

---

## 2. Kết nối MongoDB & `application.properties`

> **Lưu ý:** Luôn **khởi động MongoDB server** trước khi chạy ứng dụng.

```properties
# Khai báo đường dẫn MongoDB: mongodb://<host>:<port>/<tên database>
spring.data.mongodb.uri=mongodb://localhost:27017/db_java_t3h_module3

# Phân trang danh sách movie (dùng cho Phần 2 Thymeleaf)
app.movies.page-size=5
```

- Nếu database/collection chưa tồn tại, MongoDB sẽ **tự tạo** khi ghi document đầu tiên.
- Có thể tách rời bằng `spring.data.mongodb.host` / `port` / `database` thay cho `uri`.
- Khi có user/password: `mongodb://user:pass@host:27017/db?authSource=admin`.

> **Xem cấu hình đầy đủ:** [`application.properties`](../../demo-bai3-mongodb-spring/java-springboot-bai3/src/main/resources/application.properties)

---

## 3. Kiến trúc phân tầng + luồng xử lý request

### 3.0. Cấu trúc package (gợi ý)

Project tách package theo **vai trò từng tầng**. Riêng tầng Controller chia thêm
`api` (REST — Phần 1) và `web` (Thymeleaf — Phần 2) để người mới **nhìn là biết** phần nào
trả JSON, phần nào trả HTML.

```
src/main/java/vn/demo/
├── DemoBai3MongoApplication.java        ← điểm khởi động Spring Boot
├── config/
│   └── DataSeeder.java                  ← nạp dữ liệu mẫu khi collection rỗng
├── model/
│   └── MovieModel.java                  ← MODEL: ánh xạ collection "movies"
├── repository/
│   └── MovieRepository.java             ← REPOSITORY: truy cập MongoDB
├── service/
│   └── MovieService.java                ← SERVICE: nghiệp vụ (dùng chung 2 phần)
├── dto/
│   └── MovieFormDto.java                ← DTO: dữ liệu form cho Thymeleaf
├── exception/
│   └── ResourceNotFoundException.java   ← lỗi nghiệp vụ "không tìm thấy"
└── controller/
    ├── api/                             ← PHẦN 1 — REST API (trả JSON)
    │   ├── MovieRestController.java
    │   └── RestExceptionHandler.java
    └── web/                             ← PHẦN 2 — Thymeleaf (trả view HTML)
        ├── HomeController.java
        └── MovieViewController.java

src/main/resources/
├── application.properties
├── static/css/movies.css
└── templates/
    ├── fragments/layout.html
    └── movies/{list,form,detail,not-found}.html
```

### 3.1. Nhiệm vụ cụ thể của từng tầng

| Tầng | Lớp trong bài | Nhiệm vụ cụ thể | Điều **không** nên làm |
|------|---------------|-----------------|------------------------|
| **Model (Entity)** | `MovieModel` | Mô tả cấu trúc 1 document trong collection; ánh xạ field ↔ thuộc tính | Không chứa logic nghiệp vụ |
| **Repository** | `MovieRepository` | Truy cập MongoDB; khai báo derived query (Spring tự sinh code) | Không xử lý nghiệp vụ |
| **Service** | `MovieService` | Chứa logic nghiệp vụ: kiểm tra tồn tại, partial update, ném lỗi 404... | Không phụ thuộc HTTP (request/response) |
| **DTO** | `MovieFormDto` | Đối tượng trung gian nhận/gửi dữ liệu với form; chuyển đổi qua lại entity | Không lưu trực tiếp xuống DB |
| **Controller (api)** | `MovieRestController` | Nhận HTTP request, gọi Service, trả **JSON** + status code | Không viết logic nghiệp vụ |
| **Controller (web)** | `MovieViewController` | Nhận request, gọi Service, đẩy dữ liệu vào `Model`, trả **tên view** | Không viết logic nghiệp vụ |
| **Exception handler** | `RestExceptionHandler` | Bắt lỗi tập trung cho REST → map sang status code | — |
| **Config** | `DataSeeder` | Nạp dữ liệu mẫu lúc khởi động | — |

> **Quy tắc vàng:** request đi **một chiều** xuống: `Controller → Service → Repository → MongoDB`,
> rồi dữ liệu đi ngược lên. Controller **không** gọi thẳng Repository, Service **không** biết gì
> về HTTP.

### 3.2. Luồng một request

```mermaid
sequenceDiagram
    participant Client as Client (Browser/Postman)
    participant C as Controller
    participant S as MovieService
    participant R as MovieRepository
    participant DB as MongoDB

    Client->>C: HTTP request (yêu cầu + tham số)
    C->>S: gọi hàm nghiệp vụ
    S->>R: gọi hàm truy vấn
    R->>DB: thực thi trên collection movies
    DB-->>R: documents
    R-->>S: List<MovieModel> / MovieModel
    S-->>C: dữ liệu
    C-->>Client: JSON (REST) hoặc HTML (Thymeleaf)
```

> **Điểm cốt lõi của bài:** cả REST API (Phần 1) và Thymeleaf (Phần 2) **dùng chung**
> `MovieService` + `MovieRepository`. Chỉ tầng Controller (và View) khác nhau → minh họa
> lợi ích của tách tầng.

---

## 4. Model `MovieModel` & các annotation

```java
@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
@Document(collection = "movies")   // ánh xạ tới collection "movies"
public class MovieModel {

    @Id                            // khóa chính "_id" của MongoDB
    private String id;

    @NotBlank
    @Indexed                       // tạo index để tối ưu tìm kiếm theo title
    private String title;

    @Min(1888)
    private Integer year;

    private List<String> genre;
    private String director;

    @Min(0)
    private Double rating;
}
```

| Annotation | Ý nghĩa |
|------------|---------|
| `@Document(collection="movies")` | Ánh xạ class tới collection; mỗi thuộc tính ↔ một field |
| `@Id` | Khai báo khóa chính. Nếu thiếu, MongoDB tự sinh `_id` (`ObjectId`) |
| `@Indexed` | Tạo **index** trên field → truy vấn theo field đó nhanh hơn |
| `@Getter/@Setter/@ToString` | Lombok sinh getter/setter/toString |

> **Lưu ý Lombok:** Dùng `@Getter/@Setter` thay cho `@Data` trên entity để tránh
> `equals()/hashCode()` mặc định gây lỗi tinh vi khi document có cấu trúc lồng nhau.
>
> **`_id` (String) vs `ObjectId`:** Spring Data ánh xạ `ObjectId` của MongoDB sang `String`
> trong Java cho tiện hiển thị/truyền qua URL.

> **Xem code đầy đủ:** [`model/MovieModel.java`](../../demo-bai3-mongodb-spring/java-springboot-bai3/src/main/java/vn/demo/model/MovieModel.java)

---

## 5. Repository & derived query method

`MongoRepository<MovieModel, String>` đã có sẵn `findAll()`, `findById()`, `save()`, `saveAll()`,
`delete()`, `count()`, phân trang... **Không cần khai báo lại**. Chỉ cần thêm các
**derived query** — Spring tự sinh truy vấn theo **tên hàm**.

```java
@Repository
public interface MovieRepository extends MongoRepository<MovieModel, String> {

    Optional<MovieModel> findByTitle(String title);
    List<MovieModel> findByDirector(String director);
    List<MovieModel> findByYearAndTitle(Integer year, String title);
    List<MovieModel> findByRatingGreaterThan(Double rating);
    List<MovieModel> findByYearBetween(Integer startYear, Integer endYear);
    List<MovieModel> findByTitleContainingIgnoreCase(String keyword);

    // bài tập: rating >= ... và year >= ...
    List<MovieModel> findByRatingGreaterThanEqualAndYearGreaterThanEqual(Double rating, Integer year);

    // phiên bản phân trang cho Phần 2
    Page<MovieModel> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}
```

| Từ khóa trong tên hàm | Ý nghĩa |
|-----------------------|---------|
| `findBy<Field>` | Lọc theo field |
| `And` / `Or` | Ghép nhiều điều kiện |
| `GreaterThan` / `LessThanEqual` / `Between` | So sánh số |
| `Containing` + `IgnoreCase` | Tìm chuỗi con, không phân biệt hoa thường |

> Tham khảo: [Spring Data MongoDB — Query Methods](https://docs.spring.io/spring-data/mongodb/reference/repositories/query-methods-details.html)
>
> **Xem code đầy đủ:** [`repository/MovieRepository.java`](../../demo-bai3-mongodb-spring/java-springboot-bai3/src/main/java/vn/demo/repository/MovieRepository.java)

---

## 6. Service — tầng nghiệp vụ dùng chung

`MovieService` là nơi tập trung nghiệp vụ; **cả REST API lẫn Thymeleaf đều gọi tới đây**.

| Hàm | Mục đích |
|-----|----------|
| `getAllMovies()` | Lấy tất cả phim |
| `findPage(keyword, pageable)` | 1 trang phim có tìm kiếm (dùng cho Thymeleaf) |
| `getById(id)` | Lấy 1 phim; ném `ResourceNotFoundException` nếu không có |
| `getByTitle(title)` | Lấy 1 phim theo title; ném 404 nếu không có |
| `searchByKeyword(keyword)` | Tìm theo từ khóa title |
| `findGoodMovies(rating, year)` | Bài tập: rating ≥ & năm ≥ |
| `create(movie)` | Tạo mới (đặt `id=null` để chắc chắn là insert) |
| `update(id, params)` | Cập nhật **partial** — chỉ field có giá trị |
| `delete(id)` | Xóa theo id |

```java
public MovieModel getById(String id) {
    return movieRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim với id: " + id));
}
```

> Dùng `Optional` + `orElseThrow` thay cho việc trả `null` rồi kiểm tra `== null` — an toàn và rõ nghĩa hơn.
>
> **Xem code đầy đủ:** [`service/MovieService.java`](../../demo-bai3-mongodb-spring/java-springboot-bai3/src/main/java/vn/demo/service/MovieService.java)

---

## 7. PHẦN 1 — CRUD qua REST API (`@RestController`)

> **Mục tiêu Phần 1:** học viên thao tác dữ liệu MongoDB qua **REST API trả JSON**, test bằng
> trình duyệt / Postman / `curl`.

### 7.1. `@RestController` là gì?

`@RestController` = `@Controller` + `@ResponseBody`. Giá trị mỗi handler trả về được Spring
**chuyển thành JSON** ghi thẳng vào response (không tìm template).

```java
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieRestController {

    private final MovieService movieService;
    // ...
}
```

### 7.2. Các endpoint

| Method | URL | Mục đích | HTTP status |
|--------|-----|----------|-------------|
| GET | `/api/movies` | Lấy tất cả | 200 |
| GET | `/api/movies/{id}` | Lấy 1 phim | 200 / 404 |
| GET | `/api/movies/search?keyword=` | Tìm theo title | 200 |
| GET | `/api/movies/good?rating=7&year=2015` | Lọc rating & năm | 200 |
| POST | `/api/movies` | Tạo mới | 201 |
| PUT | `/api/movies/{id}` | Cập nhật | 200 / 404 |
| DELETE | `/api/movies/{id}` | Xóa | 204 / 404 |

### 7.3. Read — tìm kiếm

```java
@GetMapping("/search")
public ResponseEntity<List<MovieModel>> search(@RequestParam String keyword) {
    return ResponseEntity.ok(movieService.searchByKeyword(keyword));
}
```

Test: `GET http://localhost:8080/api/movies/search?keyword=incept`

### 7.4. Create

```java
@PostMapping
public ResponseEntity<MovieModel> create(@Valid @RequestBody MovieModel movie) {
    MovieModel created = movieService.create(movie);
    return new ResponseEntity<>(created, HttpStatus.CREATED);   // 201
}
```

```bash
curl -X POST http://localhost:8080/api/movies \
  -H "Content-Type: application/json" \
  -d '{"title":"Tenet","year":2020,"genre":["Action"],"director":"Christopher Nolan","rating":7.4}'
```

### 7.5. Update (partial) & Delete

```java
@PutMapping("/{id}")
public ResponseEntity<MovieModel> update(@PathVariable String id, @RequestBody MovieModel movie) {
    return ResponseEntity.ok(movieService.update(id, movie));
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable String id) {
    movieService.delete(id);
    return ResponseEntity.noContent().build();   // 204
}
```

> Điều kiện update/delete không bắt buộc là `id` — có thể gộp nhiều điều kiện khác qua Service.
>
> **Xem code đầy đủ:** [`controller/api/MovieRestController.java`](../../demo-bai3-mongodb-spring/java-springboot-bai3/src/main/java/vn/demo/controller/api/MovieRestController.java)

---

## 8. Xử lý lỗi tập trung + validation

Thay vì `try/catch` rải rác trong từng handler, ta bắt lỗi tại **một nơi** bằng
`@RestControllerAdvice`:

```java
@RestControllerAdvice(basePackageClasses = MovieRestController.class)
public class RestExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<...> handleNotFound(...) { /* 404 */ }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<...> handleValidation(...) { /* 400 + chi tiết field */ }
}
```

| Tình huống | Kết quả |
|------------|---------|
| `getById(id)` không thấy → `ResourceNotFoundException` | **404 Not Found** |
| POST thiếu `title` (vi phạm `@NotBlank`) | **400 Bad Request** + danh sách field lỗi |

> **Xem code đầy đủ:** [`exception/ResourceNotFoundException.java`](../../demo-bai3-mongodb-spring/java-springboot-bai3/src/main/java/vn/demo/exception/ResourceNotFoundException.java) ·
> [`controller/api/RestExceptionHandler.java`](../../demo-bai3-mongodb-spring/java-springboot-bai3/src/main/java/vn/demo/controller/api/RestExceptionHandler.java)

---

## 9. PHẦN 2 — BÀI TẬP: làm lại bằng Thymeleaf (`@Controller`)

> **Đề bài:** Dựng **giao diện web** quản lý phim với **đúng các chức năng** đã làm ở Phần 1
> (list + tìm kiếm + phân trang, tạo, xem, sửa, xóa) — nhưng dùng **`@Controller` + Thymeleaf**
> thay vì REST API. **Tái sử dụng** `MovieService` và `MovieRepository` của Phần 1.

### 9.1. `@RestController` vs `@Controller`

| | `@RestController` (Phần 1) | `@Controller` (Phần 2) |
|--|---------------------------|------------------------|
| Trả về | **JSON** (qua `@ResponseBody`) | **Tên template** → HTML |
| Ví dụ return | `ResponseEntity.ok(movie)` | `return "movies/list";` |
| Dữ liệu cho view | — | `model.addAttribute(...)` |
| Dùng khi | API cho FE/mobile/khác | Trang web server-render |

> **Lỗi kinh điển:** dùng `@RestController` rồi `return "movies/list"` → trình duyệt hiện ra
> đúng chữ `movies/list` (vì bị coi là chuỗi JSON), **không** render template.

### 9.2. Các màn hình (giống Phần 1 nhưng là web)

| Màn hình | URL | Method | View |
|----------|-----|--------|------|
| Danh sách + tìm kiếm + phân trang | `/movies?keyword=&page=` | GET | `movies/list` |
| Form tạo | `/movies/new` | GET | `movies/form` |
| Tạo mới | `/movies` | POST | redirect |
| Chi tiết | `/movies/{id}` | GET | `movies/detail` |
| Form sửa | `/movies/{id}/edit` | GET | `movies/form` |
| Cập nhật | `/movies/{id}` | POST | redirect |
| Xóa | `/movies/{id}/delete` | POST | redirect |

### 9.3. Controller trả view + phân trang

```java
@GetMapping
public String list(
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,    // page bắt đầu từ 0 (chuẩn Spring Data)
        Model model) {
    PageRequest pageRequest = PageRequest.of(Math.max(page, 0), pageSize, Sort.by("title").ascending());
    Page<MovieModel> moviePage = movieService.findPage(keyword, pageRequest);
    model.addAttribute("moviePage", moviePage);
    model.addAttribute("movies", moviePage.getContent());
    model.addAttribute("keyword", keyword == null ? "" : keyword);
    return "movies/list";
}
```

> **Phân trang 0-indexed:** `PageRequest.of(page, size)` đánh số trang **bắt đầu từ 0**.
> Với `size=5`: `page=0` → 5 phim đầu (hiển thị là "Trang 1"); `page=1` → 5 phim kế tiếp.
> Trong HTML ta hiển thị `page + 1` cho thân thiện người dùng.

### 9.4. Form + validation (PRG)

Phần 2 dùng DTO **`MovieFormDto`** (nhập `genre` dạng chuỗi `"Action, Sci-Fi"`) rồi chuyển qua
`MovieModel`. Sau khi tạo/sửa/xóa thành công → `redirect` theo mẫu **Post-Redirect-Get**.

```java
@PostMapping
public String create(@Valid @ModelAttribute("movieForm") MovieFormDto form,
                     BindingResult bindingResult, Model model,
                     RedirectAttributes ra) {
    if (bindingResult.hasErrors()) {
        model.addAttribute("isEdit", false);
        return "movies/form";          // có lỗi → render lại form + th:errors
    }
    MovieModel created = movieService.create(form.toEntity());
    ra.addFlashAttribute("message", "Tạo phim thành công!");
    return "redirect:/movies/" + created.getId();
}
```

> **Xem code đầy đủ:**
> [`controller/web/MovieViewController.java`](../../demo-bai3-mongodb-spring/java-springboot-bai3/src/main/java/vn/demo/controller/web/MovieViewController.java) ·
> [`controller/web/HomeController.java`](../../demo-bai3-mongodb-spring/java-springboot-bai3/src/main/java/vn/demo/controller/web/HomeController.java) ·
> [`dto/MovieFormDto.java`](../../demo-bai3-mongodb-spring/java-springboot-bai3/src/main/java/vn/demo/dto/MovieFormDto.java) ·
> [templates `movies/`](../../demo-bai3-mongodb-spring/java-springboot-bai3/src/main/resources/templates/movies)

### 9.5. Template danh sách (trích)

```html
<tr th:each="m : ${movies}">
    <td><a th:href="@{/movies/{id}(id=${m.id})}" th:text="${m.title}">Title</a></td>
    <td th:text="${m.year}">2010</td>
    <td th:text="${m.genre != null ? #strings.listJoin(m.genre, ', ') : ''}">Action</td>
    <td th:text="${m.rating}">8.8</td>
</tr>

<nav class="pagination" th:if="${moviePage.totalPages > 1}">
    <a th:each="p : ${#numbers.sequence(0, moviePage.totalPages - 1)}"
       th:href="@{/movies(page=${p}, keyword=${keyword})}"
       th:text="${p + 1}"
       th:classappend="${p == moviePage.number} ? ' active' : ''">1</a>
</nav>
```

---

## 10. Lỗi thường gặp

| Triệu chứng | Nguyên nhân | Cách xử lý |
|-------------|-------------|------------|
| App không khởi động, lỗi connect Mongo | Chưa chạy MongoDB server | Khởi động MongoDB ở `localhost:27017` |
| Trình duyệt in ra chữ `movies/list` | Dùng `@RestController` cho trang HTML | Đổi sang `@Controller` |
| API `/api/movies` trả HTML lỗi | Dùng `@Controller` mà quên `@ResponseBody` | Dùng `@RestController` |
| `_id` không sinh | Quên `@Id` | Thêm `@Id` cho field id |
| POST tạo nhưng trùng id | Gửi kèm `id` cũ | Service đặt `id=null` trước khi `save` |
| Validation không chạy | Thiếu `@Valid` | Thêm `@Valid` trước `@RequestBody`/`@ModelAttribute` |
| 404 không đẹp (stack trace) | Chưa có exception handler | Thêm `@RestControllerAdvice` / trang `not-found` |
| `th:*` không hoạt động | Thiếu `xmlns:th` | Thêm namespace vào `<html>` |
| Phân trang lệch số trang | Nhầm 0-indexed | `PageRequest.of` bắt đầu từ 0; hiển thị `page + 1` |

---

## Tóm tắt

| Khái niệm | Ý chính |
|-----------|---------|
| `@Document` / `@Id` / `@Indexed` | Ánh xạ collection, khóa chính, index |
| `MongoRepository` | CRUD + phân trang có sẵn |
| Derived query | Sinh truy vấn theo tên hàm |
| Service dùng chung | REST API và Thymeleaf chung nghiệp vụ |
| `@RestController` | Trả JSON (Phần 1) |
| `@Controller` | Trả tên view HTML (Phần 2) |
| `@RestControllerAdvice` | Bắt lỗi tập trung (404/400) |
| `Optional` + `orElseThrow` | Thay cho trả null |
| PRG | `redirect:` sau POST thành công |
| Phân trang | `PageRequest.of(page, size, Sort)` — 0-indexed |

---

## Phụ lục

### Bài tập

1. **Phần 1:** thêm endpoint `GET /api/movies/by-director?name=` trả các phim của 1 đạo diễn.
2. **Phần 1:** tạo trang/endpoint tìm tất cả phim có `rating > 7` và `year >= 2015`
   (dùng `findByRatingGreaterThanEqualAndYearGreaterThanEqual`).
3. **Phần 2 (chính):** hoàn thiện giao diện Thymeleaf đầy đủ list/search/pagination/create/detail/edit/delete.
4. **Mở rộng:** thêm sắp xếp theo `rating` (`Sort.by("rating").descending()`) trên trang danh sách.

### Checklist nộp bài

- [ ] Kết nối MongoDB thành công, có dữ liệu trong collection `movies`
- [ ] **Phần 1:** đủ CRUD REST API + tìm kiếm, test được bằng Postman/`curl`
- [ ] REST API trả đúng status code (201 khi tạo, 404 khi không thấy, 204 khi xóa)
- [ ] Có `@RestControllerAdvice` xử lý 404 + validation
- [ ] **Phần 2:** giao diện Thymeleaf đủ list/search/pagination/CRUD
- [ ] Dùng đúng `@Controller` cho view, `@RestController` cho API
- [ ] Validation hiển thị lỗi trên form (`th:errors`)
- [ ] PRG sau create/update/delete

### Liên kết tham khảo

- [Spring Data MongoDB Reference](https://docs.spring.io/spring-data/mongodb/reference/)
- [Query Methods](https://docs.spring.io/spring-data/mongodb/reference/repositories/query-methods-details.html)
- [Bài 2 — NoSQL & MongoDB](./java_m3_bai2_NoSQL_MongoDB.md)
- Demo: [`demo-bai3-mongodb-spring`](../../demo-bai3-mongodb-spring)
