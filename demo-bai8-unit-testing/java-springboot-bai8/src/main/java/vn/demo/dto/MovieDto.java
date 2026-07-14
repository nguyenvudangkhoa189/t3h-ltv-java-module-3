package vn.demo.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.demo.model.MovieModel;

/**
 * DTO trả ra API — Controller không trả {@link MovieModel} trực tiếp.
 *
 * <p>{@code @AllArgsConstructor} giúp viết test nhanh
 * ({@code new MovieDto("1", "Inception", ...)}).</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {

	private String id;

	@NotBlank(message = "Title không được để trống")
	private String title;

	@Min(value = 1888, message = "Year phải từ 1888 trở lên")
	private Integer year;

	private List<String> genre;

	@Size(max = 120, message = "Director tối đa 120 ký tự")
	private String director;

	@Min(value = 0, message = "Rating tối thiểu là 0")
	private Double rating;

	/** Entity → DTO (Service gọi trước khi trả Controller). */
	public static MovieDto fromEntity(MovieModel movie) {
		if (movie == null) {
			return null;
		}
		return new MovieDto(
				movie.getId(),
				movie.getTitle(),
				movie.getYear(),
				movie.getGenre(),
				movie.getDirector(),
				movie.getRating());
	}

	/** DTO → Entity (khi tạo/cập nhật). */
	public MovieModel toEntity() {
		MovieModel movie = new MovieModel();
		movie.setId(id);
		movie.setTitle(title);
		movie.setYear(year);
		movie.setGenre(genre);
		movie.setDirector(director);
		movie.setRating(rating);
		return movie;
	}

}
