package vn.demo.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
	public List<MovieModel> getAllMovies() {
		return movieRepository.findAll();
	}

	/** Lấy 1 trang phim có tìm kiếm theo từ khóa (dùng cho màn hình Thymeleaf). */
	public Page<MovieModel> findPage(String keyword, Pageable pageable) {
		String safeKeyword = keyword == null ? "" : keyword.trim();
		return movieRepository.findByTitleContainingIgnoreCase(safeKeyword, pageable);
	}

	/**
	 * Tìm 1 phim theo id; ném {@link ResourceNotFoundException} nếu không có.
	 *
	 * <p>Dùng {@code Optional.orElseThrow} thay cho việc trả null rồi kiểm tra — rõ và an toàn hơn.</p>
	 */
	public MovieModel getById(String id) {
		return movieRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim với id: " + id));
	}

	/** Tìm 1 phim theo title; ném 404 nếu không có. */
	public MovieModel getByTitle(String title) {
		return movieRepository.findByTitle(title)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim với title: " + title));
	}

	/** Tìm kiếm theo từ khóa của title (không phân biệt hoa thường). */
	public List<MovieModel> searchByKeyword(String keyword) {
		return movieRepository.findByTitleContainingIgnoreCase(keyword == null ? "" : keyword.trim());
	}

	/** Bài tập: rating >= ratingMin VÀ year >= yearFrom. */
	public List<MovieModel> findGoodMovies(Double ratingMin, Integer yearFrom) {
		return movieRepository.findByRatingGreaterThanEqualAndYearGreaterThanEqual(ratingMin, yearFrom);
	}

	/** Tạo mới 1 phim. */
	public MovieModel create(MovieModel movie) {
		movie.setId(null); // đảm bảo là tạo mới, không vô tình ghi đè document theo id gửi lên
		MovieModel saved = movieRepository.save(movie);
		log.info("Đã tạo movie id={}", saved.getId());
		return saved;
	}

	/**
	 * Cập nhật phim theo id, chỉ ghi đè những field có giá trị (partial update).
	 *
	 * @param id     id phim cần sửa
	 * @param params dữ liệu mới
	 * @return phim sau khi cập nhật
	 */
	public MovieModel update(String id, MovieModel params) {
		MovieModel existing = getById(id); // tận dụng lại getById (đã tự ném 404 nếu không có)
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

	/** Xóa phim theo id; ném 404 nếu không có. */
	public void delete(String id) {
		MovieModel existing = getById(id);
		movieRepository.delete(existing);
		log.info("Đã xóa movie id={}", id);
	}

}
