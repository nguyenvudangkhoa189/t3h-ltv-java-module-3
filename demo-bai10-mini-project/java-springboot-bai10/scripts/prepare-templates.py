#!/usr/bin/env python3
"""
[LEGACY] Script convert Anime HTML → Thymeleaf một lần.

Hiện tại các trang đã tách fragment thủ công:
  templates/fragments/{layout,movie,comments}.html
  templates/anime-main/*.html  (th:replace)

KHÔNG chạy script này nếu không muốn ghi đè trang fragment.
Giữ lại để tham khảo mapping field / th:each.
"""
from pathlib import Path
import re
import sys

print("prepare-templates.py đã ngừng ghi đè trang (dùng fragments).")
print("Xem templates/fragments/ và anime-main/*.html")
sys.exit(0)

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT.parent / "_anime-template"
OUT = ROOT / "src/main/resources/templates/anime-main"

TH_EACH_ITEM = """                            <div class="col-lg-4 col-md-6 col-sm-6" th:each="r : ${list}">
                                <div class="product__item">
                                    <a class="product__item__pic set-bg" th:href="@{/movies/detail/{id}(id=${r.id})}" th:attr="data-setbg=${r.posterUrl}">
                                        <div class="ep" th:text="${r.releaseYear}">2020</div>
                                        <div class="comment"><i class="fa fa-comments"></i> <span th:text="${r.rating}">PG</span></div>
                                        <div class="view"><i class="fa fa-eye"></i> <span th:text="${r.duration}">90 min</span></div>
                                    </a>
                                    <div class="product__item__text">
                                        <ul>
                                            <li th:text="${r.type}">Movie</li>
                                            <li th:text="${r.genreLabel}">Drama</li>
                                        </ul>
                                        <h5><a th:href="@{/movies/detail/{id}(id=${r.id})}" th:text="${r.title}">Title</a></h5>
                                    </div>
                                </div>
                            </div>"""

TH_PAGINATION = """                    <div class="product__pagination" th:if="${totalPages > 0}">
                        <a th:if="${currentPage > 0}" th:href="@{/movies/home(page=${currentPage - 1})}">&laquo;</a>
                        <a th:each="pageNum : ${#numbers.sequence(startPage, endPage)}"
                           th:href="@{/movies/home(page=${pageNum})}"
                           th:classappend="${pageNum == currentPage} ? 'current-page'"
                           th:text="${pageNum + 1}">1</a>
                        <a th:if="${currentPage < totalPages - 1}" th:href="@{/movies/home(page=${currentPage + 1})}"><i class="fa fa-angle-double-right"></i></a>
                    </div>"""

TH_SIDEBAR_TOP = """                            <div class="filter__gallery">
                                <a class="product__sidebar__view__item set-bg mix day years"
                                     th:each="t : ${topViews}"
                                     th:href="@{/movies/detail/{id}(id=${t.id})}"
                                     th:attr="data-setbg=${t.posterUrl}">
                                    <div class="ep" th:text="${t.releaseYear}">2020</div>
                                    <div class="view"><i class="fa fa-eye"></i> <span th:text="${t.duration}">90 min</span></div>
                                    <h5 th:text="${t.title}">Title</h5>
                                </a>
                            </div>"""

TH_SIDEBAR_COMMENT = """    <div class="product__sidebar__comment">
        <div class="section-title">
            <h5>New Comment</h5>
        </div>
        <div class="product__sidebar__comment__item" th:each="s : ${sidebarComments}">
            <div class="product__sidebar__comment__item__pic">
                <a th:href="@{/movies/detail/{id}(id=${s.movieId})}"><img th:src="${s.posterUrl}" alt=""></a>
            </div>
            <div class="product__sidebar__comment__item__text">
                <ul>
                    <li th:text="${s.authorName}">Guest</li>
                </ul>
                <h5><a th:href="@{/movies/detail/{id}(id=${s.movieId})}" th:text="${s.movieTitle}">Title</a></h5>
                <span th:text="${s.messagePreview}">Comment preview</span>
            </div>
        </div>
        <p th:if="${#lists.isEmpty(sidebarComments)}" style="color:#b7b7b7;font-size:13px;">Chưa có bình luận.</p>
    </div>"""

