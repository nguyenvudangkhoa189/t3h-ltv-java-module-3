package vn.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import vn.demo.dto.MovieFormDto;
import vn.demo.model.MovieModel;

/**
 * Unit test thuần (không cần MongoDB) cho việc chuyển đổi MovieFormDto <-> MovieModel.
 */
class MovieFormDtoTest {

	@Test
	void toEntity_parsesGenreFromCommaSeparatedText() {
		MovieFormDto form = new MovieFormDto();
		form.setTitle("Inception");
		form.setYear(2010);
		form.setGenreText("Action, Sci-Fi , Thriller");
		form.setRating(8.8);

		MovieModel movie = form.toEntity();

		assertThat(movie.getTitle()).isEqualTo("Inception");
		assertThat(movie.getGenre()).containsExactly("Action", "Sci-Fi", "Thriller");
	}

	@Test
	void fromEntity_joinsGenreIntoText() {
		MovieModel movie = new MovieModel(null, "Dune", 2021, List.of("Adventure", "Sci-Fi"), "Denis Villeneuve", 8.0);

		MovieFormDto form = MovieFormDto.fromEntity(movie);

		assertThat(form.getGenreText()).isEqualTo("Adventure, Sci-Fi");
		assertThat(form.getYear()).isEqualTo(2021);
	}

}
