package vn.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import vn.demo.model.MovieModel;

/**
 * REPOSITORY — tầng truy cập dữ liệu cho collection "movies".
 *
 * <p>Nhiệm vụ của tầng Repository: giao tiếp với MongoDB. Đây là interface, ta
 * KHÔNG cần tự viết code — Spring Data tự sinh phần hiện thực khi chạy.</p>
 *
 * <p>{@link MongoRepository} đã có sẵn: {@code findAll()}, {@code findById()},
 * {@code save()}, {@code saveAll()}, {@code delete()}, {@code count()}, phân trang...
 * nên KHÔNG cần khai báo lại. Bên dưới chỉ thêm các <b>derived query method</b> —
 * Spring tự sinh truy vấn dựa theo TÊN hàm.</p>
 */
@Repository
public interface MovieRepository extends MongoRepository<MovieModel, String> {

	/** Tìm 1 phim theo title chính xác. Trả Optional để xử lý khi không có. */
	Optional<MovieModel> findByTitle(String title);

	/** Tìm theo đạo diễn. */
	List<MovieModel> findByDirector(String director);

	/** Tìm theo năm VÀ title. */
	List<MovieModel> findByYearAndTitle(Integer year, String title);

	/** Tìm các phim có rating LỚN HƠN ngưỡng. */
	List<MovieModel> findByRatingGreaterThan(Double rating);

	/** Tìm các phim có rating NHỎ HƠN HOẶC BẰNG ngưỡng. */
	List<MovieModel> findByRatingLessThanEqual(Double rating);

	/** Tìm các phim có năm sản xuất trong khoảng [startYear, endYear]. */
	List<MovieModel> findByYearBetween(Integer startYear, Integer endYear);

	/** Bài tập: rating >= ... VÀ year >= ... (rating > 7 và năm từ 2015). */
	List<MovieModel> findByRatingGreaterThanEqualAndYearGreaterThanEqual(Double rating, Integer year);

	/** Tìm kiếm theo từ khóa trong title, không phân biệt hoa thường. */
	List<MovieModel> findByTitleContainingIgnoreCase(String keyword);

	/** Phiên bản phân trang của hàm trên — dùng cho màn hình danh sách (Thymeleaf). */
	Page<MovieModel> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

}