TH_HERO = """            <div class="hero__slider owl-carousel">
                <div class="hero__items set-bg" th:each="h : ${hero}" th:attr="data-setbg=${h.posterUrl}">
                    <div class="row">
                        <div class="col-lg-6">
                            <div class="hero__text">
                                <div class="label" th:text="${h.genreLabel}">Adventure</div>
                                <h2><a th:href="@{/movies/detail/{id}(id=${h.id})}" th:text="${h.title}" style="color:inherit;text-decoration:none;">Title</a></h2>
                                <p th:text="${h.description}">Description</p>
                                <a th:href="@{/movies/detail/{id}(id=${h.id})}"><span>Watch Now</span> <i class="fa fa-angle-right"></i></a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>"""

TH_REVIEWS = """                        <div class="anime__details__review">
                            <div class="section-title">
                                <h5 th:text="|Bình luận (${#lists.size(comments)})|">Bình luận</h5>
                            </div>
                            <div class="anime__review__item" th:each="c, iter : ${comments}">
                                <div class="anime__review__item__pic">
                                    <img th:src="@{/img/anime/review-{n}.jpg(n=${(iter.index % 6) + 1})}" alt="">
                                </div>
                                <div class="anime__review__item__text">
                                    <h6><span th:text="${c.name}">Name</span> - <span th:text="${#temporals.format(c.createdAt, 'dd/MM/yyyy HH:mm')}">Date</span></h6>
                                    <p th:text="${c.message}">Message</p>
                                </div>
                            </div>
                            <p th:if="${#lists.isEmpty(comments)}" style="color:#b7b7b7;">Chưa có bình luận. Hãy là người đầu tiên — không cần đăng nhập!</p>
                        </div>"""

TH_DETAIL_FORM = """                        <div class="anime__details__form">
                            <div class="section-title">
                                <h5>Viết bình luận</h5>
                            </div>
                            <p style="color:#b7b7b7;font-size:13px;margin-bottom:12px;">Không cần đăng nhập. Chỉ cần tên và nội dung.</p>
                            <p th:if="${message}" th:text="${message}" style="color:#0f0;"></p>
                            <form th:action="@{/movies/detail/{id}(id=${movie.id})}" th:object="${commentForm}" method="post">
                                <input type="text" placeholder="Tên của bạn *" th:field="*{name}" style="width:100%;margin-bottom:15px;padding:10px;background:#1d1e39;border:none;color:#fff;">
                                <span style="color:#e53637;" th:if="${#fields.hasErrors('name')}" th:errors="*{name}"></span>
                                <input type="text" placeholder="Email (không bắt buộc)" th:field="*{email}" style="width:100%;margin-bottom:15px;padding:10px;background:#1d1e39;border:none;color:#fff;">
                                <span style="color:#e53637;" th:if="${#fields.hasErrors('email')}" th:errors="*{email}"></span>
                                <textarea placeholder="Nội dung bình luận *" th:field="*{message}"></textarea>
                                <span style="color:#e53637;" th:if="${#fields.hasErrors('message')}" th:errors="*{message}"></span>
                                <button type="submit"><i class="fa fa-location-arrow"></i> Gửi bình luận</button>
                            </form>
                        </div>"""

TH_RELATED = """                        <div class="anime__details__sidebar">
                            <div class="section-title">
                                <h5>you might like...</h5>
                            </div>
                            <a class="product__sidebar__view__item set-bg"
                                 th:each="r : ${related}"
                                 th:href="@{/movies/detail/{id}(id=${r.id})}"
                                 th:attr="data-setbg=${r.posterUrl}">
                                <div class="ep" th:text="${r.releaseYear}">2020</div>
                                <div class="view"><i class="fa fa-eye"></i> <span th:text="${r.duration}">90 min</span></div>
                                <h5 th:text="${r.title}">Title</h5>
                            </a>
                        </div>"""

TH_WATCH_FORM = """                    <div class="anime__details__form">
                        <div class="section-title">
                            <h5>Viết bình luận</h5>
                        </div>
                        <p style="color:#b7b7b7;font-size:13px;margin-bottom:12px;">Không cần đăng nhập. Chỉ cần tên và nội dung.</p>
                        <p th:if="${message}" th:text="${message}" style="color:#0f0;"></p>
                        <form th:action="@{/movies/watching/{id}(id=${movie.id})}" th:object="${commentForm}" method="post">
                            <input type="text" placeholder="Tên của bạn *" th:field="*{name}" style="width:100%;margin-bottom:15px;padding:10px;background:#1d1e39;border:none;color:#fff;">
                            <span style="color:#e53637;" th:if="${#fields.hasErrors('name')}" th:errors="*{name}"></span>
                            <input type="text" placeholder="Email (không bắt buộc)" th:field="*{email}" style="width:100%;margin-bottom:15px;padding:10px;background:#1d1e39;border:none;color:#fff;">
                            <span style="color:#e53637;" th:if="${#fields.hasErrors('email')}" th:errors="*{email}"></span>
                            <textarea placeholder="Nội dung bình luận *" th:field="*{message}"></textarea>
                            <span style="color:#e53637;" th:if="${#fields.hasErrors('message')}" th:errors="*{message}"></span>
                            <button type="submit"><i class="fa fa-location-arrow"></i> Gửi bình luận</button>
                        </form>
                    </div>"""


