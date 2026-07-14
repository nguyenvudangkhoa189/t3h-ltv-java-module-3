package vn.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.dto.MovieDto;
import vn.demo.exception.ResourceNotFoundException;
import vn.demo.model.MovieModel;
import vn.demo.repository.MovieRepository;

/**
 * SERVICE — tầng nghiệp vụ (syllabus §4 — <b>ưu tiên P0 khi đi làm</b>).
 *
 * <p>Controller chỉ nhận request rồi gọi xuống đây. Unit test Service bằng Mockito:
 * <b>mock</b> {@link MovieRepository}, không cần MongoDB chạy.</p>
 *
 * <p>Các method dưới đây được cover trong test:
 * {@link #findHighlyRated(double)}, {@link #getById(String)}, {@link #isValidMinRating(double)},
 * {@link #create}, {@link #update}, {@link #delete} (qua MockMvc §6).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

	/** Spring tiêm qua constructor (Lombok {@code @RequiredArgsConstructor}). */
	private final MovieRepository movieRepository;

	/** Lấy tất cả phim — map Entity → DTO. */
	public List<MovieDto> getAllMovies() {
		return movieRepository.findAll().stream()
				.map(MovieDto::fromEntity)
				.toList();
	}

	/**
	 * Tìm phim theo id.
	 *
	 * <p>Nếu không có → {@link ResourceNotFoundException} (test bằng {@code assertThrows}).</p>
	 */
	public MovieDto getById(String id) {
		return MovieDto.fromEntity(findEntityById(id));
	}

	/**
	 * Lấy các phim có {@code rating >= minRating}, trả DTO.
	 *
	 * <p>Lab Mockito + {@code @MethodSource} (syllabus §4): stub rõ list repo trả về.</p>
	 */
	public List<MovieDto> findHighlyRated(double minRating) {
		return movieRepository.findByRatingGreaterThanEqual(minRating).stream()
				.map(MovieDto::fromEntity)
				.toList();
	}

	/**
	 * Kiểm tra ngưỡng {@code min} hợp lệ cho API highly-rated (thang điểm 0..10).
	 *
	 * <p>Logic <b>thuần</b> (không gọi Repository) — phù hợp lab {@code @CsvSource}
	 * / Boundary Value (syllabus §3–§4). Không cần mock.</p>
	 */
	public boolean isValidMinRating(double min) {
		return min >= 0.0 && min <= 10.0;
	}

	/** Tìm theo từ khóa title — dùng cho bài tập Repository / API. */
	public List<MovieDto> searchByKeyword(String keyword) {
		String safeKeyword = keyword == null ? "" : keyword.trim();
		return movieRepository.findByTitleContainingIgnoreCase(safeKeyword).stream()
				.map(MovieDto::fromEntity)
				.toList();
	}

	/** Tạo mới (REST POST). */
	public MovieDto create(MovieDto movie) {
		MovieModel entity = movie.toEntity();
		entity.setId(null);
		MovieModel saved = movieRepository.save(entity);
		log.info("Đã tạo movie id={}", saved.getId());
		return MovieDto.fromEntity(saved);
	}

	/**
	 * Cập nhật phim theo id — chỉ ghi đè field có giá trị (partial update).
	 *
	 * <p>Không tìm thấy → {@link ResourceNotFoundException} (404).</p>
	 */
	public MovieDto update(String id, MovieDto params) {
		MovieModel existing = findEntityById(id);
		MovieModel incoming = params.toEntity();
		if (incoming.getTitle() != null) {
			existing.setTitle(incoming.getTitle());
		}
		if (incoming.getYear() != null) {
			existing.setYear(incoming.getYear());
		}
		if (incoming.getGenre() != null) {
			existing.setGenre(incoming.getGenre());
		}
		if (incoming.getDirector() != null) {
			existing.setDirector(incoming.getDirector());
		}
		if (incoming.getRating() != null) {
			existing.setRating(incoming.getRating());
		}
		MovieModel saved = movieRepository.save(existing);
		log.info("Đã cập nhật movie id={}", id);
		return MovieDto.fromEntity(saved);
	}

	/** Xóa phim theo id; không tìm thấy → {@link ResourceNotFoundException}. */
	public void delete(String id) {
		MovieModel existing = findEntityById(id);
		movieRepository.delete(existing);
		log.info("Đã xóa movie id={}", id);
	}

	private MovieModel findEntityById(String id) {
		return movieRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim với id: " + id));
	}

}
