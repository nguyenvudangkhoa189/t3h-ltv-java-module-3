package vn.demo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import vn.demo.model.MovieModel;

/**
 * REPOSITORY — truy cập MongoDB cho collection {@code movies}.
 *
 * <p>Syllabus §5: kiểm tra query bằng {@code @DataMongoTest} (slice), không mock.
 * Chỉ cần test các method <b>quan trọng / dễ sai</b> (rating threshold, search) —
 * không bắt buộc test hết mọi derived method.</p>
 */
@Repository
public interface MovieRepository extends MongoRepository<MovieModel, String> {

	/**
	 * Derived query: rating &gt;= ngưỡng.
	 * Dùng trong Service {@code findHighlyRated} và trong {@code MovieRepositoryTest}.
	 */
	List<MovieModel> findByRatingGreaterThanEqual(Double rating);

	/**
	 * Tìm title chứa keyword (không phân biệt hoa thường) — bài tập checklist syllabus.
	 */
	List<MovieModel> findByTitleContainingIgnoreCase(String keyword);

	/**
	 * Tuỳ chọn syllabus §5.2 — cùng ý với derived {@link #findByRatingGreaterThanEqual}.
	 * Giữ lại để HV so sánh cú pháp {@code @Query} vs derived method.
	 */
	@Query("{ 'rating' : { $gte: ?0 } }")
	List<MovieModel> findHighlyRated(double minRating);

}