def fix_paths(content: str) -> str:
    content = content.replace('xmlns:th="http://www.thymeleaf.org"', "")
    content = re.sub(r"<html([^>]*)>", r'<html\1 xmlns:th="http://www.thymeleaf.org">', content, count=1)
    for a, b in [
        ('href="css/', 'href="/css/'),
        ('src="css/', 'src="/css/'),
        ('href="js/', 'href="/js/'),
        ('src="js/', 'src="/js/'),
        ('src="img/', 'src="/img/'),
        ('data-setbg="img/', 'data-setbg="/img/'),
        ('src="videos/', 'src="/videos/'),
        ('data-poster="./videos/', 'data-poster="/videos/'),
        ('data-poster="videos/', 'data-poster="/videos/'),
    ]:
        content = content.replace(a, b)
    content = content.replace('href="./index.html"', 'th:href="@{/movies}"')
    content = content.replace('href="./categories.html"', 'th:href="@{/movies/home}"')
    content = content.replace('href="./anime-details.html"', 'th:href="@{/movies/home}"')
    content = content.replace('href="./anime-watching.html"', 'th:href="@{/movies/home}"')
    content = content.replace('href="./blog-details.html"', 'th:href="@{/movies/chart}"')
    content = content.replace('href="./blog.html"', 'th:href="@{/movies/chart}"')
    content = content.replace('<h4>Romance</h4>', '<h4>Netflix Movies</h4>')
    content = content.replace(
        '<a href="#" class="primary-btn">View All <span class="arrow_right"></span></a>',
        '<a th:href="@{/movies/home}" class="primary-btn">View All <span class="arrow_right"></span></a>',
    )
    # Bỏ mục Contacts (header + footer)
    content = re.sub(r'\s*<li><a href="#">Contacts</a></li>', '', content)
    return content


def close_div_at(text: str, start: int) -> int:
    i, depth = start, 0
    while i < len(text):
        if text.startswith("<div", i):
            depth += 1
            i = text.index(">", i) + 1
        elif text.startswith("</div>", i):
            depth -= 1
            i += 6
            if depth == 0:
                return i
        else:
            i += 1
    raise RuntimeError("unbalanced div")


def product_item(list_name: str) -> str:
    return f"""                        <div class="row">
                            <div class="col-lg-4 col-md-6 col-sm-6" th:each="r : ${{{list_name}}}">
                                <div class="product__item">
                                    <a class="product__item__pic set-bg" th:href="@{{/movies/detail/{{id}}(id=${{r.id}})}}" th:attr="data-setbg=${{r.posterUrl}}">
                                        <div class="ep" th:text="${{r.releaseYear}}">2020</div>
                                        <div class="comment"><i class="fa fa-comments"></i> <span th:text="${{r.rating}}">PG</span></div>
                                        <div class="view"><i class="fa fa-eye"></i> <span th:text="${{r.duration}}">90 min</span></div>
                                    </a>
                                    <div class="product__item__text">
                                        <ul>
                                            <li th:text="${{r.type}}">Movie</li>
                                            <li th:text="${{r.genreLabel}}">Drama</li>
                                        </ul>
                                        <h5><a th:href="@{{/movies/detail/{{id}}(id=${{r.id}})}}" th:text="${{r.title}}">Title</a></h5>
                                    </div>
                                </div>
                            </div>
                        </div>"""


def replace_section_row(text: str, section_class: str, list_name: str) -> str:
    marker = f'<div class="{section_class}">'
    start = text.index(marker)
    row1 = text.index('<div class="row">', start)
    row2 = text.index('<div class="row">', row1 + 1)
    end_row = close_div_at(text, row2)
    return text[:row2] + product_item(list_name) + text[end_row:]


