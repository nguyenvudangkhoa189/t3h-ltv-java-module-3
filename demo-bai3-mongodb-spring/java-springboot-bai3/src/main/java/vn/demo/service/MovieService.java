package vn.demo.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.dto.MovieDto;
import vn.demo.dto.MovieFormDto;
import vn.demo.dto.PageMapper;
import vn.demo.dto.PagedListView;
import vn.demo.dto.PagedResponse;
import vn.demo.exception.ResourceNotFoundException;
import vn.demo.model.MovieModel;
import vn.demo.repository.MovieRepository;

/**
 * SERVICE — tầng nghiệp vụ cho Movie.
 *
 * <p>Nhiệm vụ của tầng Service: chứa logic nghiệp vụ (kiểm tra, xử lý, ghép dữ liệu).
 * Controller KHÔNG tự xử lý logic mà gọi xuống đây.</p>
 *
 * <p>Cả REST API ({@code MovieRestController}) lẫn giao diện Thymeleaf
 * ({@code MovieViewController}) đều gọi cùng Service này — minh họa lợi ích của tách
 * tầng: đổi tầng Controller/View không phải viết lại nghiệp vụ.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

	// @RequiredArgsConstructor sinh constructor để Spring tiêm MovieRepository vào đây
	private final MovieRepository movieRepository;

	/** Lấy tất cả phim (dùng cho API đơn giản). */
	public List<MovieDto> getAllMovies() {
		return movieRepository.findAll().stream()
				.map(MovieDto::fromEntity)
				.toList();
	}

	/** Lấy 1 trang phim có tìm kiếm theo từ khóa (dùng cho màn hình Thymeleaf). */
	public PagedListView<MovieDto> findPage(String keyword, Pageable pageable) {
		String safeKeyword = keyword == null ? "" : keyword.trim();
		Page<MovieModel> page = movieRepository.findByTitleContainingIgnoreCase(safeKeyword, pageable);
		return PagedListView.from(page, MovieDto::fromEntity, safeKeyword);
	}

	/**
	 * §6.1.1 — Cách 1: map trực tiếp sang {@code Page&lt;MovieDto&gt;} (Spring Data).
	 *
	 * <p>Dùng để học viên so sánh với {@link #findPage(String, Pageable)} (enterprise).
	 * Trong app thật, endpoint demo: {@code GET /movies/demo/spring-page} và
	 * {@code GET /api/movies/page}.</p>
	 */
	public Page<MovieDto> findPageAsSpringPage(String keyword, Pageable pageable) {
		String safeKeyword = keyword == null ? "" : keyword.trim();
		Page<MovieModel> page = movieRepository.findByTitleContainingIgnoreCase(safeKeyword, pageable);
		return page.map(MovieDto::fromEntity);
	}

	/**
	 * §6.1.3 — Cách 3 (REST): trả {@link vn.demo.dto.PagedResponse} generic, không lộ {@code Page}.
	 *
	 * <p>Endpoint demo: {@code GET /api/movies/paged}.</p>
	 */
	public vn.demo.dto.PagedResponse<MovieDto> findPageAsPagedResponse(String keyword, Pageable pageable) {
		String safeKeyword = keyword == null ? "" : keyword.trim();
		Page<MovieModel> page = movieRepository.findByTitleContainingIgnoreCase(safeKeyword, pageable);
		return PageMapper.map(page, MovieDto::fromEntity);
	}

	/**
	 * Tìm 1 phim theo id; ném {@link ResourceNotFoundException} nếu không có.
	 *
	 * <p>Dùng {@code Optional.orElseThrow} thay cho việc trả null rồi kiểm tra — rõ và an toàn hơn.</p>
	 */
	public MovieDto getById(String id) {
		return MovieDto.fromEntity(findEntityById(id));
	}

	/** Lấy dữ liệu form sửa theo id (dùng cho màn hình Thymeleaf). */
	public MovieFormDto getFormById(String id) {
		return MovieFormDto.fromEntity(findEntityById(id));
	}

	/** Tìm 1 phim theo title; ném 404 nếu không có. */
	public MovieDto getByTitle(String title) {
		MovieModel movie = movieRepository.findByTitle(title)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim với title: " + title));
		return MovieDto.fromEntity(movie);
	}

	/** Tìm kiếm theo từ khóa của title (không phân biệt hoa thường). */
	public List<MovieDto> searchByKeyword(String keyword) {
		String safeKeyword = keyword == null ? "" : keyword.trim();
		return movieRepository.findByTitleContainingIgnoreCase(safeKeyword).stream()
				.map(MovieDto::fromEntity)
				.toList();
	}

	/** Bài tập: rating >= ratingMin VÀ year >= yearFrom. */
	public List<MovieDto> findGoodMovies(Double ratingMin, Integer yearFrom) {
		return movieRepository.findByRatingGreaterThanEqualAndYearGreaterThanEqual(ratingMin, yearFrom).stream()
				.map(MovieDto::fromEntity)
				.toList();
	}

	/** Tạo mới 1 phim (REST API). */
	public MovieDto create(MovieDto movie) {
		return MovieDto.fromEntity(saveNew(movie.toEntity()));
	}

	/** Tạo mới 1 phim (form Thymeleaf). */
	public MovieDto create(MovieFormDto form) {
		return MovieDto.fromEntity(saveNew(form.toEntity()));
	}

	/**
	 * Cập nhật phim theo id, chỉ ghi đè những field có giá trị (partial update).
	 *
	 * @param id     id phim cần sửa
	 * @param params dữ liệu mới
	 * @return phim sau khi cập nhật
	 */
	public MovieDto update(String id, MovieDto params) {
		return MovieDto.fromEntity(applyUpdate(id, params.toEntity()));
	}

	/** Cập nhật phim từ form Thymeleaf. */
	public MovieDto update(String id, MovieFormDto form) {
		return MovieDto.fromEntity(applyUpdate(id, form.toEntity()));
	}

	/** Xóa phim theo id; ném 404 nếu không có. */
	public void delete(String id) {
		MovieModel existing = findEntityById(id);
		movieRepository.delete(existing);
		log.info("Đã xóa movie id={}", id);
	}

	private MovieModel findEntityById(String id) {
		return movieRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim với id: " + id));
	}

	private MovieModel saveNew(MovieModel movie) {
		movie.setId(null); // đảm bảo là tạo mới, không vô tình ghi đè document theo id gửi lên
		MovieModel saved = movieRepository.save(movie);
		log.info("Đã tạo movie id={}", saved.getId());
		return saved;
	}

	private MovieModel applyUpdate(String id, MovieModel params) {
		MovieModel existing = findEntityById(id); // tận dụng lại findEntityById (đã tự ném 404 nếu không có)
		if (params.getTitle() != null) {
			existing.setTitle(params.getTitle());
		}
		if (params.getYear() != null) {
			existing.setYear(params.getYear());
		}
		if (params.getGenre() != null) {
			existing.setGenre(params.getGenre());
		}
		if (params.getDirector() != null) {
			existing.setDirector(params.getDirector());
		}
		if (params.getRating() != null) {
			existing.setRating(params.getRating());
		}
		return movieRepository.save(existing);
	}

}
