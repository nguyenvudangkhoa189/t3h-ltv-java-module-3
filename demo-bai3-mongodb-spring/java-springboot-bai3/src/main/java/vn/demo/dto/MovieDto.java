package vn.demo.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.demo.model.MovieModel;

/**
 * DTO (Data Transfer Object) cho REST API và màn hình chi tiết.
 *
 * <p>Nhiệm vụ của tầng DTO: là đối tượng trung gian để truyền/nhận dữ liệu giữa
 * Controller và Service, KHÔNG dùng entity trực tiếp ở tầng Controller.</p>
 */
@Data
@NoArgsConstructor
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

	/** Chuyển entity sang DTO (Service gọi khi trả dữ liệu ra Controller). */
	public static MovieDto fromEntity(MovieModel movie) {
		if (movie == null) {
			return null;
		}
		MovieDto dto = new MovieDto();
		dto.setId(movie.getId());
		dto.setTitle(movie.getTitle());
		dto.setYear(movie.getYear());
		dto.setGenre(movie.getGenre());
		dto.setDirector(movie.getDirector());
		dto.setRating(movie.getRating());
		return dto;
	}

	/** Chuyển DTO sang entity (Service gọi khi lưu xuống database). */
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