def replace_sidebar_blocks(text: str) -> str:
    gal_start = text.index('<div class="filter__gallery">')
    gal_close = close_div_at(text, gal_start)
    text = text[:gal_start] + TH_SIDEBAR_TOP + text[gal_close:]
    c_start = text.index('<div class="product__sidebar__comment">')
    c_close = close_div_at(text, c_start)
    return text[:c_start] + TH_SIDEBAR_COMMENT + text[c_close:]


def prepare_categories():
    text = fix_paths((SRC / "categories.html").read_text(encoding="utf-8"))
    start = text.index('                        <div class="row">\n                            <div class="col-lg-4 col-md-6 col-sm-6">')
    pag_start = text.index('                    <div class="product__pagination">')
    pag_end = close_div_at(text, pag_start)
    # Đóng product__page__content trước pagination (giống template gốc)
    text = (
        text[:start]
        + '                        <div class="row">\n'
        + TH_EACH_ITEM
        + '\n                        </div>\n'
        + '                    </div>\n'
        + TH_PAGINATION
        + '\n'
        + text[pag_end:]
    )
    text = replace_sidebar_blocks(text)
    text = text.replace('<h4>Romance</h4>', '<h4>Netflix Movies</h4>')
    # nav active Categories
    text = text.replace(
        '<li class="active"><a th:href="@{/movies}">Homepage</a></li>\n                                <li><a th:href="@{/movies/home}">Categories',
        '<li><a th:href="@{/movies}">Homepage</a></li>\n                                <li class="active"><a th:href="@{/movies/home}">Categories',
    )
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / "categories.html").write_text(text, encoding="utf-8")


def prepare_index():
    text = fix_paths((SRC / "index.html").read_text(encoding="utf-8"))
    hero_start = text.index('<div class="hero__slider owl-carousel">')
    hero_end = text.index('</div>\n        </div>\n    </section>\n    <!-- Hero Section End -->')
    text = text[:hero_start] + TH_HERO + "\n        " + text[hero_end:]
    for section, list_name in [
        ("trending__product", "trending"),
        ("popular__product", "popular"),
        ("recent__product", "recent"),
        ("live__product", "liveAction"),
    ]:
        text = replace_section_row(text, section, list_name)
    text = replace_sidebar_blocks(text)
    (OUT / "index.html").write_text(text, encoding="utf-8")


