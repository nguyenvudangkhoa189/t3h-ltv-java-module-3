package vn.demo.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import vn.demo.model.CommentModel;

/**
 * REPOSITORY — truy vấn collection {@code comments}.
 */
public interface CommentRepository extends MongoRepository<CommentModel, String> {

	List<CommentModel> findByMovieIdOrderByCreatedAtDesc(String movieId);

	List<CommentModel> findAllByOrderByCreatedAtDesc(Pageable pageable);

}
