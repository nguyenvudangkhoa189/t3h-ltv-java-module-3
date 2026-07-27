package vn.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO — form admin thêm/sửa phim (bind Thymeleaf, không bind {@code MovieModel}).
 *
 * <p>Flow: view ↔ Controller {@code @Valid} → Service map sang entity + normalize.</p>
 */
@Data
public class MovieFormDto {

	private String id;
	private String showId;

	@NotBlank(message = "Title is required")
	private String title;

	@NotBlank(message = "Type is required")
	@Pattern(regexp = "Movie|TV Show", message = "Type must be Movie or TV Show")
	private String type;

	private String director;
	private String cast;
	private String country;
	private String dateAdded;

	@NotNull(message = "Release year is required")
	private Integer releaseYear;

	private String rating;
	private String duration;
	private String listedIn;
	private String description;

}