def prepare_anime_details():
    text = fix_paths((SRC / "anime-details.html").read_text(encoding="utf-8"))

    # breadcrumb
    text = text.replace(
        '<span>Romance</span>',
        '<span th:text="${movie.genreLabel}">Genre</span>',
        1,
    )

    # pic + title + description + widget
    text = re.sub(
        r'<div class="anime__details__pic set-bg" data-setbg="[^"]+">\s*'
        r'<div class="comment"><i class="fa fa-comments"></i> 11</div>\s*'
        r'<div class="view"><i class="fa fa-eye"></i> 9141</div>\s*</div>',
        '''<div class="anime__details__pic set-bg" th:attr="data-setbg=${movie.posterUrl}">
                            <div class="comment"><i class="fa fa-comments"></i> <span th:text="${#lists.size(comments)}">0</span></div>
                            <div class="view"><i class="fa fa-eye"></i> <span th:text="${movie.duration}">90 min</span></div>
                        </div>''',
        text,
        count=1,
        flags=re.DOTALL,
    )

    text = re.sub(
        r'<div class="anime__details__title">\s*<h3>.*?</h3>\s*<span>.*?</span>\s*</div>',
        '''<div class="anime__details__title">
                                <h3 th:text="${movie.title}">Title</h3>
                                <span th:text="${movie.subtitle}">Subtitle</span>
                            </div>''',
        text,
        count=1,
        flags=re.DOTALL,
    )

    text = re.sub(
        r'<div class="anime__details__rating">.*?</div>\s*<p>Every human.*?</p>',
        '''<div class="anime__details__rating">
                                <div class="rating">
                                    <a href="#"><i class="fa fa-star"></i></a>
                                    <a href="#"><i class="fa fa-star"></i></a>
                                    <a href="#"><i class="fa fa-star"></i></a>
                                    <a href="#"><i class="fa fa-star"></i></a>
                                    <a href="#"><i class="fa fa-star-half-o"></i></a>
                                </div>
                                <span th:text="|${movie.rating}|">Rating</span>
                            </div>
                            <p th:text="${movie.description}">Description</p>''',
        text,
        count=1,
        flags=re.DOTALL,
    )

    text = re.sub(
        r'<div class="anime__details__widget">.*?</div>\s*<div class="anime__details__btn">',
        '''<div class="anime__details__widget">
                                <div class="row">
                                    <div class="col-lg-6 col-md-6">
                                        <ul>
                                            <li><span>Type:</span> <th:block th:text="${movie.type}">Movie</th:block></li>
                                            <li><span>Director:</span> <th:block th:text="${movie.director}">N/A</th:block></li>
                                            <li><span>Date added:</span> <th:block th:text="${movie.dateAdded}">N/A</th:block></li>
                                            <li><span>Country:</span> <th:block th:text="${movie.country}">N/A</th:block></li>
                                            <li><span>Genre:</span> <th:block th:text="${movie.listedIn}">N/A</th:block></li>
                                        </ul>
                                    </div>
                                    <div class="col-lg-6 col-md-6">
                                        <ul>
                                            <li><span>Release year:</span> <th:block th:text="${movie.releaseYear}">2020</th:block></li>
                                            <li><span>Rating:</span> <th:block th:text="${movie.rating}">N/A</th:block></li>
                                            <li><span>Duration:</span> <th:block th:text="${movie.duration}">N/A</th:block></li>
                                            <li><span>Quality:</span> HD</li>
                                            <li><span>Cast:</span> <th:block th:text="${#strings.abbreviate(movie.cast, 40)}">N/A</th:block></li>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                            <div class="anime__details__btn">''',
        text,
        count=1,
        flags=re.DOTALL,
    )

    text = re.sub(
        r'<a href="#" class="watch-btn"><span>Watch Now</span> <i\s+class="fa fa-angle-right"></i></a>',
        '<a th:href="@{/movies/watching/{id}(id=${movie.id})}" class="watch-btn"><span>Watch Now</span> <i class="fa fa-angle-right"></i></a>',
        text,
        count=1,
    )

    # reviews + form
    rev_start = text.index('<div class="anime__details__review">')
    form_start = text.index('<div class="anime__details__form">')
    form_close = close_div_at(text, form_start)
    text = text[:rev_start] + TH_REVIEWS + "\n" + TH_DETAIL_FORM + text[form_close:]

    # related sidebar
    side_start = text.index('<div class="anime__details__sidebar">')
    side_close = close_div_at(text, side_start)
    text = text[:side_start] + TH_RELATED + text[side_close:]

    (OUT / "anime-details.html").write_text(text, encoding="utf-8")


def prepare_anime_watching():
    text = fix_paths((SRC / "anime-watching.html").read_text(encoding="utf-8"))

    text = text.replace(
        '<a href="#">Romance</a>\n                        <span>Fate Stay Night: Unlimited Blade</span>',
        '<a th:href="@{/movies/detail/{id}(id=${movie.id})}" th:text="${movie.genreLabel}">Genre</a>\n'
        '                        <span th:text="${movie.title}">Title</span>',
        1,
    )

    # episodes
    text = re.sub(
        r'<div class="anime__details__episodes">\s*'
        r'<div class="section-title">\s*<h5>List Name</h5>\s*</div>.*?</div>\s*</div>\s*</div>',
        '''<div class="anime__details__episodes">
                        <div class="section-title">
                            <h5 th:text="${movie.title}">Episodes</h5>
                        </div>
                        <a th:each="ep, iter : ${movie.episodes}"
                           th:href="@{/movies/watching/{id}(id=${movie.id}, ep=${iter.count})}"
                           th:text="${ep}"
                           th:classappend="${iter.count == currentEp} ? ' active' : ''">Ep 01</a>
                    </div>
                </div>
            </div>''',
        text,
        count=1,
        flags=re.DOTALL,
    )

    # reviews + form (watching has no related sidebar)
    rev_start = text.index('<div class="anime__details__review">')
    form_start = text.index('<div class="anime__details__form">')
    form_close = close_div_at(text, form_start)
    # watching reviews reuse TH_REVIEWS but form posts to watching
    text = text[:rev_start] + TH_REVIEWS + "\n" + TH_WATCH_FORM + text[form_close:]

    (OUT / "anime-watching.html").write_text(text, encoding="utf-8")


if __name__ == "__main__":
    OUT.mkdir(parents=True, exist_ok=True)
    prepare_index()
    prepare_categories()
    prepare_anime_details()
    prepare_anime_watching()
    print("Templates ready:", OUT)
    for name in ["index.html", "categories.html", "anime-details.html", "anime-watching.html"]:
        print(" -", name, (OUT / name).stat().st_size, "bytes")
