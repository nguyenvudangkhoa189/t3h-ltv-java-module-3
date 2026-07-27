package vn.demo.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import vn.demo.model.CommentModel;

/**
 * REPOSITORY — collection {@code comments}.
 *
 * <p>Bài 10 thêm {@link #deleteByMovieId} để cascade khi admin xóa phim.</p>
 */
public interface CommentRepository extends MongoRepository<CommentModel, String> {

	List<CommentModel> findByMovieIdOrderByCreatedAtDesc(String movieId);

	List<CommentModel> findAllByOrderByCreatedAtDesc(Pageable pageable);

	/** Xóa mọi comment gắn {@code movieId} (cascade delete phim). */
	void deleteByMovieId(String movieId);

}
