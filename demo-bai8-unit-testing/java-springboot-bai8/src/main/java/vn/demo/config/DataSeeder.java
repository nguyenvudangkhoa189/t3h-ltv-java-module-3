package vn.demo.config;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.model.MovieModel;
import vn.demo.repository.MovieRepository;

/**
 * Nạp vài phim mẫu khi collection rỗng — chỉ để gọi API bằng tay ({@code curl}/Postman).
 *
 * <p><b>Không dùng trong unit/slice test:</b> {@code MovieServiceTest} / {@code @WebMvcTest}
 * mock Service/Repo; {@code @DataMongoTest} tự seed trong {@code @BeforeEach}.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.movies.seed-on-startup", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {

	private final MovieRepository movieRepository;

	@Override
	public void run(ApplicationArguments args) {
		if (movieRepository.count() > 0) {
			return;
		}
		movieRepository.saveAll(List.of(
				movie("Inception", 2010, List.of("Sci-Fi"), "Christopher Nolan", 8.8),
				movie("The Dark Knight", 2008, List.of("Action"), "Christopher Nolan", 9.0),
				movie("Interstellar", 2014, List.of("Sci-Fi"), "Christopher Nolan", 8.6),
				movie("Barbie", 2023, List.of("Comedy"), "Greta Gerwig", 6.9)));
		log.info("Đã seed {} phim mẫu vào collection movies", movieRepository.count());
	}

	private static MovieModel movie(String title, int year, List<String> genre, String director, double rating) {
		MovieModel m = new MovieModel();
		m.setTitle(title);
		m.setYear(year);
		m.setGenre(genre);
		m.setDirector(director);
		m.setRating(rating);
		return m;
	}

}
