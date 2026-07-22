# Demo Bài 7 — MongoDB + Thymeleaf + Template Anime gốc

Project demo cho syllabus [`java_m3_bai7_Database_Query_To_FrontEnd.md`](../syllabus/module-3/java_m3_bai7_Database_Query_To_FrontEnd.md).

Dùng **template Anime gốc** ([ThemeWagon](https://github.com/themewagon/anime)) và dataset **Netflix CSV** (PDF trang 4 — Kaggle).

## Yêu cầu

- JDK 17+
- MongoDB `localhost:27017`
- `mongoimport` (MongoDB Database Tools)

## 1. Import dữ liệu (bắt buộc — không có DataSeeder)

**Theo PDF trang 4:** tải dataset từ
[rahulverma07/netflix-movie-dataset](https://www.kaggle.com/datasets/rahulverma07/netflix-movie-dataset),
giải nén và import:

```bash
mongoimport \
  --db db_java_t3h_module3 \
  --collection mymoviedb \
  --type csv \
  --headerline \
  --file mymoviedb.csv
```

**Hoặc dùng script** (sample 100 dòng có sẵn trong repo):

```bash
chmod +x scripts/import-movies.sh
./scripts/import-movies.sh sample-data/mymoviedb.csv
```

File CSV đầy đủ (~8800 dòng): `sample-data/mymoviedb-full.csv` (đã tải sẵn từ nguồn công khai tương đương Kaggle).

Kiểm tra:

```javascript
use db_java_t3h_module3
db.mymoviedb.findOne()
db.mymoviedb.countDocuments()
```

Header CSV: `show_id, type, title, director, cast, country, date_added, release_year, rating, duration, listed_in, description`

## 2. Chạy project

```bash
cd demo-bai7-mongodb-thymeleaf/java-springboot-bai7
./mvnw spring-boot:run
```

- **http://localhost:8080/movies** — trang chủ (`index.html` gốc: Hero + Trending + Popular + Recent + Live Action)
- **http://localhost:8080/movies/home** — danh sách phân trang (`categories.html`)
- **http://localhost:8080/movies/detail/{id}** — chi tiết + comment (`anime-details.html`)
- **http://localhost:8080/movies/chart** — biểu đồ top country

## Quy ước code

- Model: `MovieModel`, `CommentModel` (suffix `Model`)
- Template: tách fragment trong `templates/fragments/` (`layout`, `movie`, `comments`)
- Service/Controller: comment block `// --- ... ---` trong từng bước của hàm

## Template & fragments

Static assets (`css`, `js`, `img`, `fonts`) copy từ [`themewagon/anime`](https://github.com/themewagon/anime).

HTML dùng chung qua Thymeleaf fragment. Static resources **bắt buộc** `th:href="@{/css/...}"` / `th:src="@{/js/...}"` (giống Bài 4/5).

| Fragment | Nội dung |
|----------|----------|
| `fragments/layout.html` | head, header, footer, search, scripts |
| `fragments/movie.html` | productCard, productSidebar |
| `fragments/comments.html` | reviewList, commentForm, relatedSidebar |

Trang `anime-main/*.html` chỉ giữ nội dung riêng + `th:replace`.

## Màn hình

| URL | Template gốc | Dữ liệu MongoDB |
|-----|----------------|-----------------|
| `/movies` | `index.html` | Hero, Trending, Popular, Recent, Live Action, Sidebar |
| `/movies/home` | `categories.html` | Phân trang + sidebar Top Views / New Comment |
| `/movies/detail/{id}` | `anime-details.html` | Chi tiết phim + Reviews (comments) + related |
| `/movies/watching/{id}` | `anime-watching.html` | Video mẫu template + episodes + reviews |
| `/movies/chart` | trang chart | Aggregation theo `country` |

### Map section `index.html` → query

| Section template | Query | Ảnh template |
|------------------|-------|--------------|
| Hero slider | Top 3 theo `release_year` DESC | `/img/hero/hero-1.jpg` |
| Trending Now | `type = Movie`, 6 bản ghi | `/img/trending/trend-*.jpg` |
| Popular Shows | `type = TV Show`, 6 bản ghi | `/img/popular/popular-*.jpg` |
| Recently Added | Top theo `release_year` (trang 2) | `/img/recent/recent-*.jpg` |
| Live Action | `listed_in` chứa `Action` | `/img/live/live-*.jpg` |
| Top Views / New Comment | Sidebar 5 + 4 title | `/img/sidebar/*` |

### Detail & Watching

| Trang | Nguồn dữ liệu |
|-------|----------------|
| `anime-details` | `MovieDetailDto` (title, type, director, cast, country, rating, duration, listed_in…) |
| Reviews | Collection `comments` theo `movieId` |
| you might like | 4 title khác id hiện tại |
| Watch Now | → `/movies/watching/{id}` |
| Watching video | `/videos/1.mp4` + poster `/videos/anime-watch.jpg` (file gốc template) |
| Episodes | Movie = 1 tập; TV Show = 12 tập (giả lập vì CSV không có episode) |

## Liên kết

- Syllabus: [`java_m3_bai7_Database_Query_To_FrontEnd.md`](../syllabus/module-3/java_m3_bai7_Database_Query_To_FrontEnd.md)
- Template nguồn: https://github.com/themewagon/anime
