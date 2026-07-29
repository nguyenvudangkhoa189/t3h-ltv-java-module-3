# Demo Bài 10 — Mini Project: Movie Portal + Admin Analytics

Project demo cho syllabus [`java_m3_bai10_Mini_Project.md`](../syllabus/module-3/java_m3_bai10_Mini_Project.md).

Phát triển từ [`demo-bai7-mongodb-thymeleaf`](../demo-bai7-mongodb-thymeleaf): public Anime + **Spring Security** + **Admin Dashboard** (D1–D6) + **CRUD phim**.

## Yêu cầu

- JDK 17+
- MongoDB (URI trong `application.properties`)
- Dataset `mymoviedb` đã import (xem README Bài 7)

## Chạy

```bash
cd demo-bai10-mini-project/java-springboot-bai10
./mvnw spring-boot:run
```

| URL | Ai | Mô tả |
|-----|-----|--------|
| http://localhost:8080/movies | Khách | Public (không cần login) |
| http://localhost:8080/login | — | Form login admin |
| http://localhost:8080/admin/dashboard | ADMIN | Thống kê D1–D6 |
| http://localhost:8080/admin/movies | ADMIN | List / thêm / sửa / xóa |

**Tài khoản admin:** lưu trong MongoDB collection `users` (BCrypt). Lần đầu chạy app, `AdminUserSeeder` tạo `admin` / `admin123` nếu chưa có — **không** hardcode user/pass trong `application.properties`.

## Quy ước code (giống module 3)

- Model suffix `Model`; DTO form; `Controller → Service → Repository`
- Constructor injection + Lombok `@RequiredArgsConstructor`
- Comment block `// --- ... ---` trong bước quan trọng
- Aggregation dashboard **không** `findAll` rồi group trên JVM
- Static: `th:href="@{/...}"` / `th:src="@{/...}"`
- Admin UI: layout Bootstrap (sidebar) — có thể thay bằng Dashmin đầy đủ trong `static/dashmin/`

## File mới chính (so với Bài 7)

| Nhóm | File |
|------|------|
| Security | `SecurityConfig`, `UserModel`, `UserRepository`, `MongoUserDetailsService`, `AdminUserSeeder`, `LoginController`, `login.html` |
| Chuẩn hóa | `MovieDataNormalizer`, `MovieNormalizeRunner`; `MovieModel` + `dateAddedAt`, `durationMinutes` |
| Dashboard | `DashboardStatsService`, DTOs thống kê, `AdminDashboardController`, `admin/dashboard.html` |
| CRUD | `AdminMovieService`, `AdminMovieController`, `MovieFormDto`, `movies-list.html`, `movie-form.html` |

## Liên kết

- Syllabus: [`java_m3_bai10_Mini_Project.md`](../syllabus/module-3/java_m3_bai10_Mini_Project.md)
- Nền public: [`demo-bai7-mongodb-thymeleaf`](../demo-bai7-mongodb-thymeleaf)
