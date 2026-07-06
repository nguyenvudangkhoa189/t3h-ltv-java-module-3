package vn.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import vn.demo.dto.MovieDto;
import vn.demo.model.MovieModel;

class MovieDtoTest {

	@Test
	void fromEntity_mapsAllFields() {
		MovieModel movie = new MovieModel("1", "Inception", 2010, List.of("Action", "Sci-Fi"), "Nolan", 8.8);

		MovieDto dto = MovieDto.fromEntity(movie);

		assertThat(dto.getId()).isEqualTo("1");
		assertThat(dto.getTitle()).isEqualTo("Inception");
		assertThat(dto.getGenre()).containsExactly("Action", "Sci-Fi");
	}

	@Test
	void toEntity_mapsAllFields() {
		MovieDto dto = new MovieDto();
		dto.setTitle("Dune");
		dto.setYear(2021);
		dto.setGenre(List.of("Sci-Fi"));
		dto.setRating(8.0);

		MovieModel movie = dto.toEntity();

		assertThat(movie.getTitle()).isEqualTo("Dune");
		assertThat(movie.getYear()).isEqualTo(2021);
	}

}
