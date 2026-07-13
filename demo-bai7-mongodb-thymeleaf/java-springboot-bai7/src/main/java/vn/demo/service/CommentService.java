package vn.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.demo.dto.CommentFormDto;
import vn.demo.dto.CommentSidebarDto;
import vn.demo.model.CommentModel;
import vn.demo.model.MovieModel;
import vn.demo.repository.CommentRepository;
import vn.demo.repository.MovieRepository;

/**
 * SERVICE — bình luận khách (không đăng nhập), lưu MongoDB collection {@code comments}.
 */
@Service
@RequiredArgsConstructor
public class CommentService {

	private final CommentRepository commentRepository;
	private final MovieRepository movieRepository;

	/** Lưu bình luận từ form → {@link CommentModel}. */
	public CommentModel save(CommentFormDto form) {
		// --- Map DTO form → entity (không bind entity trực tiếp từ request) ---
		CommentModel comment = new CommentModel();
		comment.setMovieId(form.getMovieId());
		comment.setName(form.getName().trim());
		String email = form.getEmail();
		comment.setEmail(email != null && !email.isBlank() ? email.trim() : null);
		comment.setMessage(form.getMessage().trim());
		comment.setCreatedAt(LocalDateTime.now());

		// --- Persist vào collection comments ---
		return commentRepository.save(comment);
	}

	/** Danh sách bình luận của 1 phim (mới nhất trước). */
	public List<CommentModel> findByMovieId(String movieId) {
		return commentRepository.findByMovieIdOrderByCreatedAtDesc(movieId);
	}

	/** Sidebar "New Comment" — comment mới nhất toàn site + tên phim. */
	public List<CommentSidebarDto> findRecentSidebar(int limit) {
		// --- Lấy N comment mới nhất ---
		List<CommentModel> recent = commentRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));

		// --- Bổ sung title phim (lookup theo movieId) ---
		return recent.stream().map(this::toSidebarDto).toList();
	}

	private CommentSidebarDto toSidebarDto(CommentModel c) {
		MovieModel movie = c.getMovieId() != null
				? movieRepository.findById(c.getMovieId()).orElse(null)
				: null;
		String title = movie != null && movie.getTitle() != null ? movie.getTitle() : "Movie";
		String preview = c.getMessage() != null ? c.getMessage() : "";
		if (preview.length() > 60) {
			preview = preview.substring(0, 57) + "...";
		}
		int pic = Math.abs((c.getId() != null ? c.getId() : "x").hashCode() % 4) + 1;
		return CommentSidebarDto.builder()
				.movieId(c.getMovieId())
				.movieTitle(title)
				.authorName(c.getName())
				.messagePreview(preview)
				.posterUrl("/img/sidebar/comment-" + pic + ".jpg")
				.createdAt(c.getCreatedAt())
				.build();
	}

}
