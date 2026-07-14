package vn.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import vn.demo.model.MovieModel;

/**
 * SLICE TEST Repository — syllabus §5.
 *
 * <p><b>Mục tiêu học viên:</b></p>
 * <ul>
 *   <li>Hiểu {@code @DataMongoTest}: chỉ load phần Spring Data Mongo (nhanh hơn {@code @SpringBootTest})</li>
 *   <li>Dùng profile {@code test} + URI {@code testdb} — tránh {@code deleteAll()} đụng DB học</li>
 *   <li>Seed trong {@code @BeforeEach}, assert cả <b>số lượng</b> và <b>điều kiện rating</b></li>
 * </ul>
 *
 * <p><b>Yêu cầu:</b> MongoDB đang chạy ở {@code localhost:27017}.
 * Chạy: {@code ./mvnw test -Dtest=MovieRepositoryTest}</p>
 */
@DataMongoTest
@ActiveProfiles("test")
class MovieRepositoryTest {

	@Autowired
	private MovieRepository movieRepository;

	/** Mỗi test bắt đầu collection sạch trên testdb. */
	@BeforeEach
	void clean() {
		movieRepository.deleteAll();
	}

	@Test
	@DisplayName("findByRatingGreaterThanEqual: chỉ trả phim rating >= 8.0")
	void findByRatingGreaterThanEqual_returnsOnlyMoviesAtOrAboveThreshold() {
		movieRepository.save(movieWithRating(6.0));
		movieRepository.save(movieWithRating(9.5));
		movieRepository.save(movieWithRating(8.0));

		List<MovieModel> result = movieRepository.findByRatingGreaterThanEqual(8.0);

		assertThat(result).hasSize(2);
		assertThat(result).extracting(MovieModel::getRating)
				.allMatch(r -> r >= 8.0);
	}

	@Test
	@DisplayName("@Query findHighlyRated cho cùng kết quả với derived method")
	void findHighlyRated_queryAnnotation_matchesDerivedMethod() {
		movieRepository.save(movieWithRating(7.0));
		movieRepository.save(movieWithRating(8.5));

		List<MovieModel> derived = movieRepository.findByRatingGreaterThanEqual(8.0);
		List<MovieModel> queried = movieRepository.findHighlyRated(8.0);

		assertThat(queried).hasSize(derived.size());
		assertThat(queried).extracting(MovieModel::getRating)
				.containsExactlyInAnyOrderElementsOf(
						derived.stream().map(MovieModel::getRating).toList());
	}

	@Test
	@DisplayName("findByTitleContainingIgnoreCase: tìm không phân biệt hoa thường")
	void findByTitleContainingIgnoreCase_matchesSubstring() {
		movieRepository.save(movieWithTitle("Inception"));
		movieRepository.save(movieWithTitle("Interstellar"));
		movieRepository.save(movieWithTitle("Barbie"));

		List<MovieModel> result = movieRepository.findByTitleContainingIgnoreCase("inter");

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getTitle()).isEqualTo("Interstellar");
	}

	private static MovieModel movieWithRating(double rating) {
		MovieModel m = new MovieModel();
		m.setTitle("Sample " + rating);
		m.setYear(2020);
		m.setRating(rating);
		return m;
	}

	private static MovieModel movieWithTitle(String title) {
		MovieModel m = new MovieModel();
		m.setTitle(title);
		m.setYear(2020);
		m.setRating(7.0);
		return m;
	}

}
