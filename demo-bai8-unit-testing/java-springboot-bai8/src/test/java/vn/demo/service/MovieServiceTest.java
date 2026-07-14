package vn.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.demo.dto.MovieDto;
import vn.demo.exception.ResourceNotFoundException;
import vn.demo.model.MovieModel;
import vn.demo.repository.MovieRepository;

/**
 * UNIT TEST Service — syllabus §4 (P0).
 *
 * <p><b>Mục tiêu học viên:</b></p>
 * <ul>
 *   <li>Hiểu {@code @Mock} / {@code @InjectMocks}: giả Repository, tiêm vào Service</li>
 *   <li>Viết AAA (Arrange–Act–Assert) + {@code when}/{@code verify}/{@code assertThrows}</li>
 *   <li>{@code @CsvSource}: tham số đơn giản (số, boolean) — vd {@link #isValidMinRating}</li>
 *   <li>{@code @MethodSource}: stub list/object phức tạp — vd {@link #findHighlyRated_mapsRepositoryResultToDto}</li>
 *   <li><b>Không cần MongoDB</b> — chạy rất nhanh, phù hợp CI</li>
 * </ul>
 *
 * <p>Chạy: {@code ./mvnw test -Dtest=MovieServiceTest}</p>
 */
@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

	/** Đối tượng giả — không gọi Mongo thật. */
	@Mock
	private MovieRepository movieRepository;

	/**
	 * Service thật + các {@code @Mock} được tiêm vào field.
	 * (MovieService dùng constructor injection → Mockito khớp được.)
	 */
	@InjectMocks
	private MovieService movieService;

	@Test
	@DisplayName("findHighlyRated: map Entity → DTO và gọi đúng repository")
	void findHighlyRated_mapsEntitiesFromRepository() {
		// ----- Arrange (Given): chuẩn bị dữ liệu giả -----
		MovieModel entity = new MovieModel("1", "Inception", 2010,
				List.of("Sci-Fi"), "Nolan", 8.8);
		when(movieRepository.findByRatingGreaterThanEqual(8.0))
				.thenReturn(List.of(entity));

		// ----- Act (When): gọi method đang test -----
		List<MovieDto> result = movieService.findHighlyRated(8.0);

		// ----- Assert (Then): kiểm tra kết quả + tương tác -----
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getTitle()).isEqualTo("Inception");
		assertThat(result.get(0).getRating()).isEqualTo(8.8);
		verify(movieRepository).findByRatingGreaterThanEqual(8.0);
	}

	@Test
	@DisplayName("findHighlyRated: repo rỗng → list rỗng")
	void findHighlyRated_returnsEmpty_whenRepositoryEmpty() {
		when(movieRepository.findByRatingGreaterThanEqual(8.0))
				.thenReturn(List.of());

		assertThat(movieService.findHighlyRated(8.0)).isEmpty();
	}

	@Test
	@DisplayName("getById: không có phim → ResourceNotFoundException")
	void getById_throws_whenMovieMissing() {
		when(movieRepository.findById("missing")).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> movieService.getById("missing"));
		verify(movieRepository).findById("missing");
	}

	@Test
	@DisplayName("getById: có phim → trả DTO đúng title")
	void getById_returnsDto_whenMovieExists() {
		MovieModel entity = new MovieModel("42", "Interstellar", 2014,
				List.of("Sci-Fi"), "Nolan", 8.6);
		when(movieRepository.findById("42")).thenReturn(Optional.of(entity));

		MovieDto dto = movieService.getById("42");

		assertThat(dto.getId()).isEqualTo("42");
		assertThat(dto.getTitle()).isEqualTo("Interstellar");
	}

	// name: {0}/{1} = tham số theo VỊ TRÍ (không cần trùng tên biến min, expected)
	@ParameterizedTest(name = "min={0} → {1}")
	@CsvSource({
			"0.0, true",
			"8.0, true",
			"10.0, true",
			"-0.1, false",
			"10.1, false"
	})
	void isValidMinRating(double min, boolean expected) {
		assertThat(movieService.isValidMinRating(min)).isEqualTo(expected);
	}

	/**
	 * Data-driven với {@code @MethodSource}: mỗi case nêu rõ <b>repo trả list nào</b>.
	 *
	 * <p>{@code name = "min={0} → size={2}"}: {@code {0}} = min, {@code {2}} = expectedSize;
	 * bỏ {@code {1}} (list) cho báo cáo gọn. Chữ {@code min}/{@code size} chỉ là nhãn hiển thị.</p>
	 *
	 * <p><b>Không</b> viết lại điều kiện lọc ({@code min <= 8.8}) trong {@code when(...)} —
	 * việc filter thuộc Mongo/Repository (xem {@code MovieRepositoryTest}).
	 * Unit Service chỉ kiểm: “repo trả X → Service map thành DTO đúng size/nội dung”.</p>
	 */
	@ParameterizedTest(name = "min={0} → size={2}")
	@MethodSource("highlyRatedCases")
	void findHighlyRated_mapsRepositoryResultToDto(
			double min,
			List<MovieModel> repoResult,
			int expectedSize) {
		when(movieRepository.findByRatingGreaterThanEqual(min))
				.thenReturn(repoResult);

		List<MovieDto> result = movieService.findHighlyRated(min);

		assertThat(result).hasSize(expectedSize);
		verify(movieRepository).findByRatingGreaterThanEqual(min);
	}

	/**
	 * Nguồn dữ liệu cho {@link #findHighlyRated_mapsRepositoryResultToDto}.
	 *
	 * <p>Mỗi {@link Arguments}: {@code min}, list entity mà mock repo trả về, size kỳ vọng.</p>
	 */
	static Stream<Arguments> highlyRatedCases() {
		MovieModel inception = new MovieModel(
				"1", "Inception", 2010, List.of("Sci-Fi"), "Nolan", 8.8);
		MovieModel darkKnight = new MovieModel(
				"2", "The Dark Knight", 2008, List.of("Action"), "Nolan", 9.0);

		return Stream.of(
				Arguments.of(8.0, List.of(inception), 1),
				Arguments.of(9.5, List.of(), 0),
				Arguments.of(8.0, List.of(inception, darkKnight), 2));
	}

}
