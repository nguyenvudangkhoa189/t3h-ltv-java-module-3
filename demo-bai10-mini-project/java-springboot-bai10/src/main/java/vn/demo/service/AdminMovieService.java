package vn.demo.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import vn.demo.dto.MovieFormDto;
import vn.demo.model.MovieModel;
import vn.demo.repository.CommentRepository;
import vn.demo.repository.MovieRepository;
import vn.demo.util.MovieDataNormalizer;

/**
 * SERVICE — CRUD phim phía admin.
 *
 * <p><b>Flow chung:</b> Controller nhận form DTO → validate → Service map/normalize
 * → Repository save/delete → PRG về list. Xóa phim kèm xóa comment theo {@code movieId}.</p>
 */
@Service
@RequiredArgsConstructor
public class AdminMovieService {

	private final MovieRepository movieRepository;
	private final CommentRepository commentRepository;

	/** List phân trang (sort năm phát hành giảm dần). */
	public Page<MovieModel> findPage(int page, int size) {
		return movieRepository.findAll(
				PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "release_year")));
	}

	/** Prefill form sửa — không thấy id → 404. */
	public MovieFormDto getFormById(String id) {
		return toForm(requireMovie(id));
	}

	/**
	 * Thêm phim mới.
	 *
	 * <p>Flow: map DTO → sinh {@code show_id} → normalize ngày/phút → save → trả id.</p>
	 */
	public String create(MovieFormDto form) {
		MovieModel m = new MovieModel();
		// --- Copy field từ form ---
		applyForm(m, form);
		// --- show_id server-side (không nhập tay) ---
		m.setShowId("s" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
		normalize(m);
		return movieRepository.save(m).getId();
	}

	/**
	 * Cập nhật phim.
	 *
	 * <p>Flow: load theo id → ghi field cho phép → giữ nguyên show_id → normalize → save.</p>
	 */
	public void update(String id, MovieFormDto form) {
		MovieModel m = requireMovie(id);
		String showId = m.getShowId();
		applyForm(m, form);
		// --- Không cho đổi show_id / @Id ---
		m.setShowId(showId);
		normalize(m);
		movieRepository.save(m);
	}

	/**
	 * Xóa phim (hard delete).
	 *
	 * <p>Flow: kiểm tra tồn tại → xóa comments theo movieId → xóa movie.</p>
	 */
	public void deleteById(String id) {
		requireMovie(id);
		// --- Cascade comment (khuyến nghị syllabus) ---
		commentRepository.deleteByMovieId(id);
		movieRepository.deleteById(id);
	}

	private MovieModel requireMovie(String id) {
		return movieRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"));
	}

	/** Gán field cho phép sửa từ DTO sang entity. */
	private void applyForm(MovieModel m, MovieFormDto form) {
		m.setTitle(form.getTitle());
		m.setType(form.getType());
		m.setDirector(form.getDirector());
		m.setCast(form.getCast());
		m.setCountry(form.getCountry());
		m.setDateAdded(form.getDateAdded());
		m.setReleaseYear(form.getReleaseYear());
		m.setRating(form.getRating());
		m.setDuration(form.getDuration());
		m.setListedIn(form.getListedIn());
		m.setDescription(form.getDescription());
	}

	/** Đồng bộ field chuẩn hóa sau create/update. */
	private void normalize(MovieModel m) {
		m.setDateAddedAt(MovieDataNormalizer.parseDateAdded(m.getDateAdded()));
		if ("Movie".equalsIgnoreCase(m.getType())) {
			m.setDurationMinutes(MovieDataNormalizer.parseDurationMinutes(m.getDuration()));
		} else {
			m.setDurationMinutes(null);
		}
	}

	/** Entity → DTO form (prefill edit). */
	private MovieFormDto toForm(MovieModel m) {
		MovieFormDto dto = new MovieFormDto();
		dto.setId(m.getId());
		dto.setShowId(m.getShowId());
		dto.setTitle(m.getTitle());
		dto.setType(m.getType());
		dto.setDirector(m.getDirector());
		dto.setCast(m.getCast());
		dto.setCountry(m.getCountry());
		dto.setDateAdded(m.getDateAdded());
		dto.setReleaseYear(m.getReleaseYear());
		dto.setRating(m.getRating());
		dto.setDuration(m.getDuration());
		dto.setListedIn(m.getListedIn());
		dto.setDescription(m.getDescription());
		return dto;
	}

}
